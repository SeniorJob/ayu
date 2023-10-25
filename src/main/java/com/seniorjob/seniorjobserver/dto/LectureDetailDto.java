package com.seniorjob.seniorjobserver.dto;

<<<<<<< HEAD
=======

>>>>>>> develop
import lombok.*;

import java.util.List;

@Getter
@Setter
<<<<<<< HEAD
@ToString
@NoArgsConstructor
=======
@NoArgsConstructor
@AllArgsConstructor
>>>>>>> develop
public class LectureDetailDto {
    private LectureDto lectureDto;
    private List<WeekDto> weekDto;
    private List<WeekPlanDto> weekPlanDto;
    private AttendanceDto attendanceDto;

<<<<<<< HEAD
    public LectureDetailDto(LectureDto lectureDto, List<WeekDto> weekDto, List<WeekPlanDto> weekPlanDto, AttendanceDto attendanceDto) {
        this.lectureDto = lectureDto;
        this.weekDto = weekDto;
        this.weekPlanDto = weekPlanDto;
        this.attendanceDto = attendanceDto;
    }
}
=======
}
>>>>>>> develop
