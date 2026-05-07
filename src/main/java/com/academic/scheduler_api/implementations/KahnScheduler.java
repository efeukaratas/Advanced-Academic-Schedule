package com.academic.scheduler_api.implementations;

import com.academic.scheduler_api.models.ScheduleResult;
import com.academic.scheduler_api.models.ScheduledCourse;
import com.academic.scheduler_api.models.UnscheduledCourse;

import java.util.*;

/**
 * KahnScheduler — Kahn topolojik siralama + Critical Path onceligi + CSP Backtracking.
 *
 * Algoritma adimlari:
 *   1. Critical Path hesabi (DP on DAG)
 *   2. Kahn topolojik sira (PriorityQueue: kritik dersler once)
 *   3. CSP Constraint Satisfaction:
 *      - Domain: tum (timeSlot, room) ciftleri
 *      - Constraints: egitmen musaitligi, oda kapasitesi, oda dolulugu
 *      - Optimizasyon: egitmen tercih puanina gore skor
 *      - Backtracking: ders atanamiyorsa, onceki dersi geri al ve alternatif dene
 */
public class KahnScheduler {

    private FastInstructorManager instructorManager;
    private FastRoomManager       roomManager;
    private DAGCourseGraph        courseGraph;

    private int[] courseIds;
    private int[] instructorIds;
    private int[] enrollmentCounts;
    private int   numCourses;
    private int   numInstructors;
    private int   numTimeSlots;

    private static final int MAX_BACKTRACK_DEPTH = 3;  // backtracking derinlik siniri

    // Setter'lar
    public void setManagers(FastInstructorManager im, FastRoomManager rm, DAGCourseGraph cg) {
        this.instructorManager = im;
        this.roomManager       = rm;
        this.courseGraph       = cg;
    }

    public void setCourseData(int[] courseIds, int[] instructorIds,
                              int numCourses, int numInstructors, int numTimeSlots) {
        this.courseIds      = courseIds;
        this.instructorIds  = instructorIds;
        this.numCourses     = numCourses;
        this.numInstructors = numInstructors;
        this.numTimeSlots   = numTimeSlots;
        this.enrollmentCounts = new int[numCourses];
        Arrays.fill(this.enrollmentCounts, 20);
    }

    public void setEnrollmentCounts(int[] ec) { this.enrollmentCounts = ec; }

