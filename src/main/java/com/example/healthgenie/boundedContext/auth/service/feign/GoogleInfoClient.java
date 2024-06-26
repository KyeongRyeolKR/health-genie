package com.example.healthgenie.boundedContext.auth.service.feign;

import com.example.healthgenie.boundedContext.auth.dto.GoogleUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "google-info", url = "https://www.googleapis.com")
public interface GoogleInfoClient {

    @GetMapping("/oauth2/v2/userinfo")
    GoogleUserInfo getUserInfo(@RequestHeader("Authorization") String authorization);
}
