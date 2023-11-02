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
public class RecommendLectureDto {
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
    public LectureEntity.LectureStatus getStatus() {
        return status;
    }
    public void setStatus(LectureEntity.LectureStatus status) {
        this.status = status;
    }
    private LocalDateTime createdDate;

    public static RecommendLectureDto from(LectureEntity lectureEntity) {
        RecommendLectureDto recommendLectureDto = new RecommendLectureDto();
        recommendLectureDto.setCreate_id(lectureEntity.getCreate_id());
        recommendLectureDto.setUid(lectureEntity.getUser().getUid());
        recommendLectureDto.setUserName(lectureEntity.getUser().getName());
        recommendLectureDto.setCreator(lectureEntity.getCreator());
        recommendLectureDto.setCategory(lectureEntity.getCategory());
        recommendLectureDto.setImage_url(lectureEntity.getImage_url());
        recommendLectureDto.setTitle(lectureEntity.getTitle());
        recommendLectureDto.setContent(lectureEntity.getContent());
        recommendLectureDto.setLearning_target(lectureEntity.getLearningTarget());
        recommendLectureDto.setWeek(lectureEntity.getWeek());
        recommendLectureDto.setRecruitEnd_date(lectureEntity.getRecruitEnd_date());
        recommendLectureDto.setStart_date(lectureEntity.getStart_date());
        recommendLectureDto.setEnd_date(lectureEntity.getEnd_date());
        recommendLectureDto.setMax_participants(lectureEntity.getMaxParticipants());
        recommendLectureDto.setCurrent_participants(lectureEntity.getCurrentParticipants());
        recommendLectureDto.setRegion(lectureEntity.getRegion());
        recommendLectureDto.setPrice(lectureEntity.getPrice());
        recommendLectureDto.setBank_name(lectureEntity.getBank_name());
        recommendLectureDto.setAccount_name(lectureEntity.getAccount_name());
        recommendLectureDto.setAccount_number(lectureEntity.getAccount_number());
        recommendLectureDto.setStatus(lectureEntity.getStatus());
        recommendLectureDto.setCreatedDate(lectureEntity.getCreatedDate());
        return recommendLectureDto;
    }
}
