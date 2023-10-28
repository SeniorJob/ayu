package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RecommendLectureDto {
    private Long create_id;
    private Long uid;
    private String creator;
    private String image_url;
    private String title;
    private String category;
    private Integer price;
    private Integer current_participants;
    private String region;
    private LectureEntity.LectureStatus status;
    private LocalDateTime createdDate;

    public static RecommendLectureDto from(LectureEntity lectureEntity) {
        RecommendLectureDto recommendLectureDto = new RecommendLectureDto();
        recommendLectureDto.setCreate_id(lectureEntity.getCreate_id());
        recommendLectureDto.setUid(lectureEntity.getUser().getUid());
        recommendLectureDto.setCreator(lectureEntity.getCreator());
        recommendLectureDto.setPrice(lectureEntity.getPrice());
        recommendLectureDto.setCurrent_participants(lectureEntity.getCurrentParticipants());
        recommendLectureDto.setImage_url(lectureEntity.getImage_url());
        recommendLectureDto.setTitle(lectureEntity.getTitle());
        recommendLectureDto.setCategory(lectureEntity.getCategory());
        recommendLectureDto.setRegion(lectureEntity.getRegion());
        recommendLectureDto.setStatus(lectureEntity.getStatus());
        recommendLectureDto.setCreatedDate(lectureEntity.getCreatedDate());
        return recommendLectureDto;
    }
}
