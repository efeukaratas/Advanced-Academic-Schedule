package com.academic.scheduler_api.implementations;

public class FastInstructorManager {

    // 2D boolean matris: [eğitmenId][zamanDilimi] → true = müsait
    private boolean[][] available;
    private int numInstructors;
    private int numTimeSlots;

    public void initialize(int numInstructors, int numTimeSlots) {
        this.numInstructors = numInstructors;
        this.numTimeSlots   = numTimeSlots;
        this.available      = new boolean[numInstructors][numTimeSlots];

        // Başlangıçta tüm eğitmenler tüm saatlerde müsait
        for (int i = 0; i < numInstructors; i++)
            for (int t = 0; t < numTimeSlots; t++)
                available[i][t] = true;
    }

    // O(1) — döngü yok, direkt indeks
    public boolean isAvailable(int instructorId, int timeSlot) {
        return available[instructorId][timeSlot];
    }

    // Eğitmeni o saatte meşgul işaretle
    public void markBusy(int instructorId, int timeSlot) {
        available[instructorId][timeSlot] = false;
    }
}