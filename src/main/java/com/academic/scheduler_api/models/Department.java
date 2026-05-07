package com.academic.scheduler_api.models;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;           // "Bilgisayar Mühendisliği", "Matematik", vb.

    @Column
    private String code;           // "CS", "MATH", "PHY"

    @Column
    private String building;       // "A Blok", "B Blok"

    public Department() {}

    public Department(String name, String code, String building) {
        this.name     = name;
        this.code     = code;
        this.building = building;
    }

    // Getters
    public Long   getId()       { return id; }
    public String getName()     { return name; }
    public String getCode()     { return code; }
    public String getBuilding() { return building; }

    // Setters
    public void setId(Long id)          { this.id = id; }
    public void setName(String name)    { this.name = name; }
    public void setCode(String code)    { this.code = code; }
    public void setBuilding(String b)   { this.building = b; }
}
