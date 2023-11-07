package com.seniorjob.seniorjobserver.config;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

@RequiredArgsConstructor
@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException, java.io.IOException {
        String errorMessage = "알 수 없는 오류가 발생했습니다.";

        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 틀렸습니다.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않는 계정입니다.";
        }

        setDefaultFailureUrl("/login?error=true&message=" + URLEncoder.encode(errorMessage, "UTF-8"));
        super.onAuthenticationFailure(request, response, exception);
    }
}
