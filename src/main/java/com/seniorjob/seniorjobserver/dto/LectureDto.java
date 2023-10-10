package com.seniorjob.seniorjobserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity.LectureStatus;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import lombok.*;
import org.apache.catalina.User;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LectureDto {

    private Long create_id;
    private UserEntity user;

    private String userName;
    public UserEntity getUser() {
        return this.user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    private String creator;
    private Integer max_participants;
    private Integer current_participants;
    private String category;
    private String bank_name;
    private String account_name;
    private String account_number;
    private Integer price;
    private String title;
    private String content;
    //private String cycle;
    private Integer week;
    //private Integer count;
    private String learning_target;
    private Integer attendance_requirements;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime start_date;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime end_date;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recruitEnd_date;
    private String region;
    private String image_url;

    private LocalDateTime createdDate;

    private LectureEntity.LectureStatus status;

    public LectureEntity.LectureStatus getStatus() {
        return status;
    }

    public void setStatus(LectureEntity.LectureStatus status) {
        this.status = status;
    }

    private Boolean recruitmentClosed;

    public LectureEntity toEntity(UserEntity userEntity) {
        LectureEntity lectureEntity = LectureEntity.builder()
                .create_id(create_id)
                .user(user)
                .creator(creator)
                .maxParticipants(max_participants)
                .currentParticipants(current_participants)
                .category(category)
                .bank_name(bank_name)
                .account_name(account_name)
                .account_number(account_number)
                .price(price)
                .title(title)
                .content(content)
                //.cycle(cycle)
                .week(week)
                .learningTarget(learning_target)
                .attendanceRequirements(attendance_requirements)
                //.count(count)
                .start_date(start_date)
                .end_date(end_date)
                .region(region)
                .image_url(image_url)
                .createdDate(createdDate)
                .recruitEnd_date(recruitEnd_date)
                .build();

        lectureEntity.setUser(userEntity);
        lectureEntity.updateStatus();
        this.status = lectureEntity.getStatus();

        return lectureEntity;
    }

    @Builder
    public LectureDto(Long create_id,String creator, UserEntity user,String userName, Integer max_participants, Integer current_participants, String category,
                      String bank_name, String account_name, String account_number, Integer price, String title, String content,
                      Integer week, String learning_target, Integer attendance_requirements, LocalDateTime start_date, LocalDateTime end_date, String region, String image_url,
                      LocalDateTime createdDate, LocalDateTime recruitEnd_date, LectureEntity.LectureStatus status) {
        this.create_id = create_id;
        this.user = user;
        this.userName = userName;
        this.creator = creator;
        this.max_participants = max_participants;
        this.current_participants = current_participants;
        this.category = category;
        this.bank_name = bank_name;
        this.account_name = account_name;
        this.account_number = account_number;
        this.price = price;
        this.title = title;
        this.content = content;
        //this.cycle = cycle;
        this.week = week;
        this.learning_target = learning_target;
        this.attendance_requirements = attendance_requirements;
        //this.count = count;
        this.start_date = start_date;
        this.end_date = end_date;
        this.region = region;
        this.status = status;
        this.image_url = image_url;
        this.createdDate = createdDate;
        this.recruitEnd_date = recruitEnd_date;
    }
}
