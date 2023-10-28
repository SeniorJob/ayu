package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MyPageCreateLectureDto {
    private Long create_id;
    private Long uid;
    private String userName;
    private String creator;
    private String category;
    private String title;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recruitEnd_date;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime start_date;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime end_date;
    private Integer max_participants;
    private Integer current_participants;
    private String region;
    private Integer price;
    private LectureEntity.LectureStatus status;
    private LocalDateTime createdDate;

    @Builder
    public MyPageCreateLectureDto(LectureEntity lectureEntity){
        this.create_id = lectureEntity.getCreate_id();
        this.uid = lectureEntity.getUser().getUid();
        this.userName = lectureEntity.getUser().getName();
        this.creator = lectureEntity.getCreator();
        this.category = lectureEntity.getCategory();
        this.title = lectureEntity.getTitle();
        this.recruitEnd_date = lectureEntity.getRecruitEnd_date();
        this.start_date = lectureEntity.getStart_date();
        this.end_date = lectureEntity.getEnd_date();
        this.max_participants = lectureEntity.getMaxParticipants();
        this.current_participants = lectureEntity.getCurrentParticipants();
        this.region = lectureEntity.getRegion();
        this.price = lectureEntity.getPrice();
        this.status = lectureEntity.getStatus();
        this.createdDate = lectureEntity.getCreatedDate();
    }
}
