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
public class LectureDto {

    private Long create_id;
    private Long uid;
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
    private Integer max_participants;
    private Integer current_participants;
    private String region;
    private Integer price;
    private String bank_name;
    private String account_name;
    private String account_number;
    private LectureEntity.LectureStatus status;
    public LectureEntity.LectureStatus getStatus() {
        return status;
    }
    public void setStatus(LectureEntity.LectureStatus status) {
        this.status = status;
    }
    private LocalDateTime createdDate;

    public LectureEntity toEntity(UserEntity userEntity) {
        LectureEntity lectureEntity = LectureEntity.builder()
                .create_id(create_id)
                .creator(creator)
                .category(category)
                .image_url(image_url)
                .title(title)
                .content(content)
                .learningTarget(learning_target)
                .week(week)
                .recruitEnd_date(recruitEnd_date)
                .start_date(start_date)
                .end_date(end_date)
                .maxParticipants(max_participants)
                .currentParticipants(current_participants)
                .region(region)
                .price(price)
                .bank_name(bank_name)
                .account_name(account_name)
                .account_number(account_number)
                .createdDate(createdDate)
                .build();

        lectureEntity.setUser(userEntity);
        lectureEntity.updateStatus();
        this.status = lectureEntity.getStatus();

        return lectureEntity;
    }

    @Builder
    public LectureDto(Long create_id, Long uid, String userName, String creator, String category,
                      String image_url, String title,
                      String content, String learning_target, Integer week, LocalDateTime recruitEnd_date,
                      LocalDateTime start_date, LocalDateTime end_date, Integer max_participants,
                      Integer current_participants, String region, Integer price,
                      String bank_name, String account_name, String account_number,
                      LectureEntity.LectureStatus status, LocalDateTime createdDate) {
        this.create_id = create_id;
        this.uid = uid;
        this.userName = userName;
        this.creator = creator;
        this.category = category;
        this.image_url = image_url;
        this.title = title;
        this.content = content;
        this.learning_target = learning_target;
        this.week = week;
        this.recruitEnd_date = recruitEnd_date;
        this.start_date = start_date;
        this.end_date = end_date;
        this.max_participants = max_participants;
        this.current_participants = current_participants;
        this.region = region;
        this.price = price;
        this.bank_name = bank_name;
        this.account_name = account_name;
        this.account_number = account_number;
        this.status = status;
        this.createdDate = createdDate;
    }
}
