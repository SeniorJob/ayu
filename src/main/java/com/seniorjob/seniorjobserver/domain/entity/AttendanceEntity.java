package com.seniorjob.seniorjobserver.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "required_attendance", nullable = false)
    private int required_attendance;

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    public void setRequiredAttendance(int requiredAttendance) {
        if (requiredAttendance <= 0 || requiredAttendance >= 5) {
            throw new IllegalArgumentException("수료에 필요한 출석 회수를 1회이상 5회 이하로 설정해 주세요");
        }
        this.required_attendance = requiredAttendance;
    }

    @Builder
    public AttendanceEntity(Long attendance_id, LectureEntity create_id, int required_attendance){
        this.attendance_id = attendance_id;
        this.create_id = create_id;
        this.required_attendance = required_attendance;
    }
}
