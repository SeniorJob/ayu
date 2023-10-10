package com.seniorjob.seniorjobserver.repository;

import com.seniorjob.seniorjobserver.domain.entity.CompletionLectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletionLectureRepository extends JpaRepository<CompletionLectureEntity, Long> {

}
