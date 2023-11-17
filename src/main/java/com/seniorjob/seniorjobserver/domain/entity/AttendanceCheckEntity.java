package com.seniorjob.seniorjobserver.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "attendance_check")
public class AttendanceCheckEntity extends TimeEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long attendance_check_id; // 출석체크 id

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "create_id", referencedColumnName = "create_id")
    private LectureEntity create_id;  // lecture테이블 create_id 강좌id

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uid", referencedColumnName = "uid")
    private UserEntity uid; // user테이블 uid 회원id

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leId", referencedColumnName = "leId")
    private LectureApplyEntity leId;  // lectureapply테이블 le_id 강좌신청id

    public enum Attendance_status {
        출석, 미등록 ,결석
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status",nullable = false)
    private AttendanceCheckEntity.Attendance_status attendance_status = AttendanceCheckEntity.Attendance_status.미등록;

    @Column(name = "created_date", columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Builder
    public AttendanceCheckEntity(Long attendance_check_id, LectureEntity create_id,
                                 UserEntity uid, LectureApplyEntity leId, Attendance_status attendance_status,
                                 LocalDateTime createdDate) {
        this.attendance_check_id = attendance_check_id;
        this.create_id = create_id;
        this.uid = uid;
        this.leId = leId;
        this.attendance_status = (attendance_status == null) ? Attendance_status.미등록 : attendance_status;
        this.createdDate = createdDate;
    }
}
