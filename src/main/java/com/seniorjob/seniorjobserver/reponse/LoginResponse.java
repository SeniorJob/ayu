package com.seniorjob.seniorjobserver.reponse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String message;

    public LoginResponse(String token, String userName) {
        this.token = token;
        this.message = userName + "님이 로그인에 성공하였습니다.";
    }
}
