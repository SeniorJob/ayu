package com.seniorjob.seniorjobserver.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TokenDto {
    private String token;

    public TokenDto(String token) {
        this.token = token;
    }
}