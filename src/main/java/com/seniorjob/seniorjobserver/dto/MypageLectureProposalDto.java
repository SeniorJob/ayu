package com.seniorjob.seniorjobserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import lombok.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MypageLectureProposalDto {


    private Long proposalId;
    @JsonIgnore
    private UserEntity user;
    private String category;
    private String title;
    private String content;
    private String region;
    private LocalDateTime createdDate;

    @Builder
    public MypageLectureProposalDto(LectureProposalEntity lectureProposalEntity){
        this.proposalId = lectureProposalEntity.getProposal_id();
        this.user = lectureProposalEntity.getUser();
        this.category = lectureProposalEntity.getCategory();
        this.title = lectureProposalEntity.getTitle();
        this.content = lectureProposalEntity.getContent();
        this.region = lectureProposalEntity.getRegion();
        this.createdDate = lectureProposalEntity.getCreated_date();
    }

}
