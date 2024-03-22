package com.example.healthgenie.boundedContext.user.service;

import static com.example.healthgenie.base.exception.Diet.DietErrorResult.TYPE_EMPTY;
import static com.example.healthgenie.base.exception.User.UserErrorResult.ALREADY_SIGN_UP;
import static com.example.healthgenie.base.exception.User.UserErrorResult.DUPLICATED_NICKNAME;
import static com.example.healthgenie.base.exception.User.UserErrorResult.NOT_VALID_FIELD;
import static com.example.healthgenie.base.exception.User.UserErrorResult.PROFILE_PHOTO_UPLOAD_EXCEPTION;
import static com.example.healthgenie.base.exception.User.UserErrorResult.USER_NOT_FOUND;

import com.example.healthgenie.base.exception.Diet.DietException;
import com.example.healthgenie.base.exception.User.UserException;
import com.example.healthgenie.base.utils.S3UploadUtils;
import com.example.healthgenie.boundedContext.routine.entity.Level;
import com.example.healthgenie.boundedContext.user.dto.DietResponse;
import com.example.healthgenie.boundedContext.user.entity.User;
import com.example.healthgenie.boundedContext.user.entity.enums.AuthProvider;
import com.example.healthgenie.boundedContext.user.entity.enums.Gender;
import com.example.healthgenie.boundedContext.user.entity.enums.Role;
import com.example.healthgenie.boundedContext.user.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Transactional(readOnly = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final S3UploadUtils s3UploadUtils;

    @Transactional
    public User signUp(String email, String name, AuthProvider authProvider, Role role) {
        String defaultNickname = createUniqueNickname();

        if (userRepository.existsByEmail(email)) {
            throw new UserException(ALREADY_SIGN_UP);
        }

        User user = User.builder()
                .email(email)
                .name(name)
                .nickname(defaultNickname)
                .authProvider(authProvider)
                .uniName("")
                .role(role)
                .level(Level.EMPTY)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User signUp(String email, String name, AuthProvider authProvider) {
        return signUp(email, name, authProvider, Role.EMPTY);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
    }

    @Transactional
    public User update(User user, Role role) {
        if (Objects.isNull(role)) {
            throw new UserException(NOT_VALID_FIELD);
        }

        return update(user, role, null, null, null, null, null, null, null, null, null, null);
    }

    @Transactional
    public User update(User user, Level level) {
        if (Objects.isNull(level)) {
            throw new UserException(NOT_VALID_FIELD);
        }

        return update(user, null, level, null, null, null, null, null, null, null, null, null);
    }

    @Transactional
    public User update(User user, MultipartFile photo, String nickname, Gender gender, LocalDateTime birth,
                       Double height, Double weight, Double muscleWeight) {
        return update(user, null, null, photo, nickname, gender, birth, height, weight, muscleWeight, null, null);
    }

    @Transactional
    public User update(User user, String uniName, Boolean emailVerify) {
        return update(user, null, null, null, null, null, null, null, null, null, uniName, emailVerify);
    }

    public DietResponse calculate(User user, Integer type) {
        if (Objects.isNull(type)) {
            throw new DietException(TYPE_EMPTY);
        }

        Gender gender = user.getGender();
        double weight = user.getWeight();
        double height = user.getHeight();
        int age = LocalDateTime.now().getYear() - user.getBirth().getYear() + 1;

        double basic;
        if (gender == Gender.MALE) {
            basic = 66 + (13.7 * weight) + (5 * height) - (6.8 * age);
        } else if (gender == Gender.FEMALE) {
            basic = 655 + (9.6 * weight) + (1.7 * height) - (4.7 * age);
        } else {
            throw new UserException(NOT_VALID_FIELD);
        }

        double active = switch (type) {
            case 1 -> basic * 1.2;
            case 2 -> basic * 1.4;
            case 3 -> basic * 1.6;
            case 4 -> basic * 1.8;
            default -> throw new UserException(NOT_VALID_FIELD);
        };

        return DietResponse.builder()
                .basicRate((int) Math.round(basic))
                .activeRate((int) Math.round(active))
                .build();
    }

    private User update(User user, Role role, Level level, MultipartFile photo, String nickname, Gender gender,
                        LocalDateTime birth, Double height, Double weight, Double muscleWeight, String uniName,
                        Boolean emailVerify) {
        user = findById(user.getId());

        // 역할
        if (Objects.nonNull(role)) {
            user.updateRole(role);
        }
        // 단계
        if (Objects.nonNull(level)) {
            user.updateLevel(level);
        }
        // 프로필 사진
        if (Objects.nonNull(photo) && !photo.isEmpty()) {
            String path = uploadAndDelete(photo, user.getProfilePhoto());

            user.updateProfilePhoto(path);
        }
        // 닉네임
        if (StringUtils.hasText(nickname)) {
            if (userRepository.existsByNickname(nickname)) {
                throw new UserException(DUPLICATED_NICKNAME);
            }
            user.updateNickname(nickname);
        }
        // 성별
        if (Objects.nonNull(gender)) {
            user.updateGender(gender);
        }
        // 생년월일
        if (Objects.nonNull(birth)) {
            user.updateBirth(birth);
        }
        // 키
        if (Objects.nonNull(height)) {
            user.updateHeight(height);
        }
        // 몸무게
        if (Objects.nonNull(weight)) {
            user.updateWeight(weight);
        }
        // 골격근량
        if (Objects.nonNull(muscleWeight)) {
            user.updateMuscleWeight(muscleWeight);
        }
        // 학교 이름
        if (StringUtils.hasText(uniName)) {
            user.updateUniname(uniName);
        }
        // 이메일 인증 확인
        if (Objects.nonNull(emailVerify) && emailVerify) {
            user.updateEmailVerify(true);
        }

        return user;
    }

    private String createUniqueNickname() {
        String nickname;

        Random random = new Random();
        while (true) {
            String temp = String.valueOf(random.nextInt(99999999) + 1);

            if (!userRepository.existsByNickname(temp)) {
                nickname = temp;
                break;
            }
        }

        return nickname;
    }

    private String uploadAndDelete(MultipartFile uploadPhoto, String deletePhotoPath) {
        try {
            String uploadedPath = s3UploadUtils.upload(uploadPhoto, "profile-photo");

            if (StringUtils.hasText(deletePhotoPath)) {
                s3UploadUtils.deleteS3Object("profile-photo", deletePhotoPath);
            }

            return uploadedPath;
        } catch (IOException e) {
            throw new UserException(PROFILE_PHOTO_UPLOAD_EXCEPTION);
        }
    }
}