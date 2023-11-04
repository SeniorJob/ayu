package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.LectureApplyEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageLectureApplyDetailDto {
    private LectureApplyDto lectureApplyDto; // 강좌신청정보
    private LectureDto lectureInfo; // 강좌개설1단계 정보
    // user, status, recruitmentClosed 제외
    private List<WeekDto> weekDto; // 강좌개설 2단계 주차별 제목
    private List<WeekPlanDto> weekPlanDto; // 강좌개설 2단계 주차별 상세내용
    //private  AttendanceDto attendanceDto; // 강좌개설 2단계 수료조건 출석 회수 설정

    // 모집 마감까지 남은 일
    public String getDaysUntilRecruitEndMessage() {

        if (lectureInfo.getStatus() != LectureEntity.LectureStatus.신청가능상태){
            return null;
        }
        if(lectureInfo.getRecruitEnd_date() == null){
            return "모집 마감일이 설정되지 않았습니다.";
        }
        LocalDateTime now = LocalDateTime.now();
        int days = (int) ChronoUnit.DAYS.between(now, lectureInfo.getRecruitEnd_date());
        return "모집 마감까지 " + days + "일 남았습니다!";
    }
}
