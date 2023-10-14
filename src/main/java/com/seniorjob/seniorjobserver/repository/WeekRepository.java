package com.seniorjob.seniorjobserver.repository;

import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.WeekEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeekRepository extends JpaRepository<WeekEntity, Long> {

    @Query("SELECT w FROM WeekEntity w WHERE w.create_id = :create_id ORDER BY w.week_number ASC")
    List<WeekEntity> findByCreate_idOrderByWeekNumberAsc(@Param("create_id") LectureEntity create_id);
}

