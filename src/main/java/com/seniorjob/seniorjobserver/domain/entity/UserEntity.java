package com.seniorjob.seniorjobserver.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.usertype.UserType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Entity
@Table(name = "user") // 가입된 구인자, 강사 회원정보
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;
    @Column(name = "encryption_code", nullable = false)
    private String encryptionCode;
    @Column(name = "job", nullable = false)
    private String job;
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    @Column(name = "category")
    private String category;
    @Column(name = "region")
    private String region;
    @Column(name = "img_key", nullable = true)
    private String imgKey;

    @CreationTimestamp
    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;
    @LastModifiedDate
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Builder
    public UserEntity(Long uid, String name, String phoneNumber, String encryptionCode, String job, LocalDate dateOfBirth,
                       String category, String region, String imgKey, LocalDateTime updateDate, LocalDateTime createDate) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.encryptionCode = encryptionCode;
        this.job = job;
        this.dateOfBirth = dateOfBirth;
        this.category = category;
        this.region = region;
        this.imgKey = imgKey;
        //this.updateDate = updateDate;
        //this.createDate = createDate;
    }
}
