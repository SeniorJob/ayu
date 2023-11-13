package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.LectureApplyEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MyApplyDto {
    private Long create_id; // 강좌ID
    private Long le_id; // 강좌신청ID
    private Long uid; // 유저ID
    private String userName;

    @Builder
    public MyApplyDto(LectureApplyEntity lectureApply, LectureEntity lectureEntity) {
        this.create_id = lectureEntity.getCreate_id();
        this.uid = lectureApply.getUser().getUid();
        this.le_id = lectureApply.getLeId();
        this.userName = lectureEntity.getUser().getName();
    }
}
