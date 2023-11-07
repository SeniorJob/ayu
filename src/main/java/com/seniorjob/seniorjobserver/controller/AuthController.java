package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.config.JwtTokenProvider;
import com.seniorjob.seniorjobserver.reponse.ErrorResponse;
import com.seniorjob.seniorjobserver.reponse.LogoutResponse;
import com.seniorjob.seniorjobserver.service.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService blacklistService;
    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(JwtTokenProvider jwtTokenProvider, TokenBlacklistService blacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.blacklistService = blacklistService;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // "Authorization" 헤더에서 토큰을 추출합니다.
        String token = jwtTokenProvider.resolveToken(request);

        // 토큰의 유효성을 검증하고, 블랙리스트에 없는지 확인합니다.
        if (token != null && jwtTokenProvider.validateToken(token) && !blacklistService.isTokenBlacklisted(token)) {
            // 토큰을 블랙리스트에 추가합니다.
            blacklistService.blacklistToken(token);

            // 로그아웃을 기록합니다.
            String username = jwtTokenProvider.getUserPk(token);
            log.info("{}님이 로그아웃하였습니다.", username);

            // 클라이언트에 로그아웃 성공 메시지를 보냅니다.
            return ResponseEntity.ok().body(new LogoutResponse(username + "님이 로그아웃에 성공하였습니다."));
        } else {
            // 토큰이 유효하지 않거나 블랙리스트에 있다면 오류 메시지를 보냅니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("INVALID_TOKEN", "유효하지 않거나 블랙리스트에 등록된 토큰입니다."));
        }
    }
}
