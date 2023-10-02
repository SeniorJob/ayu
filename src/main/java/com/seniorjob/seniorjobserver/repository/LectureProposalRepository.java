package com.seniorjob.seniorjobserver.repository;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureProposalEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureProposalRepository extends JpaRepository<LectureProposalEntity, Long>{

    List<LectureProposalEntity> findByTitleContaining(String title);

    Page<LectureProposalEntity> findAll(Pageable pageable);

    List<LectureProposalEntity> findAllByUser(UserEntity user);

    List<LectureProposalEntity> findByUser(UserEntity user);
}
