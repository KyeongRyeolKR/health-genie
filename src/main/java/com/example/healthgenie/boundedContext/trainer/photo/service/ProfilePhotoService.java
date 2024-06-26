package com.example.healthgenie.boundedContext.trainer.photo.service;

import static com.example.healthgenie.base.exception.ErrorCode.DATA_NOT_FOUND;
import static com.example.healthgenie.base.exception.ErrorCode.NO_PERMISSION;

import com.example.healthgenie.base.exception.CustomException;
import com.example.healthgenie.base.utils.S3UploadUtils;
import com.example.healthgenie.boundedContext.trainer.photo.dto.ProfilePhotoDeleteResponseDto;
import com.example.healthgenie.boundedContext.trainer.photo.dto.ProfilePhotoRequest;
import com.example.healthgenie.boundedContext.trainer.photo.entity.TrainerPhoto;
import com.example.healthgenie.boundedContext.trainer.photo.entity.enums.PurposeOfUsing;
import com.example.healthgenie.boundedContext.trainer.photo.repository.TrainerProfilePhotoRepository;
import com.example.healthgenie.boundedContext.trainer.photo.repository.TrainerProfileQueryPhotoRepository;
import com.example.healthgenie.boundedContext.trainer.profile.entity.TrainerInfo;
import com.example.healthgenie.boundedContext.trainer.profile.repository.ProfileRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProfilePhotoService {

    private final ProfileRepository profileRepository;
    private final TrainerProfilePhotoRepository trainerProfilePhotoRepository;
    private final S3UploadUtils s3UploadUtils;
    private final TrainerProfileQueryPhotoRepository trainerProfileQueryPhotoRepository;


    @Transactional
    public List<TrainerPhoto> save(Long profileId, Long userId, ProfilePhotoRequest dto, PurposeOfUsing purpose)
            throws IOException {
        List<TrainerPhoto> photos = new ArrayList<>();

        TrainerInfo profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new CustomException(DATA_NOT_FOUND));

        checkPermission(userId, profile);

        // 이미 profile photo가 있으면 기존 profile은 삭제되고 새롭게 업로드
        if (purpose.equals(PurposeOfUsing.PROFILE)) {
            TrainerPhoto profilePhoto = trainerProfileQueryPhotoRepository.findByPurpose(purpose, userId);
            if (profilePhoto != null) {
                s3UploadUtils.deleteS3Object("trainer-profile-photos", profilePhoto.getInfoPhotoPath());
                trainerProfilePhotoRepository.deleteByInfoId(profileId);
            }
        }

        for (MultipartFile file : dto.getPhotos()) {
            String uploadUrl = s3UploadUtils.upload(file, "trainer-profile-photos");
            String originName = file.getOriginalFilename();

            TrainerPhoto savedPhoto = trainerProfilePhotoRepository.save(
                    dto.toEntity(profile, uploadUrl, originName, purpose));

            photos.add(savedPhoto);
        }

        return photos;
    }

    @Transactional(readOnly = true)
    public TrainerPhoto findById(Long id) {
        return trainerProfilePhotoRepository.findById(id)
                .orElseThrow(() -> new CustomException(DATA_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<TrainerPhoto> findAllByProfileId(Long profileId) {
        return trainerProfilePhotoRepository.findAllByInfoId(profileId);
    }

    @Transactional
    public ProfilePhotoDeleteResponseDto deleteAllByProfileId(Long profileId, Long userId) {
        TrainerInfo profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new CustomException(DATA_NOT_FOUND));

        checkPermission(userId, profile);

        List<TrainerPhoto> photos = findAllByProfileId(profileId);

        for (TrainerPhoto photo : photos) {
            String path = photo.getInfoPhotoPath();
            s3UploadUtils.deleteS3Object("trainer-profile-photos", path);
        }

        trainerProfilePhotoRepository.deleteByInfoId(profileId);

        // 어느 프로필의 사진인지 프로필을 id를 반환
        return ProfilePhotoDeleteResponseDto.builder()
                .id(profileId)
                .build();
    }

    private static void checkPermission(Long userId, TrainerInfo profile) {
        if (!Objects.equals(userId, profile.getMember().getId())) {
            throw new CustomException(NO_PERMISSION);
        }
    }
}
