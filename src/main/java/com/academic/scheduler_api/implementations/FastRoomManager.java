package com.academic.scheduler_api.implementations;

/**
 * FastRoomManager — O(1) oda musaitlik kontrolu.
 * CSP destegi icin markFree (backtracking geri alma) eklendi.
 */
public class FastRoomManager {

    private boolean[][] roomFree;
    private int[]       capacities;
    private int numRooms;
    private int numTimeSlots;

    public void initialize(int numRooms, int numTimeSlots, int[] capacities) {
        this.numRooms     = numRooms;
        this.numTimeSlots = numTimeSlots;
        this.capacities   = capacities;
        this.roomFree     = new boolean[numRooms][numTimeSlots];

        for (int r = 0; r < numRooms; r++)
            for (int t = 0; t < numTimeSlots; t++)
                roomFree[r][t] = true;
    }

    // O(1)
    public boolean isFree(int roomId, int timeSlot) {
        return roomFree[roomId][timeSlot];
    }

    public int getCapacity(int roomId) {
        return capacities[roomId];
    }

    public void markOccupied(int roomId, int timeSlot) {
        roomFree[roomId][timeSlot] = false;
    }

    /** Backtracking: atamayi geri al */
    public void markFree(int roomId, int timeSlot) {
        roomFree[roomId][timeSlot] = true;
    }

    public int getNumRooms()     { return numRooms; }
    public int getNumTimeSlots() { return numTimeSlots; }
}