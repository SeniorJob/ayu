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
public class FilterLectureDto {

    private Long create_id;
    private Long uid;
    private String userName;
    private String creator;
    private String category;
    private String image_url;
    private String title;
    private String content;
    private String learning_target;
    private Integer week;
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
    private String bank_name;
    private String account_name;
    private String account_number;
    private LectureEntity.LectureStatus status;
    private LocalDateTime createdDate;

    public static FilterLectureDto from(LectureDto lectureDto) {
        FilterLectureDto filterLectureDto = new FilterLectureDto();
        filterLectureDto.setCreate_id(lectureDto.getCreate_id());
        filterLectureDto.setUid(lectureDto.getUid());
        filterLectureDto.setUserName(lectureDto.getUserName());
        filterLectureDto.setCreator(lectureDto.getCreator());
        filterLectureDto.setCategory(lectureDto.getCategory());
        filterLectureDto.setImage_url(lectureDto.getImage_url());
        filterLectureDto.setTitle(lectureDto.getTitle());
        filterLectureDto.setContent(lectureDto.getContent());
        filterLectureDto.setLearning_target(lectureDto.getLearning_target());
        filterLectureDto.setWeek(lectureDto.getWeek());
        filterLectureDto.setRecruitEnd_date(lectureDto.getRecruitEnd_date());
        filterLectureDto.setStart_date(lectureDto.getStart_date());
        filterLectureDto.setEnd_date(lectureDto.getEnd_date());
        filterLectureDto.setMax_participants(lectureDto.getMax_participants());
        filterLectureDto.setCurrent_participants(lectureDto.getCurrent_participants());
        filterLectureDto.setPrice(lectureDto.getPrice());
        filterLectureDto.setRegion(lectureDto.getRegion());
        filterLectureDto.setBank_name(lectureDto.getBank_name());
        filterLectureDto.setAccount_name(lectureDto.getAccount_name());
        filterLectureDto.setAccount_number(lectureDto.getAccount_number());
        filterLectureDto.setStatus(lectureDto.getStatus());
        filterLectureDto.setCreatedDate(lectureDto.getCreatedDate());
        return filterLectureDto;
    }
}
