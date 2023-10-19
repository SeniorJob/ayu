package com.seniorjob.seniorjobserver.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "Lectureproposal")
public class LectureProposalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long proposal_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private UserEntity user;

    @Column(name = "category")
    private String category;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "region")
    private String region;

    @Column(name = "start_date", columnDefinition = "datetime")
    private LocalDateTime start_date;

    @Column(name = "end_date", columnDefinition = "datetime")
    private LocalDateTime end_date;

    @Column(name = "current_participants")
    private Integer current_participants;

    private Boolean recruitmentClosed;

    // 강좌제안의 모집 마감 여부를 반환하는 메서드
    public boolean isRecruitmentClosed() {
        return Boolean.TRUE.equals(this.recruitmentClosed);
    }

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime created_date;

    @Builder
    public LectureProposalEntity(Long proposal_id, UserEntity user, String title, String category,
                                 String region, LocalDateTime start_date, LocalDateTime end_date,
                                 String content, Integer currentParticipants, LocalDateTime created_date) {
        this.proposal_id = proposal_id;
        this.user = user;
        this.title = title;
        this.category = category;
        this.start_date = start_date;
        this.end_date = end_date;
        this.region = region;
        this.content = content;
        this.current_participants = currentParticipants;
        this.created_date = created_date;
    }

    public void increaseCurrentParticipants() {
        if (current_participants == null) {
            current_participants = 0;
        }
        current_participants++;
    }
    public void decreaseCurrent_participants() {
        if (current_participants != null && current_participants > 0) {
            current_participants--;
        }
    }
}
