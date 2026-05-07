package com.academic.scheduler_api.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;                // "Veri Yapıları", "Lineer Cebir"

    @Column
    private String code;                // "CS201", "MATH101"

    @Column
    private int credits;                // 3 veya 4

    @Column(name = "instructor_id")
    private int instructorId;           // FastInstructorManager'daki indeks

    @Column(name = "min_room_capacity")
    private int minRoomCapacity;        // gereken minimum oda kapasitesi

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Department department;

    // Prerequisite ders ID'leri (DAG için)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "course_prerequisites",
                     joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "prerequisite_id")
    private List<Long> prerequisiteIds = new ArrayList<>();

    public Course() {}

    public Course(String name, String code, int credits,
                  int instructorId, int minRoomCapacity, Department department) {
        this.name            = name;
        this.code            = code;
        this.credits         = credits;
        this.instructorId    = instructorId;
        this.minRoomCapacity = minRoomCapacity;
        this.department      = department;
    }

    // Getters
    public Long         getId()              { return id; }
    public String       getName()            { return name; }
    public String       getCode()            { return code; }
    public int          getCredits()         { return credits; }
    public int          getInstructorId()    { return instructorId; }
    public int          getMinRoomCapacity() { return minRoomCapacity; }
    public Department   getDepartment()      { return department; }
    public List<Long>   getPrerequisiteIds() { return prerequisiteIds; }

    // Setters
    public void setId(Long id)                          { this.id = id; }
    public void setName(String name)                    { this.name = name; }
    public void setCode(String code)                    { this.code = code; }
    public void setCredits(int credits)                 { this.credits = credits; }
    public void setInstructorId(int iid)                { this.instructorId = iid; }
    public void setMinRoomCapacity(int c)               { this.minRoomCapacity = c; }
    public void setDepartment(Department d)             { this.department = d; }
    public void setPrerequisiteIds(List<Long> ids)      { this.prerequisiteIds = ids; }
}
