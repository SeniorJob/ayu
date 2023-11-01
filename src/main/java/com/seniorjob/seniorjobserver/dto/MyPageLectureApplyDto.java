package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MyPageLectureApplyDto {
    private Long create_id; // 강좌ID
    private Long le_id; // 강좌신청ID
    private Long uid; // 유저ID
    private String creator;
    private String category;
    private String title;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime recruitEnd_date;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime start_date;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime end_date;

    // 모집 마감까지 남은 일
    public String getDaysUntilRecruitEndMessage() {

        if (status != LectureEntity.LectureStatus.신청가능상태){
            return null;
        }
        if(recruitEnd_date == null){
            return "모집 마감일이 설정되지 않았습니다.";
        }
        LocalDateTime now = LocalDateTime.now();
        int days = (int) ChronoUnit.DAYS.between(now, recruitEnd_date);
        return "모집 마감까지 " + days + "일 남았습니다!";
    }

    private Integer max_participants;
    private Integer current_participants;
    private String region;
    private Integer price;
    private LectureEntity.LectureStatus status;
    private LocalDateTime createdDate;


}
