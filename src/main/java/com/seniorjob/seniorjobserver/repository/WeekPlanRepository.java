package com.seniorjob.seniorjobserver.repository;

import com.seniorjob.seniorjobserver.domain.entity.WeekEntity;
import com.seniorjob.seniorjobserver.domain.entity.WeekPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeekPlanRepository extends JpaRepository<WeekPlanEntity, Long> {

    @Query("SELECT wp FROM WeekPlanEntity wp WHERE wp.plan_id = :planId AND wp.week = :week")
    Optional<WeekPlanEntity> findPlanByIdAndWeek(@Param("planId") Long planId, @Param("week") WeekEntity week);

}

