package com.academic.scheduler_api.implementations;

public class FastInstructorManager {

    private boolean[][] available;
    private int numInstructors;
    private int numTimeSlots;

    public void initialize(int numInstructors, int numTimeSlots) {
        this.numInstructors = numInstructors;
        this.numTimeSlots   = numTimeSlots;
        this.available      = new boolean[numInstructors][numTimeSlots];

        for (int i = 0; i < numInstructors; i++)
            for (int t = 0; t < numTimeSlots; t++)
                available[i][t] = true;
    }

    // O(1) — döngü yok, direkt indeks erişimi
    public boolean isAvailable(int instructorId, int timeSlot) {
        return available[instructorId][timeSlot];
    }

    public void markBusy(int instructorId, int timeSlot) {
        available[instructorId][timeSlot] = false;
    }

    public int getNumInstructors() { return numInstructors; }
    public int getNumTimeSlots()   { return numTimeSlots; }
}