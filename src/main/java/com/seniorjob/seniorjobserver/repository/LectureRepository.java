package com.seniorjob.seniorjobserver.repository;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<LectureEntity, Long> {

    List<LectureEntity> findByTitleContaining(String title);

    Page<LectureEntity> findAll(Pageable pageable);

    List<LectureEntity> findAllByUser(UserEntity user);

    List<LectureEntity> findByUser(UserEntity user);
    List<LectureEntity> findByStatusOrderByCurrentParticipantsDesc(LectureEntity.LectureStatus status, Pageable pageable);
    List<LectureEntity> findByCategoryAndStatusAndCreatorNotOrderByCurrentParticipantsDesc(String category, LectureEntity.LectureStatus status, String creator, Pageable pageable);
}


