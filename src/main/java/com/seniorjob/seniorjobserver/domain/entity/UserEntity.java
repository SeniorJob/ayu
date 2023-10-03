package com.seniorjob.seniorjobserver.domain.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user") // 가입된 구인자, 강사 회원정보
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(name = "encryption_code", nullable = false)
    private String encryptionCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private UserEntity.Gender gender = Gender.기타;

    public enum Gender{
        여성, 남성, 기타
    }

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserEntity.UserType userType = UserType.개인;

    public enum UserType{
        사업자, 개인
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private UserEntity.LoginType loginType = LoginType.쇼셜;

    public enum LoginType{
        쇼셜, 카카오
    }

    @Column(name = "job", nullable = false)
    private String job;

    @Column(name = "region")
    private String region;

    @Column(name = "img_key")
    private String imgKey;

    @Column(name = "category")
    private String category;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "create_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    private LocalDateTime createDate;

    @Builder
    public UserEntity(Long uid, String encryptionCode, String name, LocalDate dateOfBirth, Gender gender, String phoneNumber,
                      LoginType loginType, UserType userType, String job, String region, String imgKey, String category,
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
