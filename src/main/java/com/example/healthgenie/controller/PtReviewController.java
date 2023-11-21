package com.example.healthgenie.controller;

import com.example.healthgenie.domain.ptreview.dto.PtReviewRequestDto;
import com.example.healthgenie.domain.ptreview.dto.PtReviewResponseDto;
import com.example.healthgenie.domain.ptreview.entity.PtReview;
import com.example.healthgenie.service.PtReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
@Slf4j
public class PtReviewController {

    private final PtReviewService reviewService;

    /*
        프론트에서 트레이너에게는 후기 작성란이 없기에 작성 불가하게 막아줌.
        프론트에서 본인이 작성한 후기에만 수정할 수 있도록 막아줌.
     */
    @PostMapping("/write")// http://localhost:1234/review/write
    public ResponseEntity addReview(@RequestBody PtReviewRequestDto dto){

        Long userId = dto.getUserId();
        PtReviewResponseDto responseDto = reviewService.addPtReview(dto,userId);
        return new ResponseEntity(responseDto,HttpStatus.OK);
    }

    // 수정하려고 form 불러오기 -> 조회가 필요함 결국은
    @GetMapping("/{reviewId}") // http://localhost:1234/review
    public ResponseEntity getReview(@PathVariable Long reviewId){

        PtReviewResponseDto responseDto = reviewService.getPtReview(reviewId);
        return new ResponseEntity(responseDto,HttpStatus.OK);
    }

    // 트레이너에 관한 후기 전체 모아보기
    @GetMapping("/list/trainer/{trainerId}") // http://localhost:1234/review/list/trainer/{trainerId}
    public Page<PtReviewResponseDto> getAllTrainerReview(@PathVariable Long trainerId, @RequestParam(required = false, defaultValue = "0") int page){
        // 5개씩 페이징 처리
        int size = 5;
        return reviewService.getAllTrainerReview(trainerId, page, size);
    }

    // 본인이 작성한 후기 전체 모아보기
    @GetMapping("/list/my/{userId}") // http://localhost:1234/review/list/my/{userId}
    public Page<PtReviewResponseDto> getAllMyReview(@PathVariable Long userId, @RequestParam(required = false, defaultValue = "0") int page){
        // 5개씩 페이징 처리
        int size = 5;
        return reviewService.getAllMyReview(userId, page, size);
    }

    // 수정
    @PostMapping("/edit/{reviewId}")// http://localhost:1234/review/edit/{reviewId}
    public ResponseEntity editReview(@RequestBody PtReviewRequestDto dto, @PathVariable Long reviewId){

        Long id = reviewService.editPtReview(dto,reviewId);
        return new ResponseEntity(id,HttpStatus.OK);
    }

    // 본인만 삭제 가능하게 하기 -> 프론트에서 기능을 숨기면 되어서 구별 로직뺌
    @DeleteMapping("/member/{reviewId}") // http://localhost:1234/review/member/{reviewId}
    public ResponseEntity deleteReview(@PathVariable Long reviewId) {
        reviewService.deletePtReview(reviewId);

        return new ResponseEntity("후기 삭제가 성공했습니다",HttpStatus.OK);
    }
}
