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
@Table(name = "week_plan")
public class WeekPlanEntity extends TimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plan_id;

    @ManyToOne
    @JoinColumn(name = "create_id")
    private LectureEntity create_id;

    @ManyToOne
    @JoinColumn(name = "week_id")
    private WeekEntity week;

    private Integer detail_number;

    @Column(columnDefinition = "text")
    private String detail;

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Builder
    public WeekPlanEntity(Long plan_id, WeekEntity week, Integer detail_number, String detail, LectureEntity create_id){
        this.plan_id = plan_id;
        this.week = week;
        this.detail_number = detail_number;
        this.detail = detail;
        this.create_id = create_id;
    }
}
