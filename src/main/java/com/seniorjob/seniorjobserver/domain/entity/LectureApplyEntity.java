package com.seniorjob.seniorjobserver.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "lectureapply")
public class LectureApplyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "create_id", referencedColumnName = "create_id")
    private LectureEntity lecture;

    @Column(name = "applyReason")
    private String applyReason;

    public enum LectureApplyStatus {
        승인,
        대기
    }

    @Column(name = "lectureapply_status")
    @Enumerated(EnumType.STRING)
    private LectureApplyStatus lectureApplyStatus = LectureApplyStatus.승인;

    public LectureApplyStatus getLectureApplyStatus(){
        return lectureApplyStatus;
    }

    public void setLectureApplyStatus(LectureApplyStatus lectureApplyStatus){
        this.lectureApplyStatus = lectureApplyStatus;
    }

    @Column(name = "recruitment_closed")
    private Boolean recruitmentClosed;

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Builder
    public LectureApplyEntity(Long leId, LectureEntity lecture, UserEntity user, String applyReason,
                              LocalDateTime createdDate) {
        this.leId = leId;
        this.lecture = lecture;
        this.user = user;
        this.applyReason = applyReason;
        this.createdDate = createdDate;
    }
}
