package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.CompletionLectureEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompletionLectureDto {
    private Long completion_id;
    private Long create_id;
    private Integer attendance_requirements;
    private LocalDateTime createdDate;

    public CompletionLectureDto(CompletionLectureEntity completionLectureEntity){
        this.completion_id = completionLectureEntity.getCompletion_id();
        this.create_id = completionLectureEntity.getCreate_id().getCreate_id();
        this.attendance_requirements = completionLectureEntity.getAttendance_requirements();
        this.createdDate = completionLectureEntity.getCreatedDate();
    }
}
