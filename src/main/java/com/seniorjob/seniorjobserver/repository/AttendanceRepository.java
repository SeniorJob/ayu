package com.seniorjob.seniorjobserver.repository;

import com.seniorjob.seniorjobserver.domain.entity.AttendanceEntity;
import com.seniorjob.seniorjobserver.domain.entity.LectureEntity;
import com.seniorjob.seniorjobserver.domain.entity.WeekEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    @Query("SELECT a FROM AttendanceEntity a WHERE a.create_id = :create_id")
    Optional<AttendanceEntity> findByCreate_id(@Param("create_id") LectureEntity create_id);
    @Query("SELECT a FROM AttendanceEntity a WHERE a.create_id.create_id = :createId")
    List<AttendanceEntity> findByCreateId(@Param("createId") Long createId);
    @Modifying
    @Query("DELETE FROM AttendanceEntity w WHERE w.create_id = :lecture")
    void deleteByLecture(@Param("lecture") LectureEntity lecture);
}
