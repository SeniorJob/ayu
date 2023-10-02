package com.seniorjob.seniorjobserver.repository;

import com.seniorjob.seniorjobserver.domain.entity.*;
import com.seniorjob.seniorjobserver.dto.LectureProposalApplyDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureProposalApplyRepository extends JpaRepository<LectureProposalApplyEntity, Long> {

    Optional<LectureProposalApplyEntity> findByUserAndLectureProposal(UserEntity user, LectureProposalEntity lectureProposal);

    List<LectureProposalApplyEntity> findByLectureProposal(LectureProposalEntity lectureProposal);

    List<LectureProposalApplyEntity> findByLectureProposalAndLectureProposalApplyStatus(LectureProposalEntity lectureProposal, LectureProposalApplyEntity.LectureProposalApplyStatus status);

    boolean existsByUserAndLectureProposal(UserEntity user, LectureProposalEntity lectureProposal);

    Optional<LectureProposalApplyEntity> findByLectureProposalAndRecruitmentClosed(LectureProposalEntity lectureProposal, boolean recruitmentClosed);

    void deleteByUser(UserEntity user);

    void deleteByLectureProposal(LectureProposalEntity proposal);
}