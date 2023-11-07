package com.seniorjob.seniorjobserver.reponse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String message;
    private Date tokenExpiarationDate;

    public LoginResponse(String accessToken, String userName, Date tokenExpiarationDate) {
        this.accessToken = accessToken;
        this.message = userName + "님이 로그인에 성공하였습니다.";
        this.tokenExpiarationDate = tokenExpiarationDate;
    }

}
