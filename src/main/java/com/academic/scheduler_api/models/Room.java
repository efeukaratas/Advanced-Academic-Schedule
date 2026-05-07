package com.academic.scheduler_api.models;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;            // "A-101", "B-Lab2"

    @Column
    private String building;        // "A Blok", "B Blok"

    @Column(nullable = false)
    private int capacity;           // 30, 50, 100

    @Column
    private String roomType;        // "Amfi", "Lab", "Sinif"

    public Room() {}

    public Room(String name, String building, int capacity, String roomType) {
        this.name     = name;
        this.building = building;
        this.capacity = capacity;
        this.roomType = roomType;
    }

    // Getters
    public Long   getId()       { return id; }
    public String getName()     { return name; }
    public String getBuilding() { return building; }
    public int    getCapacity() { return capacity; }
    public String getRoomType() { return roomType; }

    // Setters
    public void setId(Long id)             { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setBuilding(String b)      { this.building = b; }
    public void setCapacity(int c)         { this.capacity = c; }
    public void setRoomType(String t)      { this.roomType = t; }
}
