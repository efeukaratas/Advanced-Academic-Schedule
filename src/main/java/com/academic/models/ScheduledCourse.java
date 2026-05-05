package scheduler_api.models;
public class ScheduledCourse {
    public int courseId, roomId, timeSlotId, instructorId;
    public ScheduledCourse() {}
    public ScheduledCourse(int c, int r, int t, int i) {
        this.courseId=c; this.roomId=r; this.timeSlotId=t; this.instructorId=i;
    }
}