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
    private Long uid;
    private Long week_id;
    private String attendance_status;
    private LocalDateTime date;
    private LocalDateTime createdDate;

    public AttendanceDto(AttendanceEntity attendanceEntity){
        this.attendance_id = attendanceEntity.getAttendance_id();
        this.create_id = attendanceEntity.getCreate_id().getCreate_id();
        this.uid = attendanceEntity.getUid().getUid();
        this.week_id = attendanceEntity.getWeek_id().getWeek_id();
        this.attendance_status = attendanceEntity.getAttendance_status();
        this.date = attendanceEntity.getDate();
        this.createdDate = attendanceEntity.getCreatedDate();
    }
}
