package com.academic.scheduler_api.implementations;

import com.academic.scheduler_api.models.ScheduleResult;
import com.academic.scheduler_api.models.ScheduledCourse;
import com.academic.scheduler_api.models.UnscheduledCourse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * KahnScheduler — Kahn topolojik sıralama + Critical Path önceliği.
 *
 * Critical Path:
 *   Her ders için "en uzun önkoşul zinciri" uzunluğu hesaplanır (DP üzerinde DAG).
 *   PriorityQueue bu değere göre büyükten küçüğe sıralar:
 *   kritik derslere zaman dilimi önce atanır.
 */
public class KahnScheduler {

    private FastInstructorManager instructorManager;
    private FastRoomManager       roomManager;
    private DAGCourseGraph        courseGraph;

    private int[] courseIds;
    private int[] instructorIds;
    private int[] enrollmentCounts;   // her ders için kayıtlı öğrenci sayısı
    private int   numCourses;
    private int   numInstructors;
    private int   numTimeSlots;

    // -----------------------------------------------------------------------
    //  Setter'lar
    // -----------------------------------------------------------------------

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
        // Varsayılan enrollment sayıları (DB entegrasyonunda doldurulur)
        this.enrollmentCounts = new int[numCourses];
        Arrays.fill(this.enrollmentCounts, 20);
    }

    public void setEnrollmentCounts(int[] enrollmentCounts) {
        this.enrollmentCounts = enrollmentCounts;
    }

    // -----------------------------------------------------------------------
    //  Critical Path Hesabı — DAG üzerinde DP (O(V+E))
    //  Her düğüm için: critLen[v] = 1 + max(critLen[w]) for all w in predecessors
    // -----------------------------------------------------------------------

    public int[] computeCriticalPath() {
        int[] critLen = new int[numCourses];
        // Kahn ile topolojik sıralama + DP
        int[] inDegCopy = Arrays.copyOf(courseGraph.getInDegree(), numCourses);

        java.util.Queue<Integer> q = new java.util.LinkedList<>();
        for (int i = 0; i < numCourses; i++)
            if (inDegCopy[i] == 0) { q.add(i); critLen[i] = 1; }

        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : courseGraph.getNeighbors(u)) {
                if (critLen[u] + 1 > critLen[v])
                    critLen[v] = critLen[u] + 1;   // en uzun yolu güncelle
                inDegCopy[v]--;
                if (inDegCopy[v] == 0) q.add(v);
            }
        }
        return critLen;
    }

    // -----------------------------------------------------------------------
    //  Ana Zamanlama — Kahn + PriorityQueue (kritik dersler önce)
    // -----------------------------------------------------------------------

    public ScheduleResult generateSchedule() {
        List<ScheduledCourse>   result      = new ArrayList<>();
        List<UnscheduledCourse> unscheduled = new ArrayList<>();

        int[] critLen  = computeCriticalPath();
        int[] inDegree = Arrays.copyOf(courseGraph.getInDegree(), numCourses);

        // PriorityQueue: critLen büyük olan ders önce işlenir (kritik yol önceliği)
        PriorityQueue<Integer> pq = new PriorityQueue<>(
            (a, b) -> critLen[b] - critLen[a]
        );
        for (int i = 0; i < numCourses; i++)
            if (inDegree[i] == 0) pq.add(i);

        while (!pq.isEmpty()) {
            int courseIdx    = pq.poll();
            int instructorId = instructorIds[courseIdx];
            int enrollment   = enrollmentCounts[courseIdx];

            boolean assigned = false;
            String firstSlotFailReason = null;

            // Uygun zaman dilimi + yeterli kapasiteli oda bul — her kontrol O(1)
            for (int t = 0; t < numTimeSlots && !assigned; t++) {
                if (!instructorManager.isAvailable(instructorId, t)) {
                    if (firstSlotFailReason == null) {
                        firstSlotFailReason = "Eğitmen başka bir sınıfta veya o saatte müsait değil.";
                    }
                    continue;
                }

                boolean roomFound = false;
                for (int r = 0; r < roomManager.getNumRooms() && !assigned; r++) {
                    if (roomManager.isFree(r, t)
                            && roomManager.getCapacity(r) >= enrollment) {

                        instructorManager.markBusy(instructorId, t);
                        roomManager.markOccupied(r, t);

                        ScheduledCourse sc = new ScheduledCourse(
                            courseIds[courseIdx], instructorId, r, t
                        );
                        sc.setCriticalPathLength(critLen[courseIdx]);
                        sc.setRoomCapacity(roomManager.getCapacity(r));
                        sc.setEnrollmentCount(enrollment);

                        if (t == 0) {
                            sc.setResolutionReason("En uygun saate (ilk slot) atandı.");
                            sc.setWasShifted(false);
                        } else {
                            String reason = firstSlotFailReason != null ? firstSlotFailReason : "Yeterli kapasitede boş oda bulunamadı.";
                            sc.setResolutionReason("Sorun: " + reason + " Çözüm: " + (t+1) + ". slota kaydırıldı.");
                            sc.setWasShifted(true);
                        }

                        result.add(sc);
                        assigned = true;
                        roomFound = true;
                    }
                }

                if (!roomFound && firstSlotFailReason == null) {
                    firstSlotFailReason = "Yeterli kapasitede boş oda bulunamadı.";
                }
            }

            if (!assigned) {
                String failReason = firstSlotFailReason != null ? firstSlotFailReason
                    : "Tüm zaman dilimleri dolu; hiçbir uygun oda/saat kombinasyonu bulunamadı.";
                System.out.printf("UYARI: Ders %d zamanlanamadı! Neden: %s%n",
                    courseIds[courseIdx], failReason);
                UnscheduledCourse uc = new UnscheduledCourse(
                    courseIds[courseIdx], enrollmentCounts[courseIdx], failReason
                );
                unscheduled.add(uc);
            }

            // Önkoşulu tamamlandı → bağımlı derslerin in-degree'sini azalt
            for (int neighbor : courseGraph.getNeighbors(courseIdx)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) pq.add(neighbor);
            }
        }

        return new ScheduleResult(result, unscheduled, 0);
    }
}