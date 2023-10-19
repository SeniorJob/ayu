package com.seniorjob.seniorjobserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seniorjob.seniorjobserver.domain.entity.LectureApplyEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LectureProposalDto {

    private Long proposalId;
    @JsonIgnore
    private UserEntity user;
    private String userName;
    private String category;
    private String title;
    private String content;
    private String region;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer currentParticipants;
    private LocalDateTime createDate;

    public LectureProposalEntity toEntity() {
        return LectureProposalEntity.builder()
                .proposal_id(proposalId)
                .user(user)
                .title(title)
                .category(category)
                .start_date(startDate)
                .end_date(endDate)
                .region(region)
                .content(content)
                .currentParticipants(currentParticipants)
                .created_date(createDate)
                .build();
    }

    public LectureProposalDto(LectureProposalEntity lectureProposal) {
        this.proposalId = lectureProposal.getProposal_id();
        this.userName = lectureProposal.getUser().getName();
        this.title = lectureProposal.getTitle();
        this.category = lectureProposal.getCategory();
        this.startDate = lectureProposal.getStart_date();
        this.endDate = lectureProposal.getEnd_date();
        this.region = lectureProposal.getRegion();
        this.content = lectureProposal.getContent();
        this.currentParticipants = lectureProposal.getCurrent_participants();
        this.createDate = lectureProposal.getCreated_date();
    }

    @Builder
    public LectureProposalDto(Long proposalId, UserEntity user, String userName, String title,
                              String category, LocalDateTime startDate, LocalDateTime endDate, String region,
                              String content, Integer currentParticipants, LocalDateTime createDate){
        this.proposalId = proposalId;
        this.user = user;
        this.userName = userName;
        this.title = title;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.region = region;
        this.content = content;
        this.currentParticipants = currentParticipants;
        this.createDate = createDate;
    }

    public static LectureProposalDto convertToDto(LectureProposalEntity lectureProposalEntity) {
        return LectureProposalDto.builder()
                .proposalId(lectureProposalEntity.getProposal_id())
                .user(lectureProposalEntity.getUser())
                .userName(lectureProposalEntity.getUser().getName())
                .title(lectureProposalEntity.getTitle())
                .category(lectureProposalEntity.getCategory())
                .startDate(lectureProposalEntity.getStart_date())
                .endDate(lectureProposalEntity.getEnd_date())
                .region(lectureProposalEntity.getRegion())
                .content(lectureProposalEntity.getContent())
                .currentParticipants(lectureProposalEntity.getCurrent_participants())
                .createDate(lectureProposalEntity.getCreated_date())
                .build();
    }
}
