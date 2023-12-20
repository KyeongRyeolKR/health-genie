package com.example.healthgenie.boundedContext.email.controller;

import com.example.healthgenie.base.response.Result;
import com.example.healthgenie.boundedContext.email.dto.MailRequestDto;
import com.example.healthgenie.boundedContext.email.service.UserMailService;
import com.univcert.api.UnivCert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor // 생성자 DI
@RequestMapping("/auth/mail")
public class MailController {

    private final UserMailService userMailService;

    @Value("${univCert.key}")
    private String KEY;

    // 이메일 코드전송, 이메일 유효성검사 -> accessToken 필요
    @PostMapping("/send") // http://localhost:1234/auth/mail/send
    public ResponseEntity<Result> sendUnivCertMail(@RequestBody MailRequestDto dto) throws IOException {

        UnivCert.clear(KEY, dto.getUniv_email());

        boolean univ_check = false;
        Map<String,Object> check = UnivCert.check(dto.getUnivName());
        boolean success = (boolean) check.get("success");

        if(success) univ_check = true;

        Map<String, Object> result = UnivCert.certify(KEY, dto.getUniv_email(), dto.getUnivName(), univ_check);

        if((boolean) result.get("success")) {
            userMailService.updateUniv(dto.getUnivName());
        }

        return ResponseEntity.ok(Result.of("이메일이 성공적으로 보내졌습니다."));

    }


    //이메일 코드검증  -> accessToken 필요
    @GetMapping("/verify") // http://localhost:1234/auth/mail/verify
    public ResponseEntity<Result> validMailCode(@RequestBody MailRequestDto dto) throws IOException {

        Map<String, Object> response = UnivCert.certifyCode(KEY, dto.getUniv_email(), dto.getUnivName(), dto.getCode());
        boolean success = (boolean) response.get("success");

        if(success) {
            userMailService.updateUnivVerify();
        }

        return ResponseEntity.ok(Result.of(success ? "검증이 성공했습니다" : "검증이 실패했습니다"));

    }
}
