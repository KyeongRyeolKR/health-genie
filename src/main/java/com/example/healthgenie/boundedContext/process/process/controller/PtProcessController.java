package com.example.healthgenie.boundedContext.process.process.controller;


import com.example.healthgenie.base.response.Result;
import com.example.healthgenie.boundedContext.process.process.dto.PtProcessDeleteResponseDto;
import com.example.healthgenie.boundedContext.process.process.dto.PtProcessRequestDto;
import com.example.healthgenie.boundedContext.process.process.dto.PtProcessResponseDto;
import com.example.healthgenie.boundedContext.process.process.dto.PtProcessSliceResponse;
import com.example.healthgenie.boundedContext.process.process.service.PtProcessService;
import com.example.healthgenie.boundedContext.user.entity.User;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/process")
@Slf4j
public class PtProcessController {
    private final PtProcessService processService;

    // trainer가 작성
    @PostMapping("/trainers")
    public ResponseEntity<Result> addPtProcess(@AuthenticationPrincipal User user,
                                               @RequestBody @Valid PtProcessRequestDto dto) {

        PtProcessResponseDto response = processService.addPtProcess(dto, user);
        return ResponseEntity.ok(Result.of(response));
    }

    /*
        본인은 본인것만 볼 수 있기에 userId 같은거 필요없다 [ security로 가능 ]

        트레이너가 작성한 전체 피드백 모아보기 [ 트레이너용 관리페이지에서 사용 ]
        관리페이지 : 최근 작성한 글들 순서로 정렬해 놓은 것이기 때문에 상위 3개씩 가져다가 쓰면 된다.
     */
    @GetMapping("/trainers/list")
    public ResponseEntity<Result> getAllTrainerProcess(@RequestParam(required = false, defaultValue = "0") int page,
                                                       @AuthenticationPrincipal User user) {

        List<PtProcessResponseDto> response = processService.getAllTrainerProcess(page, 5, user);
        return ResponseEntity.ok(Result.of(response));
    }

    /*
        본인이 관련 모든 피드백 모아보기 [ 회원용 관리페이지에서 사용 ]
        관리페이지 : 최근 작성한 글들 순서로 정렬해 놓은 것이기 때문에 상위 3개씩 가져다가 쓰면 된다.
     */
    @GetMapping("/my/list")
    public ResponseEntity<Result> getAllMyProcess(@RequestParam(required = false, defaultValue = "0") int page,
                                                  @AuthenticationPrincipal User user) {

        List<PtProcessResponseDto> response = processService.getAllMyProcess(page, 5, user);
        return ResponseEntity.ok(Result.of(response));
    }

    // 일지를 검색으로 찾기
    @GetMapping("/list/findAll")
    public ResponseEntity<Result> findAll(@RequestParam(name = "search", defaultValue = "") String keyword,
                                          @RequestParam(name = "lastId", required = false) Long lastId,
                                          Pageable pageable, @AuthenticationPrincipal User user) {
        PtProcessSliceResponse response = PtProcessSliceResponse.of(
                processService.findAll(keyword, lastId, pageable, user));

        return ResponseEntity.ok(Result.of(response));
    }

    // 날짜 필터링으로 일지 모아보기
    @GetMapping("/list/dateFilter")
    public ResponseEntity<Result> findAll(
            @RequestParam(required = false, defaultValue = "1900-01-01") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate searchStartDate,
            @RequestParam(required = false, defaultValue = "9999-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate searchEndDate) {

        List<PtProcessResponseDto> response = processService.findAllByDate(searchStartDate, searchEndDate);

        return ResponseEntity.ok(Result.of(response));
    }


    @GetMapping("/details/{processId}")
    public ResponseEntity<Result> getProcess(@PathVariable Long processId,
                                             @AuthenticationPrincipal User user) {
        PtProcessResponseDto response = processService.getPtProcess(processId, user);
        return ResponseEntity.ok(Result.of(response));
    }

    // 트레이너만 삭제 기능이 가능
    @DeleteMapping("/{processId}")
    public ResponseEntity<Result> deleteProcess(@PathVariable Long processId,
                                                @AuthenticationPrincipal User user) {

        PtProcessDeleteResponseDto response = processService.deletePtProcess(processId, user);

        return ResponseEntity.ok(Result.of(response));
    }

}
