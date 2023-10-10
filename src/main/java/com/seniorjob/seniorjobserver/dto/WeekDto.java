package com.seniorjob.seniorjobserver.dto;

import com.seniorjob.seniorjobserver.domain.entity.WeekEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeekDto {
    private Long week_id;
    private Long create_id;
    private Integer week_number;
    private String week_title;
    private LocalDateTime createdDate;

    public WeekDto(WeekEntity weekEntity){
        this.week_id = weekEntity.getWeek_id();
        this.create_id = weekEntity.getCreate_id().getCreate_id();
        this.week_number = weekEntity.getWeek_number();
        this.week_title = weekEntity.getWeek_title();
        this.createdDate = weekEntity.getCreatedDate();
    }
}
