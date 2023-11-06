package com.seniorjob.seniorjobserver.config;

import org.springframework.http.ResponseCookie;

import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

    // 쿠키생성
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(false) // HTTPS일때는 true
                .maxAge(maxAge)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // 쿠키삭제
    public static void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(false)
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
