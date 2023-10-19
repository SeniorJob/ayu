package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.WeekPlanEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeekPlanDto {
    private Long plan_id;
    private Long week_id;
    private String week_title;
    private Long create_id;
    private Integer detail_number;
    private String detail;
    private LocalDateTime createdDate;

    public WeekPlanDto(WeekPlanEntity weekPlanEntity){
        this.plan_id = weekPlanEntity.getPlan_id();
        this.week_id = weekPlanEntity.getWeek().getWeek_id();
        this.week_title = weekPlanEntity.getWeek().getWeek_title();
        this.create_id = weekPlanEntity.getCreate_id().getCreate_id();
        this.detail_number = weekPlanEntity.getDetail_number();
        this.detail = weekPlanEntity.getDetail();
        this.createdDate = weekPlanEntity.getCreatedDate();
    }
}
