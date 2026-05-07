package com.academic.scheduler_api.models;

import jakarta.persistence.*;

@Entity
@Table(name = "faculty_preferences")
public class FacultyPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instructor_id", nullable = false)
    private int instructorId;

    // slot = (day * 10) + hour -> 0 ile 49 arası
    @Column(nullable = false)
    private int slot;

    // false ise bu slotta ders veremez (unavailable)
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    // Tercih oncelik puani: 0 = yasakli, 1 = istenmiyor, 2 = farketmez, 3 = tercih ediliyor
    @Column(name = "preference_score")
    private int preferenceScore;

    public FacultyPreference() {}

    public FacultyPreference(int instructorId, int slot, boolean isAvailable) {
        this.instructorId    = instructorId;
        this.slot            = slot;
        this.isAvailable     = isAvailable;
        this.preferenceScore = isAvailable ? 2 : 0;
    }

    public FacultyPreference(int instructorId, int slot, boolean isAvailable, int preferenceScore) {
        this.instructorId    = instructorId;
        this.slot            = slot;
        this.isAvailable     = isAvailable;
        this.preferenceScore = preferenceScore;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public int getPreferenceScore() { return preferenceScore; }
    public void setPreferenceScore(int s) { this.preferenceScore = s; }
}
