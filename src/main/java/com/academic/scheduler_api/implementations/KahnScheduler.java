package com.academic.scheduler_api.implementations;

import com.academic.scheduler_api.models.ScheduledCourse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class KahnScheduler {

    private FastInstructorManager instructorManager;
    private FastRoomManager       roomManager;
    private DAGCourseGraph        courseGraph;

    private int[] courseIds;
    private int[] instructorIds;
    private int   numCourses;
    private int   numInstructors;
    private int   numTimeSlots;

    public void setManagers(FastInstructorManager instructorManager,
                            FastRoomManager roomManager,
                            DAGCourseGraph courseGraph) {
        this.instructorManager = instructorManager;
        this.roomManager       = roomManager;
        this.courseGraph       = courseGraph;
    }

    public void setCourseData(int[] courseIds, int[] instructorIds,
                              int numCourses, int numInstructors, int numTimeSlots) {
        this.courseIds      = courseIds;
        this.instructorIds  = instructorIds;
        this.numCourses     = numCourses;
        this.numInstructors = numInstructors;
        this.numTimeSlots   = numTimeSlots;
    }

    public List<ScheduledCourse> generateSchedule() {
        List<ScheduledCourse> result = new ArrayList<>();

        // Orijinal inDegree dizisini korumak için kopyasını al
        int[] inDegree = Arrays.copyOf(
            courseGraph.getInDegree(),
            courseGraph.getNumCourses()
        );

        // Kahn Algoritması: önkoşulu 0 olan dersleri kuyruğa al
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++)
            if (inDegree[i] == 0)
                queue.add(i);

        while (!queue.isEmpty()) {
            int courseIdx    = queue.poll();
            int instructorId = instructorIds[courseIdx];

            boolean assigned = false;

            // Uygun zaman dilimi ve oda bul — her kontrol O(1)
            for (int t = 0; t < numTimeSlots && !assigned; t++) {
                if (!instructorManager.isAvailable(instructorId, t)) continue;

                for (int r = 0; r < roomManager.getNumRooms() && !assigned; r++) {
                    if (roomManager.isFree(r, t)) {
                        instructorManager.markBusy(instructorId, t);
                        roomManager.markOccupied(r, t);
                        result.add(new ScheduledCourse(
                            courseIds[courseIdx], instructorId, r, t
                        ));
                        assigned = true;
                    }
                }
            }

            if (!assigned) {
                System.out.println("UYARI: Ders " + courseIds[courseIdx] +
                                   " için uygun slot bulunamadı!");
            }

            // Önkoşulu tamamlandı → bağımlı derslerin in-degree'sini azalt
            for (int neighbor : courseGraph.getNeighbors(courseIdx)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0)
                    queue.add(neighbor);
            }
        }

        return result;
    }
}