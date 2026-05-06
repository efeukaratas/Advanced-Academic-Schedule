package com.academic.scheduler_api.models;

public class ScheduledCourse {

    private int courseId;
    private int instructorId;
    private int roomId;
    private int timeSlot;

    public ScheduledCourse(int courseId, int instructorId, int roomId, int timeSlot) {
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.roomId = roomId;
        this.timeSlot = timeSlot;
    }

    public int getCourseId()      { return courseId; }
    public int getInstructorId()  { return instructorId; }
    public int getRoomId()        { return roomId; }
    public int getTimeSlot()      { return timeSlot; }

    @Override
    public String toString() {
        return "Course=" + courseId +
               " | Instructor=" + instructorId +
               " | Room=" + roomId +
               " | TimeSlot=" + timeSlot;
    }
}