    // ─── Critical Path ── DP on DAG ── O(V+E) ──────────────────────────────
    public int[] computeCriticalPath() {
        int[] critLen = new int[numCourses];
        int[] inDegCopy = Arrays.copyOf(courseGraph.getInDegree(), numCourses);
        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < numCourses; i++)
            if (inDegCopy[i] == 0) { q.add(i); critLen[i] = 1; }
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : courseGraph.getNeighbors(u)) {
                if (critLen[u] + 1 > critLen[v]) critLen[v] = critLen[u] + 1;
                if (--inDegCopy[v] == 0) q.add(v);
            }
        }
        return critLen;
    }

    // ─── Bir ders icin CSP: tum uygun (slot, room) ciftlerini bul + skorla ──
    private static class Assignment {
        int timeSlot, roomId, score;
        String reason;
        Assignment(int t, int r, int s, String reason) {
            this.timeSlot = t; this.roomId = r; this.score = s; this.reason = reason;
        }
    }

    /**
     * Belirli bir ders icin tum kisitlari saglayan atamalari dondurur.
     * Score: egitmen tercih puani * 10 + (kapasiteye yakinlik bonusu)
     */
    private List<Assignment> findFeasibleAssignments(int courseIdx) {
        List<Assignment> candidates = new ArrayList<>();
        int instrId    = instructorIds[courseIdx];
        int enrollment = enrollmentCounts[courseIdx];

        for (int t = 0; t < numTimeSlots; t++) {
            // Kisit 1: Egitmen musait mi?
            if (!instructorManager.isAvailable(instrId, t)) continue;

            for (int r = 0; r < roomManager.getNumRooms(); r++) {
                // Kisit 2: Oda bos mu?
                if (!roomManager.isFree(r, t)) continue;
                // Kisit 3: Kapasite yeterli mi?
                if (roomManager.getCapacity(r) < enrollment) continue;

                // CSP skoru hesapla
                int prefScore    = instructorManager.getPreferenceScore(instrId, t);
                int capacityFit  = 10 - Math.min(10, (roomManager.getCapacity(r) - enrollment) / 5);
                int totalScore   = prefScore * 10 + capacityFit;

                String reason;
                if (prefScore >= 3) {
                    reason = "Egitmen tercihiyle uyumlu saate atandi (tercih puani: " + prefScore + ").";
                } else if (prefScore == 2) {
                    reason = "En uygun saate atandi.";
                } else {
                    reason = "Egitmen bu saati tercih etmiyor (puan: " + prefScore + "), ancak atanabildi.";
                }

                candidates.add(new Assignment(t, r, totalScore, reason));
            }
        }

        // Skora gore sirala: yuksek skor = daha iyi atama
        candidates.sort((a, b) -> b.score - a.score);
        return candidates;
    }

    // ─── Ana Zamanlama: Kahn + CSP + Backtracking ───────────────────────────

    public ScheduleResult generateSchedule() {
        List<ScheduledCourse>   result      = new ArrayList<>();
        List<UnscheduledCourse> unscheduled = new ArrayList<>();

        int[] critLen  = computeCriticalPath();
        int[] inDegree = Arrays.copyOf(courseGraph.getInDegree(), numCourses);

        // Kahn PriorityQueue: kritik yol once
        PriorityQueue<Integer> pq = new PriorityQueue<>((a, b) -> critLen[b] - critLen[a]);
        for (int i = 0; i < numCourses; i++)
            if (inDegree[i] == 0) pq.add(i);

        // Atama kayitlari (backtracking icin)
        Map<Integer, Assignment> assignments = new HashMap<>();

        while (!pq.isEmpty()) {
            int courseIdx    = pq.poll();
            int instructorId = instructorIds[courseIdx];
            int enrollment   = enrollmentCounts[courseIdx];

            // CSP: tum uygun atamalari bul
            List<Assignment> candidates = findFeasibleAssignments(courseIdx);

            boolean assigned = false;

            if (!candidates.isEmpty()) {
                // En iyi atamayi sec
                Assignment best = candidates.get(0);
                applyAssignment(courseIdx, best, critLen, result, assignments);
                assigned = true;
            } else {
                // CSP basarisiz: Backtracking dene
                assigned = tryBacktrack(courseIdx, critLen, result, assignments, 0);
            }

            if (!assigned) {
                String failReason = "CSP: Tum kisitlar denendi, backtracking ("
                    + MAX_BACKTRACK_DEPTH + " seviye) uygulanmasina ragmen uygun atama bulunamadi.";
                System.out.printf("UYARI: Ders %d zamanlanamadi! %s%n", courseIds[courseIdx], failReason);
                unscheduled.add(new UnscheduledCourse(courseIds[courseIdx], enrollment, failReason));
            }

            // Bagimli derslerin in-degree'sini azalt
            for (int neighbor : courseGraph.getNeighbors(courseIdx)) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) pq.add(neighbor);
            }
        }

        return new ScheduleResult(result, unscheduled, 0);
    }

    // ─── Atama uygula ──────────────────────────────────────────────────────

    private void applyAssignment(int courseIdx, Assignment a,
                                 int[] critLen, List<ScheduledCourse> result,
                                 Map<Integer, Assignment> assignments) {
        int instrId    = instructorIds[courseIdx];
        int enrollment = enrollmentCounts[courseIdx];

        instructorManager.markBusy(instrId, a.timeSlot);
        roomManager.markOccupied(a.roomId, a.timeSlot);

        ScheduledCourse sc = new ScheduledCourse(courseIds[courseIdx], instrId, a.roomId, a.timeSlot);
        sc.setCriticalPathLength(critLen[courseIdx]);
        sc.setRoomCapacity(roomManager.getCapacity(a.roomId));
        sc.setEnrollmentCount(enrollment);
        sc.setResolutionReason(a.reason);

        // Eger en iyi skor degil ve ilk slot degilse, shifted isaretle
        boolean isFirstSlot = (a.timeSlot == 0);
        boolean isTopScore  = (a.score >= 30); // prefScore 3 * 10 = 30
        sc.setWasShifted(!isFirstSlot && !isTopScore);

        result.add(sc);
        assignments.put(courseIdx, a);
    }

    // ─── Backtracking ──────────────────────────────────────────────────────

    private boolean tryBacktrack(int failedCourseIdx, int[] critLen,
                                 List<ScheduledCourse> result,
                                 Map<Integer, Assignment> assignments,
                                 int depth) {
        if (depth >= MAX_BACKTRACK_DEPTH) return false;

        // Onceden atanmis dersleri tara — geri alinabilecek aday bul
        for (int prevIdx : new ArrayList<>(assignments.keySet())) {
            Assignment prevAssign = assignments.get(prevIdx);

            // Onceki dersin atamasini geri al
            int prevInstrId = instructorIds[prevIdx];
            instructorManager.markFree(prevInstrId, prevAssign.timeSlot);
            roomManager.markFree(prevAssign.roomId, prevAssign.timeSlot);

            // Simdiki (basarisiz) ders icin tekrar CSP dene
            List<Assignment> newCandidates = findFeasibleAssignments(failedCourseIdx);

            if (!newCandidates.isEmpty()) {
                // Basarisiz ders icin atama yap
                Assignment bestNew = newCandidates.get(0);
                bestNew.reason = "CSP Backtracking: Ders " + courseIds[prevIdx]
                    + " yeniden yerlestirildi, bu derse yer acildi.";
                applyAssignment(failedCourseIdx, bestNew, critLen, result, assignments);

                // Onceki dersi alternatif slota yerlestir
                List<Assignment> altCandidates = findFeasibleAssignments(prevIdx);
                if (!altCandidates.isEmpty()) {
                    // Onceki derse yeni slot bul
                    Assignment altAssign = altCandidates.get(0);
                    altAssign.reason = "CSP Backtracking sonucu yeniden atandi.";

                    // Onceki dersin eski ScheduledCourse'unu sil ve yenisini ekle
                    result.removeIf(sc -> sc.getCourseId() == courseIds[prevIdx]);
                    applyAssignment(prevIdx, altAssign, critLen, result, assignments);
                    return true;
                } else {
                    // Onceki ders icin de yer bulunamadi — bu backtrack basarisiz
                    // Geri al: failed ders atamasini iptal et
                    int failInstr = instructorIds[failedCourseIdx];
                    instructorManager.markFree(failInstr, bestNew.timeSlot);
                    roomManager.markFree(bestNew.roomId, bestNew.timeSlot);
                    result.removeIf(sc -> sc.getCourseId() == courseIds[failedCourseIdx]);
                    assignments.remove(failedCourseIdx);

                    // Onceki dersi geri yerlestir
                    instructorManager.markBusy(prevInstrId, prevAssign.timeSlot);
                    roomManager.markOccupied(prevAssign.roomId, prevAssign.timeSlot);
                    // Daha derin backtrack dene
                    continue;
                }
            } else {
                // Bu geri alma ise yaramadi, eski halini restore et
                instructorManager.markBusy(prevInstrId, prevAssign.timeSlot);
                roomManager.markOccupied(prevAssign.roomId, prevAssign.timeSlot);
            }
        }

        return false;
    }
}