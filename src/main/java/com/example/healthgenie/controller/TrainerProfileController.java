package com.example.healthgenie.controller;

import com.example.healthgenie.dto.*;
import com.example.healthgenie.service.TrainerProfileServie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TrainerProfileController {


    private final TrainerProfileServie profileServie;


    @PostMapping("/profile/add")
    public ResponseEntity profileAdd(@RequestBody TrainerProfileRequestDto dto){

        Long userId =1L;//회원기능 구현 시 userAdapter에서 Id받는 것으로 대체

        TrainerProfileResponseDto result = profileServie.profileAdd(dto,userId);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @PostMapping("/profile/modify")
    public ResponseEntity profileModifiy(@RequestBody TrainerProfileModifyRequestDto dto){

        Long userId = 1L;//회원기능 구현 시 userAdapter에서 Id받는 것으로 대체

        TrainerProfileModifiyResponseDto result = profileServie.profileModify(dto,userId);

        return new ResponseEntity(result,HttpStatus.OK);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity profileGet(@PathVariable("id") Long id){

        TrainerProfileGetResponseDto result =profileServie.profileGet(id);

        return new ResponseEntity(result,HttpStatus.OK);
    }
}
