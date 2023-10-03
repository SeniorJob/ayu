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
    private String encryptionCode;
    private String confirmPassword; // 비밀번호확인용 임시필드
    private String name;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private UserEntity.Gender gender;
    private String phoneNumber;
    private UserEntity.UserType userType;
    private UserEntity.LoginType loginType;
    private String job;
    private String region;
    private String imgKey;
    private String category;
    private LocalDateTime updateDate;
    private LocalDateTime createDate;

    public UserEntity toEntity(){
        UserEntity userEntity = UserEntity.builder()
                .uid(uid)
                .encryptionCode(encryptionCode)
                .name(name)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .loginType(loginType)
                .userType(userType)
                .phoneNumber(phoneNumber)
                .job(job)
                .region(region)
                .imgKey(imgKey)
                .category(category)
                .updateDate(updateDate)
                .createDate(createDate)
                .build();
        return userEntity;
    }

    @Builder
    public UserDto(Long uid, String encryptionCode, String name, LocalDate dateOfBirth, UserEntity.Gender gender, String phoneNumber,
                      UserEntity.LoginType loginType, UserEntity.UserType userType, String job, String region, String imgKey, String category,
                      LocalDateTime updateDate, LocalDateTime createDate) {
        this.uid = uid;
        this.encryptionCode = encryptionCode;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.loginType = loginType;
        this.userType = userType;
        this.phoneNumber = phoneNumber;
        this.job = job;
        this.region = region;
        this.imgKey = imgKey;
        this.category = category;
        this.updateDate = updateDate;
        this.createDate = createDate;
    }
}
