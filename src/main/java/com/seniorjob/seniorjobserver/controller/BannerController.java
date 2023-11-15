package com.seniorjob.seniorjobserver.controller;

import com.seniorjob.seniorjobserver.dto.BannerDto;
import com.seniorjob.seniorjobserver.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerController {
    private final BannerService bannerService;

    @Autowired
    public BannerController(BannerService bannerService){
        this.bannerService = bannerService;
    }

    @GetMapping
    public ResponseEntity<List<BannerDto>> getBannerUrls() {
        List<BannerDto> bannerUrls = bannerService.getBannerDtos();
        return ResponseEntity.ok(bannerUrls);
    }
}
