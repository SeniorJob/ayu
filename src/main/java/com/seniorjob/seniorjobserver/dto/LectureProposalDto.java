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
    private LocalDateTime createDate;

    // 강좌제안 개설 및 수정 유효성검사
    public void validateFields(){
        if (category == null || category.trim().isEmpty()){
            throw new IllegalArgumentException("카테고리를 입력해주세요!");
        }
        if (title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("제목을 입력해주세요!");
        }
        if(content == null || content.trim().isEmpty()){
            throw new IllegalArgumentException("간단한 소개를 작성해주세요!");
        }
        if(region == null || region.trim().isEmpty()){
            throw new IllegalArgumentException("지역을 입력해주세요!");
        }
    }

    public LectureProposalEntity toEntity() {
        return LectureProposalEntity.builder()
                .proposal_id(proposalId)
                .user(user)
                .title(title)
                .category(category)
                .region(region)
                .content(content)
                .created_date(createDate)
                .build();
    }

    public LectureProposalDto(LectureProposalEntity lectureProposal) {
        this.proposalId = lectureProposal.getProposal_id();
        this.userName = lectureProposal.getUser().getName();
        this.title = lectureProposal.getTitle();
        this.category = lectureProposal.getCategory();
        this.region = lectureProposal.getRegion();
        this.content = lectureProposal.getContent();
        this.createDate = lectureProposal.getCreated_date();
    }

    @Builder
    public LectureProposalDto(Long proposalId, UserEntity user, String userName, String title,
                              String category, String region, String content, LocalDateTime createDate){
        this.proposalId = proposalId;
        this.user = user;
        this.userName = userName;
        this.title = title;
        this.category = category;
        this.region = region;
        this.content = content;
        this.createDate = createDate;
    }

    public static LectureProposalDto convertToDto(LectureProposalEntity lectureProposalEntity) {
        if (lectureProposalEntity == null)
            return null;
        return LectureProposalDto.builder()
                .proposalId(lectureProposalEntity.getProposal_id())
                .user(lectureProposalEntity.getUser())
                .userName(lectureProposalEntity.getUser().getName())
                .title(lectureProposalEntity.getTitle())
                .category(lectureProposalEntity.getCategory())
                .region(lectureProposalEntity.getRegion())
                .content(lectureProposalEntity.getContent())
                .createDate(lectureProposalEntity.getCreated_date())
                .build();
    }
}
