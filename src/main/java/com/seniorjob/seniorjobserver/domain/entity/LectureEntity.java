package com.seniorjob.seniorjobserver.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lecture")
public class LectureEntity extends TimeEntity {

    public enum LectureStatus {
        WAITING,
        RECRUITING,
        CLOSED,
        NORMAL_STATUS
    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long create_id;

    @Column(name = "creator", nullable = false)
    private String creator;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "current_participants")
    private Integer current_participants; // 수정된 부분

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

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LectureStatus status;

    public void updateStatus() {
        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.isBefore(start_date)) {
            status = LectureStatus.WAITING;
        } else if (currentDate.isBefore(end_date)) {
            status = LectureStatus.RECRUITING;
        } else {
            status = LectureStatus.CLOSED;
        }
    }

    @Column(name = "region")
    private String region;

    @Column(name = "image_url")
    private String image_url;

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;


    @Builder
    public LectureEntity(Long create_id, String creator, Integer maxParticipants, Integer current_participants, String category,
                         String bank_name, String account_name, String account_number, Integer price, String title, String content,
                         String cycle, Integer count, LocalDateTime start_date, LocalDateTime end_date, String region, String image_url,
                         LocalDateTime createdDate) {
        this.create_id = create_id;
        this.creator = creator;
        this.maxParticipants = maxParticipants;
        this.current_participants = current_participants;
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
        this.image_url = image_url;
        this.createdDate = createdDate;
    }
}
