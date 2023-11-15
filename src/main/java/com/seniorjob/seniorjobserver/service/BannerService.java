package com.seniorjob.seniorjobserver.service;

import com.seniorjob.seniorjobserver.dto.BannerDto;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BannerService {
    public List<BannerDto> getBannerDtos() {
        List<BannerDto> banners = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            banners.add(new BannerDto(i, "https://seniorjob-bucket.s3.amazonaws.com/배너" + i + ".png"));
        }
        return banners;
    }

}
