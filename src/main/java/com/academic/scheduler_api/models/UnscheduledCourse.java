package com.academic.scheduler_api.models;

/**
 * Hiçbir zaman dilimine yerleştirilemeyen dersi temsil eder.
 * Controller tarafından API yanıtına eklenir; UI "❌ Zamanlanamayan Dersler" panelinde gösterir.
 */
public class UnscheduledCourse {

    private int    courseId;
    private String courseName;
    private String departmentName;
    private int    enrollmentCount;
    private String failReason;

    public UnscheduledCourse() {}

    public UnscheduledCourse(int courseId, int enrollmentCount, String failReason) {
        this.courseId       = courseId;
        this.enrollmentCount = enrollmentCount;
        this.failReason     = failReason;
    }

    public int    getCourseId()       { return courseId; }
    public String getCourseName()     { return courseName; }
    public String getDepartmentName() { return departmentName; }
    public int    getEnrollmentCount(){ return enrollmentCount; }
    public String getFailReason()     { return failReason; }

    public void setCourseName(String n)     { this.courseName = n; }
    public void setDepartmentName(String d) { this.departmentName = d; }
}
