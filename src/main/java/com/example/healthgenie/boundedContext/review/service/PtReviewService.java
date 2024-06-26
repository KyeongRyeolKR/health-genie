package com.example.healthgenie.boundedContext.review.service;

import static com.example.healthgenie.base.exception.ErrorCode.DATA_NOT_FOUND;
import static com.example.healthgenie.base.exception.ErrorCode.DUPLICATED;
import static com.example.healthgenie.base.exception.ErrorCode.NOT_VALID;
import static com.example.healthgenie.base.exception.ErrorCode.NO_HISTORY;
import static com.example.healthgenie.base.exception.ErrorCode.NO_PERMISSION;

import com.example.healthgenie.base.exception.CustomException;
import com.example.healthgenie.boundedContext.matching.entity.Matching;
import com.example.healthgenie.boundedContext.matching.entity.MatchingUser;
import com.example.healthgenie.boundedContext.matching.repository.MatchingRepository;
import com.example.healthgenie.boundedContext.matching.repository.MatchingUserRepository;
import com.example.healthgenie.boundedContext.review.dto.PtReviewDeleteResponseDto;
import com.example.healthgenie.boundedContext.review.dto.PtReviewRequestDto;
import com.example.healthgenie.boundedContext.review.dto.PtReviewResponseDto;
import com.example.healthgenie.boundedContext.review.dto.PtReviewUpdateRequest;
import com.example.healthgenie.boundedContext.review.entity.PtReview;
import com.example.healthgenie.boundedContext.review.repository.PtReviewQueryRepository;
import com.example.healthgenie.boundedContext.review.repository.PtReviewRepository;
import com.example.healthgenie.boundedContext.user.entity.User;
import com.example.healthgenie.boundedContext.user.entity.enums.Role;
import com.example.healthgenie.boundedContext.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class PtReviewService {

    private final PtReviewRepository ptReviewRepository;
    private final UserRepository userRepository;
    private final MatchingRepository matchingRepository;
    private final MatchingUserRepository matchingUserRepository;
    private final PtReviewQueryRepository ptReviewQueryRepository;

    @Transactional
    public PtReviewResponseDto addPtReview(PtReviewRequestDto dto, User user) {

        User trainer = userRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new CustomException(DATA_NOT_FOUND, "trainer"));

        User matchingUser = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new CustomException(DATA_NOT_FOUND, "user"));

        ShouldNotBeTrainer(matchingUser, Role.TRAINER);

        MatchingUser userMatching = matchingUserRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(DATA_NOT_FOUND, "matching"));

        List<MatchingUser> trainerMatchings = matchingUserRepository.findAllByUserId(trainer.getId());

        PtReview review = ptReviewRepository.findByMemberIdAndTrainerId(trainer.getId(), matchingUser.getId());

        duplicateReview(review);

        for (MatchingUser match : trainerMatchings) {
            // matching User안에 있는 값들중 matching id값이 같은 경우
            if (match.getMatching().getId().equals(userMatching.getMatching().getId())) {

                Matching matching = matchingRepository.findById(match.getMatching().getId())
                        .orElseThrow(() -> new CustomException(DATA_NOT_FOUND));

                // 작성 날짜가 매칭날짜보다 뒤에 있어야 한다
                if (LocalDate.now().isAfter(matching.getDate().toLocalDate())) {
                    return makePtReview(dto, trainer, user);
                }

                log.warn("일지 작성 날짜가 매칭날짜보다 뒤에 있어야 하는데 그렇지 못함");
                throw new CustomException(NOT_VALID);

            }
        }

        log.warn("해당하는 매칭이 없음");
        throw new CustomException(DATA_NOT_FOUND, "매칭");
    }

    @Transactional
    public PtReviewResponseDto makePtReview(PtReviewRequestDto dto, User trainer, User currentUser) {

        ShouldNotBeTrainer(currentUser, Role.TRAINER);

        PtReview review = dto.toEntity(trainer, currentUser);

        return PtReviewResponseDto.of(ptReviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public PtReviewResponseDto getPtReview(Long reviewId, User user) {
        PtReview review = ptReviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomException(NO_HISTORY));

        reviewWriter(user, review);
        return PtReviewResponseDto.of(review);
    }

    /*
         특정 trainer review list 조회
         review안에서 trainerId를 조회하는데, review안에는 userId/trainerId가 나뉘어 있어서 필요함

    */
    @Transactional(readOnly = true)
    public List<PtReviewResponseDto> getAllTrainerReview(Long trainerId, int page, int size) {

        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new CustomException(DATA_NOT_FOUND));

        ShouldBeTrainer(trainer, trainer.getRole());

        List<PtReview> review = ptReviewQueryRepository.findAllByTrainerId(trainerId, page, size);
        return PtReviewResponseDto.of(review);
    }

    /*
        본인이 작성한 review list 조회
    */
    @Transactional(readOnly = true)
    public List<PtReviewResponseDto> getAllReview(int page, int size, User currentUser) {

        // 트레이너면 후기를 작성할 수 없으니 error
        ShouldNotBeTrainer(currentUser, Role.TRAINER);

        List<PtReview> review = ptReviewQueryRepository.findAllByMemberId(currentUser.getId(), page, size);
        return PtReviewResponseDto.of(review);
    }

    @Transactional
    public PtReviewResponseDto updateReview(PtReviewUpdateRequest dto, Long reviewId, User user) {

        PtReview review = authorizationReviewWriter(reviewId, user);
        updateEachReviewItem(dto, review);

        return PtReviewResponseDto.of(review);
    }

    private void updateEachReviewItem(PtReviewUpdateRequest dto, PtReview review) {
        if (dto.hasContent()) {
            review.updateContent(dto.getContent());
        }
        if (dto.hasReviewScore()) {
            review.updateScore(dto.getReviewScore());
        }
        if (dto.hasStopReason()) {
            review.updateReason(dto.getStopReason());
        }
    }


    @Transactional
    public PtReviewDeleteResponseDto deletePtReview(Long reviewId, User user) {

        PtReview review = authorizationReviewWriter(reviewId, user);
        ptReviewRepository.deleteById(review.getId());

        return PtReviewDeleteResponseDto.builder()
                .id(review.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public Slice<PtReview> findAll(String keyword, Long lastId, Pageable pageable) {
        return ptReviewQueryRepository.findAll(keyword, lastId, pageable);
    }

    @Transactional(readOnly = true)
    public List<PtReviewResponseDto> findAllByDate(LocalDate searchStartDate, LocalDate searchEndDate) {
        return PtReviewResponseDto.of(ptReviewQueryRepository.findAllByDate(searchStartDate, searchEndDate));
    }

    // review는 회원만 수정 삭제 가능
    private PtReview authorizationReviewWriter(Long id, User member) {
        PtReview review = ptReviewRepository.findById(id)
                .orElseThrow(() -> new CustomException(NO_HISTORY));

        reviewWriter(member, review);
        return review;
    }

    private void reviewWriter(User member, PtReview review) {
        if (!review.getMember().getId().equals(member.getId())) {
            log.warn("this user doesn't have authentication : {}", review.getMember());
            throw new CustomException(DATA_NOT_FOUND);
        }
    }

    private void ShouldNotBeTrainer(User currentUser, Role role) {
        if (currentUser.getRole().equals(role)) {
            log.warn("trainer can't write review ( role : {} )", currentUser.getRole());
            throw new CustomException(NO_PERMISSION);
        }
    }

    private void ShouldBeTrainer(User trainer, Role role) {
        if (!trainer.getRole().equals(role)) {
            log.warn("wrong user role : {} ( is not trainer )", trainer.getRole());
            throw new CustomException(NO_PERMISSION);
        }
    }

    private void duplicateReview(PtReview review) {
        if (review != null) {
            log.warn("duplicate review : {}", review);
            throw new CustomException(DUPLICATED);
        }
    }
}
