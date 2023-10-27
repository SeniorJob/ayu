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
public class FilterLectureDto {
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

    public static FilterLectureDto from(LectureDto lectureDto) {
        FilterLectureDto filterLectureDto = new FilterLectureDto();
        filterLectureDto.setCreate_id(lectureDto.getCreate_id());
        filterLectureDto.setUid(lectureDto.getUid());
        filterLectureDto.setCreator(lectureDto.getCreator());
        filterLectureDto.setPrice(lectureDto.getPrice());
        filterLectureDto.setCurrent_participants(lectureDto.getCurrent_participants());
        filterLectureDto.setImage_url(lectureDto.getImage_url());
        filterLectureDto.setTitle(lectureDto.getTitle());
        filterLectureDto.setCategory(lectureDto.getCategory());
        filterLectureDto.setRegion(lectureDto.getRegion());
        filterLectureDto.setStatus(lectureDto.getStatus());
        filterLectureDto.setCreatedDate(lectureDto.getCreatedDate());
        return filterLectureDto;
    }
}
