package com.seniorjob.seniorjobserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BannerDto {
    private int bannerId;
    private String bannerUrl;
    private String bannerTitle;
}
