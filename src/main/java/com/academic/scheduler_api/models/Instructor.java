package com.academic.scheduler_api.models;

import jakarta.persistence.*;

@Entity
@Table(name = "instructors")
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;            // "Dr. Ahmet Yilmaz"

    @Column
    private String title;           // "Prof. Dr.", "Doc. Dr.", "Dr."

    @Column
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id",
                foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Department department;

    public Instructor() {}

    public Instructor(String name, String title, String email, Department department) {
        this.name       = name;
        this.title      = title;
        this.email      = email;
        this.department = department;
    }

    // Getters
    public Long       getId()         { return id; }
    public String     getName()       { return name; }
    public String     getTitle()      { return title; }
    public String     getEmail()      { return email; }
    public Department getDepartment() { return department; }

    // Setters
    public void setId(Long id)               { this.id = id; }
    public void setName(String name)         { this.name = name; }
    public void setTitle(String title)       { this.title = title; }
    public void setEmail(String email)       { this.email = email; }
    public void setDepartment(Department d)  { this.department = d; }

    /** Tam goruntuleme: "Prof. Dr. Ahmet Yilmaz" */
    public String getFullTitle() {
        return (title != null ? title + " " : "") + name;
    }
}
