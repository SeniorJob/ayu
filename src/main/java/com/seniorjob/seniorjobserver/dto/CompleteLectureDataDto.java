package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CompleteLectureDataDto {
    // 1단계 데이터
    private Long createId;
    private UserEntity user;
    private Long userId;
    private String creator;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String category;
    private String bankName;
    private String accountName;
    private String accountNumber;
    private Integer price;
    private String title;
    private String content;
    private Integer week;
    private String learningTarget;
    private Integer attendanceRequirements;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recruitEndDate;
    private String region;
    private String imageUrl;
    private LocalDateTime createdDate;
    private LectureEntity.LectureStatus status;

    // 2단계 데이터


}
