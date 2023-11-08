package com.seniorjob.seniorjobserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seniorjob.seniorjobserver.exception.*;
import com.seniorjob.seniorjobserver.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService blacklistService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, TokenBlacklistService blacklistService) {
        this.jwtTokenProvider = tokenProvider;
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwtToken = jwtTokenProvider.resolveToken(request);
            if (jwtToken != null) {
                if (jwtTokenProvider.validateToken(jwtToken)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(jwtToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (CustomJwtExpiredException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, "AuthenticationFilter : 만료된 JWT 토큰입니다.");
        } catch (CustomJwtUnsupportedException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, "AuthenticationFilter : 지원되지 않는 JWT 토큰입니다.");
        } catch (CustomJwtMalformedException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, "AuthenticationFilter : 잘못된 구조의 JWT 토큰입니다.");
        } catch (CustomJwtSignatureException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, "AuthenticationFilter : JWT 토큰의 서명을 검증할 수 없습니다.");
        } catch (CustomJwtIllegalArgumentException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, "AuthenticationFilter : JWT 토큰이 올바르지 않습니다.");
        }
    }

    private void setErrorResponse(HttpStatus status, HttpServletResponse response, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), errorDetails);
    }

}