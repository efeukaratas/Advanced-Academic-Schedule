package com.academic.scheduler_api.models;

public class ScheduledCourse {

    private int    courseId;
    private String courseName;
    private int    instructorId;
    private String instructorName;
    private int    roomId;
    private int    roomCapacity;
    private int    timeSlotId;
    private String dayName;
    private String hourLabel;
    private int    enrollmentCount;
    private int    criticalPathLength;
    private String departmentName;
    private String resolutionReason;
    // İlk tercih edilen slota yerleştirilemeyen dersler true
    private boolean wasShifted;

    public ScheduledCourse() {}

    public ScheduledCourse(int courseId, int instructorId, int roomId, int timeSlotId) {
        this.courseId     = courseId;
        this.instructorId = instructorId;
        this.roomId       = roomId;
        this.timeSlotId   = timeSlotId;
        computeDayHour(timeSlotId);
    }

    // Slot → gün/saat dönüşümü
    // Slot 0-9  = Pazartesi 08:00-17:00
    // Slot 10-19 = Salı ...  Slot 40-49 = Cuma
    private void computeDayHour(int slot) {
        String[] days  = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma"};
        int dayIndex   = slot / 10;
        int hourOffset = slot % 10;
        this.dayName   = (dayIndex < days.length) ? days[dayIndex] : "Bilinmiyor";
        this.hourLabel = String.format("%02d:00", 8 + hourOffset);
    }

    // ---------- Getters ----------
    public int    getCourseId()          { return courseId; }
    public String getCourseName()        { return courseName; }
    public int    getInstructorId()      { return instructorId; }
    public String getInstructorName()    { return instructorName; }
    public int    getRoomId()            { return roomId; }
    public int    getRoomCapacity()      { return roomCapacity; }
    public int    getTimeSlotId()        { return timeSlotId; }
    public String getDayName()           { return dayName; }
    public String getHourLabel()         { return hourLabel; }
    public int    getEnrollmentCount()   { return enrollmentCount; }
    public int    getCriticalPathLength(){ return criticalPathLength; }
    public String getDepartmentName()    { return departmentName; }
    public String getResolutionReason()  { return resolutionReason; }
    public boolean isWasShifted()        { return wasShifted; }

    // ---------- Setters ----------
    public void setCourseName(String n)        { this.courseName = n; }
    public void setInstructorName(String n)    { this.instructorName = n; }
    public void setRoomCapacity(int c)         { this.roomCapacity = c; }
    public void setEnrollmentCount(int e)      { this.enrollmentCount = e; }
    public void setCriticalPathLength(int cpl) { this.criticalPathLength = cpl; }
    public void setDepartmentName(String d)    { this.departmentName = d; }
    public void setResolutionReason(String r)  { this.resolutionReason = r; }
    public void setWasShifted(boolean b)        { this.wasShifted = b; }
    public void setTimeSlotId(int t)           { this.timeSlotId = t; computeDayHour(t); }

    @Override
    public String toString() {
        return "Course=" + courseId + " | " + dayName + " " + hourLabel
             + " | Room=" + roomId + " | CriticalPath=" + criticalPathLength;
    }
}