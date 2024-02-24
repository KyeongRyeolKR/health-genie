package com.example.healthgenie.base.utils;

import com.example.healthgenie.base.constant.Constants;
import com.example.healthgenie.base.exception.JwtErrorResult;
import com.example.healthgenie.base.exception.JwtException;
import com.example.healthgenie.boundedContext.user.dto.Token;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    @Value("${custom.jwt.secret-key}")
    private String SECRET_KEY;
    private SecretKey key;

    @PostConstruct
    protected void init() {
        String secretKey = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes());
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String getAccessToken(HttpServletRequest request) {
        return request.getHeader("AccessToken");
    }

    public Token createToken(String email, String role) {
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("role", role);

        Date now = new Date();

        String accessToken = builder
                .signWith(key)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + Constants.ACCESS_TOKEN_EXPIRE_COUNT))
                .compact();

        String refreshToken = builder
                .signWith(key)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + Constants.REFRESH_TOKEN_EXPIRE_COUNT))
                .compact();

        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .key(email)
                .build();
    }

    public String getEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isExpired(String token) {
        long exp = Long.parseLong(decodeToken(token).get("exp")) * 1000;
        long now = new Date().getTime();
        log.info("exp = {}", exp);
        log.info("now = {}", now);
        return new Date(exp).before(new Date());
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new JwtException(JwtErrorResult.WRONG_SIGNATURE);
        } catch (ExpiredJwtException e) {
            throw new JwtException(JwtErrorResult.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new JwtException(JwtErrorResult.UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            throw new JwtException(JwtErrorResult.WRONG_TOKEN);
        }
    }

    public Map<String, String> decodeToken(String token) {
        String[] split = token.split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(split[1]));
        payload = payload.replaceAll("[{}\"]", "");

        Map<String, String> map = new HashMap<>();

        String[] contents = payload.split(",");
        for(String content : contents) {
            String[] c = content.split(":");
            map.put(c[0], c[1]);
        }

        return map;
    }
}