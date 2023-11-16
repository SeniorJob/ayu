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
@Table(name = "completion_lecture") // 개별 수료증목록으로 사용
public class CompletionLectureEntity extends TimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long completion_id;

    @ManyToOne
    @JoinColumn(name = "create_id")
    private LectureEntity create_id;

    private Integer attendance_requirements;

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Builder
    public CompletionLectureEntity(Long completion_id, LectureEntity create_id, Integer attendance_requirements){
        this.completion_id = completion_id;
        this.create_id = create_id;
        this.attendance_requirements = attendance_requirements;
    }
}
