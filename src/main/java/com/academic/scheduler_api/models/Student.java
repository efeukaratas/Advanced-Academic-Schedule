package com.academic.scheduler_api.models;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Department department;

    @Column(name = "study_year")
    private int studyYear;   // 1, 2, 3, 4

    public Student() {}

    public Student(String name, String email, Department department, int studyYear) {
        this.name       = name;
        this.email      = email;
        this.department = department;
        this.studyYear  = studyYear;
    }

    // Getters
    public Long       getId()         { return id; }
    public String     getName()       { return name; }
    public String     getEmail()      { return email; }
    public Department getDepartment() { return department; }
    public int        getStudyYear()  { return studyYear; }

    // Setters
    public void setId(Long id)                  { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setEmail(String email)          { this.email = email; }
    public void setDepartment(Department d)     { this.department = d; }
    public void setStudyYear(int y)             { this.studyYear = y; }
}
