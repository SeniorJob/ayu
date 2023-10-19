package com.seniorjob.seniorjobserver.repository;

import com.seniorjob.seniorjobserver.domain.entity.AttendanceEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    @Query("SELECT a FROM AttendanceEntity a WHERE a.create_id = :create_id")
    Optional<AttendanceEntity> findByCreate_id(@Param("create_id") LectureEntity create_id);

}
