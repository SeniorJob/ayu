package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.LectureApplyEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MyPageLectureApplyDto {
    private Long create_id; // 강좌ID
    private Long le_id; // 강좌신청ID
    private Long uid; // 유저ID
    private String userName;
    private String creator;
    private String category;
    private String image_url;
    private String title;
    private String content;
    private String learning_target;
    private Integer week;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recruitEnd_date;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime start_date;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime end_date;

    // 모집 마감까지 남은 일
    public String getDaysUntilRecruitEndMessage() {

        if (status != LectureEntity.LectureStatus.신청가능상태){
            return null;
        }
        if(recruitEnd_date == null){
            return "모집 마감일이 설정되지 않았습니다.";
        }
        LocalDateTime now = LocalDateTime.now();
        int days = (int) ChronoUnit.DAYS.between(now, recruitEnd_date);
        return "모집 마감까지 " + days + "일 남았습니다!";
    }

    private Integer max_participants;
    private Integer current_participants;
    private String region;
    private Integer price;
    private String account_name;
    private String account_number;
    private LectureEntity.LectureStatus status;
    private LocalDateTime createdDate;

    @Builder
    public MyPageLectureApplyDto(LectureApplyEntity lectureApply, LectureEntity lectureEntity){
        this.create_id = lectureEntity.getCreate_id();
        this.uid = lectureEntity.getUser().getUid();
        this.le_id = lectureApply.getLeId();
        this.userName = lectureEntity.getUser().getName();
        this.creator = lectureEntity.getCreator();
        this.category = lectureEntity.getCategory();
        this.image_url = lectureEntity.getImage_url();
        this.title = lectureEntity.getTitle();
        this.content = lectureEntity.getContent();
        this.learning_target = lectureEntity.getLearningTarget();
        this.week = lectureEntity.getWeek();
        this.recruitEnd_date = lectureEntity.getRecruitEnd_date();
        this.start_date = lectureEntity.getStart_date();
        this.end_date = lectureEntity.getEnd_date();
        this.max_participants = lectureEntity.getMaxParticipants();
        this.current_participants = lectureEntity.getCurrentParticipants();
        this.region = lectureEntity.getRegion();
        this.price = lectureEntity.getPrice();
        this.account_name = lectureEntity.getAccount_name();
        this.account_number = lectureEntity.getAccount_number();
        this.status = lectureEntity.getStatus();
        this.createdDate = lectureEntity.getCreatedDate();
    }
}
