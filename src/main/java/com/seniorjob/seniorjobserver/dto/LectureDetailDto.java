package com.seniorjob.seniorjobserver.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LectureDetailDto {
    private LectureDto lectureDto;
    private List<WeekDto> weekDto;
    private List<WeekPlanDto> weekPlanDto;
    private AttendanceDto attendanceDto;

    public LectureDetailDto(LectureDto lectureDto, List<WeekDto> weekDto, List<WeekPlanDto> weekPlanDto, AttendanceDto attendanceDto) {
        this.lectureDto = lectureDto;
        this.weekDto = weekDto;
        this.weekPlanDto = weekPlanDto;
        this.attendanceDto = attendanceDto;
    }
}