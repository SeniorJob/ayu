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
        banners.add(new BannerDto(1, "https://seniorjob-bucket.s3.amazonaws.com/배너1 취업은어디.png", "취업은 어디?"));
        banners.add(new BannerDto(2, "https://seniorjob-bucket.s3.amazonaws.com/배너2 SeniorJob.png", "SeniorJob"));
        banners.add(new BannerDto(3, "https://seniorjob-bucket.s3.amazonaws.com/배너3 중장년 일자리.png", "중장년 일자리"));
        banners.add(new BannerDto(4, "https://seniorjob-bucket.s3.amazonaws.com/배너4 강좌개설하기.png", "강좌 개설"));
        banners.add(new BannerDto(5, "https://seniorjob-bucket.s3.amazonaws.com/배너5 강좌찾기.png", "강좌 찾기"));
        banners.add(new BannerDto(6, "https://seniorjob-bucket.s3.amazonaws.com/배너6 동호회개설.png", "동호회개설"));
        banners.add(new BannerDto(7, "https://seniorjob-bucket.s3.amazonaws.com/배너7 시니어활동.png", "시니어활동"));
        banners.add(new BannerDto(8, "https://seniorjob-bucket.s3.amazonaws.com/배너8 제2의 학교.png", "제2의학교"));

        return banners;
    }


}
