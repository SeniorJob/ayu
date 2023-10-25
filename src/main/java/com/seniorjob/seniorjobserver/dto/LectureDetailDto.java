package com.seniorjob.seniorjobserver.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureDetailDto {
    private LectureDto lectureDto;
    private List<WeekDto> weekDto;
    private List<WeekPlanDto> weekPlanDto;
    private AttendanceDto attendanceDto;

}