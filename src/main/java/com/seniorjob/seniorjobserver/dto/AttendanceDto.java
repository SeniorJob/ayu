package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.AttendanceEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto {
    private Long attendance_id;
    private Long create_id;
    private int required_attendance;
    private LocalDateTime createdDate;

    public AttendanceDto(AttendanceEntity attendanceEntity){
        this.attendance_id = attendanceEntity.getAttendance_id();
        this.create_id = attendanceEntity.getCreate_id().getCreate_id();
        this.required_attendance = attendanceEntity.getRequired_attendance();
        this.createdDate = attendanceEntity.getCreatedDate();
    }
}
