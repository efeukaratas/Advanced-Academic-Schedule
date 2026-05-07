package com.academic.scheduler_api.models;

import jakarta.persistence.*;

@Entity
@Table(name = "enrollment_requests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id","course_id"}))
public class EnrollmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false,
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Course course;

    @Column
    private int priority;   // 1 = yüksek öncelik, 3 = düşük

    @Column
    private boolean approved;

    public EnrollmentRequest() {}

    public EnrollmentRequest(Student student, Course course, int priority) {
        this.student  = student;
        this.course   = course;
        this.priority = priority;
        this.approved = false;
    }

    // Getters
    public Long    getId()       { return id; }
    public Student getStudent()  { return student; }
    public Course  getCourse()   { return course; }
    public int     getPriority() { return priority; }
    public boolean isApproved()  { return approved; }

    // Setters
    public void setId(Long id)             { this.id = id; }
    public void setStudent(Student s)      { this.student = s; }
    public void setCourse(Course c)        { this.course = c; }
    public void setPriority(int p)         { this.priority = p; }
    public void setApproved(boolean a)     { this.approved = a; }
}
