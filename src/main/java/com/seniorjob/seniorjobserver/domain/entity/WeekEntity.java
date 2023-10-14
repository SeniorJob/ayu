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
@Table(name = "week")
public class WeekEntity extends TimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long week_id;

    @OneToMany(mappedBy = "week", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeekPlanEntity> plans = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "create_id")
    private LectureEntity create_id;
    private Integer week_number;
    private String week_title;

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Builder
    public WeekEntity(Long week_id, LectureEntity create_id, Integer week_number, String week_title){
        this.week_id = week_id;
        this.create_id = create_id;
        this.week_number = week_number;
        this.week_title = week_title;
    }
}
