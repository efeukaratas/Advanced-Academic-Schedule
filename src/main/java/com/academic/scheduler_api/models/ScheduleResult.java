package com.academic.scheduler_api.models;

import java.util.List;

/**
 * Çizelgeleme motorunun dönüş nesnesi.
 * Hem başarılı atamaları hem de zamanlanamayan dersleri taşır.
 */
public class ScheduleResult {

    private final List<ScheduledCourse> scheduled;
    private final List<UnscheduledCourse> unscheduled;
    private final long elapsedMs;

    public ScheduleResult(List<ScheduledCourse> scheduled,
                          List<UnscheduledCourse> unscheduled,
                          long elapsedMs) {
        this.scheduled   = scheduled;
        this.unscheduled = unscheduled;
        this.elapsedMs   = elapsedMs;
    }

    public List<ScheduledCourse>   getScheduled()   { return scheduled; }
    public List<UnscheduledCourse> getUnscheduled() { return unscheduled; }
    public long                    getElapsedMs()   { return elapsedMs; }
}
