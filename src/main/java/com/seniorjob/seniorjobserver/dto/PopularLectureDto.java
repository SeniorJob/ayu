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
public class PopularLectureDto {
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

    public static PopularLectureDto from(LectureDto lectureDto) {
        PopularLectureDto popularLectureDto = new PopularLectureDto();
        popularLectureDto.setCreate_id(lectureDto.getCreate_id());
        popularLectureDto.setUid(lectureDto.getUid());
        popularLectureDto.setCreator(lectureDto.getCreator());
        popularLectureDto.setPrice(lectureDto.getPrice());
        popularLectureDto.setCurrent_participants(lectureDto.getCurrent_participants());
        popularLectureDto.setImage_url(lectureDto.getImage_url());
        popularLectureDto.setTitle(lectureDto.getTitle());
        popularLectureDto.setCategory(lectureDto.getCategory());
        popularLectureDto.setRegion(lectureDto.getRegion());
        popularLectureDto.setStatus(lectureDto.getStatus());
        popularLectureDto.setCreatedDate(lectureDto.getCreatedDate());
        return popularLectureDto;
    }
}
