package com.academic.scheduler_api.implementations;

/**
 * FastInstructorManager — O(1) egitmen musaitlik kontrolu.
 * CSP destegi icin:
 *   - preferenceScores: her egitmenin her slot icin tercih puani (0-3)
 *   - markFree: backtracking sirasinda atamayi geri almak icin
 */
public class FastInstructorManager {

    private boolean[][] available;
    private int[][]     preferenceScores;   // 0=yasakli, 1=istenmiyor, 2=notr, 3=tercih
    private int numInstructors;
    private int numTimeSlots;

    public void initialize(int numInstructors, int numTimeSlots) {
        this.numInstructors   = numInstructors;
        this.numTimeSlots     = numTimeSlots;
        this.available        = new boolean[numInstructors][numTimeSlots];
        this.preferenceScores = new int[numInstructors][numTimeSlots];

        for (int i = 0; i < numInstructors; i++)
            for (int t = 0; t < numTimeSlots; t++) {
                available[i][t]        = true;
                preferenceScores[i][t] = 2;  // varsayilan: notr
            }
    }

    // O(1) musaitlik kontrolu
    public boolean isAvailable(int instructorId, int timeSlot) {
        return available[instructorId][timeSlot];
    }

    public void markBusy(int instructorId, int timeSlot) {
        available[instructorId][timeSlot] = false;
    }

    /** Backtracking: atamayi geri al */
    public void markFree(int instructorId, int timeSlot) {
        available[instructorId][timeSlot] = true;
    }

    /** Tercih puanini set et */
    public void setPreferenceScore(int instructorId, int timeSlot, int score) {
        preferenceScores[instructorId][timeSlot] = score;
        if (score == 0) available[instructorId][timeSlot] = false;
    }

    /** Tercih puanini getir (0-3). Yuksek = daha tercih edilen slot */
    public int getPreferenceScore(int instructorId, int timeSlot) {
        return preferenceScores[instructorId][timeSlot];
    }

    public int getNumInstructors() { return numInstructors; }
    public int getNumTimeSlots()   { return numTimeSlots; }
}