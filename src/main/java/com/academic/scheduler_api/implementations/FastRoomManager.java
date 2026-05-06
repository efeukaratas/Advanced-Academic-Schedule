package com.academic.scheduler_api.implementations;

public class FastRoomManager {

    // 2D boolean matris: [odaId][zamanDilimi] → true = boş
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

    // O(1) — oda o saatte boş mu?
    public boolean isFree(int roomId, int timeSlot) {
        return roomFree[roomId][timeSlot];
    }

    public int getCapacity(int roomId) {
        return capacities[roomId];
    }

    public void markOccupied(int roomId, int timeSlot) {
        roomFree[roomId][timeSlot] = false;
    }
}