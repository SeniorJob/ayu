package com.seniorjob.seniorjobserver.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lecture")
public class LectureEntity extends TimeEntity {

    public enum LectureStatus {
        신청가능상태,
        개설대기상태,
        진행상태,
        철회상태,
        완료상태
    }

    private Boolean recruitmentClosed;

    // 강좌의 모집 마감 여부를 반환하는 메서드
    public boolean isRecruitmentClosed() {
        return Boolean.TRUE.equals(this.recruitmentClosed);
    }

    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 날짜 null 확인 예외처리
        if (recruitEnd_date == null || start_date == null || end_date == null) {
            throw new IllegalArgumentException("강좌 날짜 필드 중 하나 이상이 설정되지 않았습니다!!");
        }

        // 완료상태:
        if (now.isAfter(end_date)) {
            status = LectureStatus.완료상태;
            return;
        }

        // 진행상태:
        if (isRecruitmentClosed() && now.isAfter(start_date) && now.isBefore(end_date)) {
            status = LectureStatus.진행상태;
            return;
        }

        // 신청가능상태:
        if (now.isBefore(recruitEnd_date) && start_date.isAfter(recruitEnd_date)
                && end_date.isAfter(start_date)
                && (currentParticipants == null || currentParticipants < maxParticipants)) {
            status = LectureStatus.신청가능상태;
            return;
        }

        // 철회상태:
        if (now.isAfter(recruitEnd_date) && now.isBefore(start_date) && currentParticipants < maxParticipants && !isRecruitmentClosed()) {
            status = LectureStatus.철회상태;
            return;
        }

        // 개설대기상태:
        if (now.isAfter(recruitEnd_date) || isRecruitmentClosed()) {
            status = LectureStatus.개설대기상태;
            return;
        }

        throw new IllegalStateException("강좌개설오류");
    }


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long create_id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private UserEntity user;

    @Column(name = "creator", nullable = false)
    private String creator;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "current_participants")
    private Integer currentParticipants;

    @Column(name = "category")
    private String category;

    @Column(name = "bank_name")
    private String bank_name;

    @Column(name = "account_name")
    private String account_name;

    @Column(name = "account_number")
    private String account_number;

    @Column(name = "price")
    private Integer price;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "cycle")
    private String cycle;

    @Column(name = "count")
    private Integer count;

    @Column(name = "start_date", columnDefinition = "datetime")
    private LocalDateTime start_date;

    @Column(name = "end_date", columnDefinition = "datetime")
    private LocalDateTime end_date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private LectureStatus status = LectureStatus.신청가능상태;

    @Column(name = "region")
    private String region;

    @Column(name = "image_url")
    private String image_url;

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "recruitEnd_date", columnDefinition = "datetime")
    private LocalDateTime recruitEnd_date;

    @Builder
    public LectureEntity(Long create_id, UserEntity user, String creator, Integer maxParticipants, Integer currentParticipants, String category,
                         String bank_name, String account_name, String account_number, Integer price, String title, String content,
                         String cycle, Integer count, LocalDateTime start_date, LocalDateTime end_date, String region, String image_url,
                         LocalDateTime createdDate, LocalDateTime recruitEnd_date) {
        this.create_id = create_id;
        this.user = user;
        this.creator = creator;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
        this.category = category;
        this.bank_name = bank_name;
        this.account_name = account_name;
        this.account_number = account_number;
        this.price = price;
        this.title = title;
        this.content = content;
        this.cycle = cycle;
        this.count = count;
        this.start_date = start_date;
        this.end_date = end_date;
        this.region = region;
        this.status = LectureStatus.신청가능상태;
        this.image_url = image_url;
        this.createdDate = createdDate;
        this.recruitEnd_date = recruitEnd_date;
    }

    public void increaseCurrentParticipants() {
        if (currentParticipants == null) {
            currentParticipants = 0;
        }
        currentParticipants++;
    }
    public void decreaseCurrentParticipants() {
        if (currentParticipants != null && currentParticipants > 0) {
            currentParticipants--;
        }
    }
}
