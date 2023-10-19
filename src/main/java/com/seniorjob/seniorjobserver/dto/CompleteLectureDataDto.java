package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.domain.entity.WeekEntity;
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
    private Long uid;
    private String name;
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
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recruitEndDate;
    private String region;
    private String imageUrl;
    private LocalDateTime createdDate;

    // 2단계 데이터 week
    private Long week_id;
    private Long create_id;
    private String lectureTitle;
    private Integer week_number;
    private String week_title;
    private LocalDateTime createdDateWeek;

    // 2단계 데이터 weekPlan


    // 2단계 데이터 attendan


}
