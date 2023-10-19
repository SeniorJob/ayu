package com.seniorjob.seniorjobserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seniorjob.seniorjobserver.domain.entity.LectureApplyEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserDto {
    private Long uid;
    private String name;
    private String phoneNumber;
    private String encryptionCode;
    private String confirmPassword; // 비밀번호확인용 임시필드
    private String job;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private String category;
    private String region;
    private String imgKey;
    private LocalDateTime updateDate;
    private LocalDateTime createDate;

    public UserEntity toEntity(){
        UserEntity userEntity = UserEntity.builder()
                .uid(uid)
                .name(name)
                .phoneNumber(phoneNumber)
                .encryptionCode(encryptionCode)
                .job(job)
                .dateOfBirth(dateOfBirth)
                .category(category)
                .region(region)
                .imgKey(imgKey)
                .updateDate(updateDate)
                .createDate(createDate)
                .build();
        return userEntity;
    }

    @Builder
    public UserDto(Long uid, String name, String phoneNumber, String encryptionCode, String job,
                   LocalDate dateOfBirth, String category, String region, String imgKey,
                   LocalDateTime updateDate, LocalDateTime createDate) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.encryptionCode = encryptionCode;
        this.job = job;
        this.dateOfBirth = dateOfBirth;
        this.category = category;
        this.region = region;
        this.imgKey = imgKey;
        this.updateDate = updateDate;
        this.createDate = createDate;
    }
}
