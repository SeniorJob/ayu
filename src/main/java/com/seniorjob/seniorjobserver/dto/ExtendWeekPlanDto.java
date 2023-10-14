package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.WeekEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExtendWeekPlanDto {
    private Long weekId;
    private String weekTitle;
    private List<WeekPlanDto> details;

    public ExtendWeekPlanDto(Long weekId, String weekTitle, List<WeekPlanDto> details){
        this.weekId = weekId;
        this.weekTitle = weekTitle;
        this.details = details;
    }
}
