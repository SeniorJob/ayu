package com.seniorjob.seniorjobserver.reponse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LogoutResponse {
    private String message;

    public LogoutResponse(String message) {
        this.message = message;
    }
}
