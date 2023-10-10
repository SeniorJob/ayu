package com.seniorjob.seniorjobserver.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "attendance")
public class AttendanceEntity extends TimeEntity{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long attendance_id;

    @ManyToOne
    @JoinColumn(name = "create_id")
    private LectureEntity create_id;

    @ManyToOne
    @JoinColumn(name = "uid")
    private UserEntity uid;

    @ManyToOne
    @JoinColumn(name = "week_id")
    private WeekEntity week_id;

    @Column(name = "attendance_status")
    private String attendance_status;

    @Column(name = "date", columnDefinition = "datetime")
    private LocalDateTime date;

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Builder
    public AttendanceEntity(Long attendance_id, LectureEntity create_id, UserEntity uid, WeekEntity week_id, String attendance_status, LocalDateTime date){
        this.attendance_id = attendance_id;
        this.create_id = create_id;
        this.uid = uid;
        this.week_id = week_id;
        this.attendance_status = attendance_status;
        this.date = date;
    }
}
