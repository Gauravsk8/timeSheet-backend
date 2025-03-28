package com.example.timesheet.models;

import com.example.timesheet.enums.TaskType;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class TimeSheetEntry extends BaseEntity{

    @ManyToOne
    @JoinColumn(name="daily_time_sheet_id",nullable = false)
    private DailyTimeSheet dailyTimeSheet;

    private TaskType taskType;

    @OneToOne
    @JoinColumn(name="project_id",nullable = false)
    private Project project;

    private String taskDescription;


    @Column(nullable = false)
    private Long hoursSpent;

    @OneToOne
    @JoinColumn(name="leave_id")
    private Leave leave;
}
