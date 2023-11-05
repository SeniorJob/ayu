package com.seniorjob.seniorjobserver.config;

import org.springframework.http.ResponseCookie;

import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
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
