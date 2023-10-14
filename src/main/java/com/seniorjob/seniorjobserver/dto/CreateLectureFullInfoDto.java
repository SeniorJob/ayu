package com.seniorjob.seniorjobserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLectureFullInfoDto {
    private LectureDto lectureInfo; // 강좌개설1단계 정보
    private List<WeekDto> weekDto; // 강좌개설 2단계 주차별 제목
    private List<WeekPlanDto> weekPlanDto; // 강좌개설 2단계 주차별 상세내용
   // 강좌개설 2단계 수료조건 출석 회수 설정

}
