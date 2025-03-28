package com.example.timesheet.models;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DailyTimeSheet extends BaseEntity{

    @OneToOne
    @JoinColumn(name="employeeId",nullable = false)
    private Employee employee;

    @OneToOne
    @JoinColumn(name="weekly_time_sheet_id")
    private WeeklyTimeSheet weeklyTimeSheet;

    @OneToMany
    @JoinColumn(name="daily_time_sheet_id")
    private Set<TimeSheetEntry> timeSheetEntryList=new LinkedHashSet<>();

    @Column(nullable = false)
    private Long totalWorkedHours;

    private Long totalIdleHours;

}
