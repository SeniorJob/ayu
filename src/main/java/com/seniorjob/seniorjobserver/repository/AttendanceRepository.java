package com.seniorjob.seniorjobserver.repository;

import com.seniorjob.seniorjobserver.domain.entity.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
}
