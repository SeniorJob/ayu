package com.seniorjob.seniorjobserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExtendWeekPlanDto {
    private Long weekId;
    Long create_id;
    private String weekTitle;
    private List<WeekPlanDto> details;

    public ExtendWeekPlanDto(Long weekId, Long create_id, String weekTitle, List<WeekPlanDto> details){
        this.weekId = weekId;
        this.create_id = create_id;
        this.weekTitle = weekTitle;
        this.details = details;
    }
}
