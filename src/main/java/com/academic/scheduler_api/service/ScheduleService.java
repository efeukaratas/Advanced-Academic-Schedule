package com.academic.scheduler_api.service;

import com.academic.scheduler_api.implementations.*;
import com.academic.scheduler_api.models.*;
import com.academic.scheduler_api.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Cizelgeleme is mantigi. Controller'dan ayrilmis, test edilebilir service katmani.
 */
@Service
public class ScheduleService {

    private static final int NUM_TIME_SLOTS = 50;   // 5 gun x 10 saat

    private final CourseRepository            courseRepo;
    private final EnrollmentRepository        enrollRepo;
    private final FacultyPreferenceRepository prefRepo;
    private final InstructorRepository        instructorRepo;
    private final RoomRepository              roomRepo;

    public ScheduleService(CourseRepository courseRepo,
                           EnrollmentRepository enrollRepo,
                           FacultyPreferenceRepository prefRepo,
                           InstructorRepository instructorRepo,
                           RoomRepository roomRepo) {
        this.courseRepo     = courseRepo;
        this.enrollRepo     = enrollRepo;
        this.prefRepo       = prefRepo;
        this.instructorRepo = instructorRepo;
        this.roomRepo       = roomRepo;
    }

    /** Tum dersler icin cizelge uret */
    public ScheduleResult generateAll() {
        return buildSchedule(courseRepo.findAll());
    }

    /** Belirli departman icin cizelge uret */
    public ScheduleResult generateByDepartment(Long deptId) {
        return buildSchedule(courseRepo.findByDepartmentId(deptId));
    }

    /** Dinamik guncelleme: etkilenen dersleri yeniden zamanla */
    public ScheduleResult updateSchedule(UpdateRequest req) {
        List<Course> affected;
        if ("enrollment".equals(req.getType())) {
            Optional<Course> opt = courseRepo.findById((long) req.getCourseId());
            affected = opt.map(List::of).orElse(Collections.emptyList());
        } else {
            affected = courseRepo.findAll().stream()
                .filter(c -> c.getInstructor() != null
                          && c.getInstructor().getId() == req.getEntityId())
                .toList();
        }
        return buildSchedule(affected);
    }

    // ── Core zamanlama motoru ────────────────────────────────────────────────
    public ScheduleResult buildSchedule(List<Course> courses) {
        if (courses.isEmpty())
            return new ScheduleResult(Collections.emptyList(), Collections.emptyList(), 0);

        // DB'den egitmen ve oda listelerini al
        List<Instructor> allInstructors = instructorRepo.findAll();
        List<Room>       allRooms       = roomRepo.findAll();

        int numInstructors = allInstructors.size();
        int numRooms       = allRooms.size();
        int n              = courses.size();

        // Instructor DB id -> array index eslesmesi
        Map<Long, Integer> instrIdToIdx = new HashMap<>();
        for (int i = 0; i < numInstructors; i++) {
            instrIdToIdx.put(allInstructors.get(i).getId(), i);
        }

        // Enrollment sayilari
        Map<Long, Long> enrollMap = new HashMap<>();
        for (Object[] row : enrollRepo.countEnrollmentsPerCourse()) {
            enrollMap.put((Long) row[0], (Long) row[1]);
        }

        // Dizi verileri hazirla
        int[] courseIds     = new int[n];
        int[] instructorIds = new int[n];
        int[] enrollCounts  = new int[n];
        Map<Long, Integer> courseIdToIdx = new HashMap<>();

        for (int i = 0; i < n; i++) {
            Course c = courses.get(i);
            courseIds[i] = c.getId().intValue();
            // Instructor entity -> array index
            int iIdx = 0;
            if (c.getInstructor() != null) {
                iIdx = instrIdToIdx.getOrDefault(c.getInstructor().getId(), 0);
            }
            instructorIds[i] = iIdx;
            enrollCounts[i]  = enrollMap.getOrDefault(c.getId(), 20L).intValue();
            courseIdToIdx.put(c.getId(), i);
        }

        // DAG kur
        DAGCourseGraph dag = new DAGCourseGraph();
        dag.initialize(n);
        for (int i = 0; i < n; i++) {
            Course c = courses.get(i);
            for (Long prereqId : c.getPrerequisiteIds()) {
                Integer idx = courseIdToIdx.get(prereqId);
                if (idx != null) dag.addPrerequisite(idx, i);
            }
        }

        // Egitmen manager + tercihler (CSP icin oncelik puani dahil)
        FastInstructorManager im = new FastInstructorManager();
        im.initialize(numInstructors, NUM_TIME_SLOTS);
        for (FacultyPreference p : prefRepo.findAll()) {
            int idx = instrIdToIdx.getOrDefault((long) p.getInstructorId(), -1);
            if (idx >= 0) {
                if (!p.isAvailable()) {
                    im.markBusy(idx, p.getSlot());
                }
                im.setPreferenceScore(idx, p.getSlot(), p.getPreferenceScore());
            }
        }

        // Oda manager — DB'den gelen kapasiteler
        int[] roomCapacities = allRooms.stream().mapToInt(Room::getCapacity).toArray();
        FastRoomManager rm = new FastRoomManager();
        rm.initialize(numRooms, NUM_TIME_SLOTS, roomCapacities);

        // Scheduler calistir
        KahnScheduler scheduler = new KahnScheduler();
        scheduler.setManagers(im, rm, dag);
        scheduler.setCourseData(courseIds, instructorIds, n, numInstructors, NUM_TIME_SLOTS);
        scheduler.setEnrollmentCounts(enrollCounts);

        long t0 = System.currentTimeMillis();
        ScheduleResult sr = scheduler.generateSchedule();
        long elapsed = System.currentTimeMillis() - t0;

        // Sonuclari zenginlestir: gercek isimler ekle
        enrichScheduled(sr.getScheduled(), courses, allInstructors, allRooms, instrIdToIdx);
        enrichUnscheduled(sr.getUnscheduled(), courses);

        System.out.printf("OK: %d zamanlandi, %d zamanlanamadi - %d ms%n",
            sr.getScheduled().size(), sr.getUnscheduled().size(), elapsed);

        return new ScheduleResult(sr.getScheduled(), sr.getUnscheduled(), elapsed);
    }

    // ── Zenginlestirme yardimcilari ─────────────────────────────────────────

    private void enrichScheduled(List<ScheduledCourse> list, List<Course> courses,
                                 List<Instructor> instructors, List<Room> rooms,
                                 Map<Long, Integer> instrIdToIdx) {
        for (ScheduledCourse sc : list) {
            courses.stream()
                .filter(c -> c.getId().intValue() == sc.getCourseId())
                .findFirst()
                .ifPresent(c -> {
                    sc.setCourseName(c.getName() + " (" + c.getCode() + ")");
                    if (c.getDepartment() != null)
                        sc.setDepartmentName(c.getDepartment().getName());
                    if (c.getInstructor() != null)
                        sc.setInstructorName(c.getInstructor().getFullTitle());
                });

            // Room ismi
            int roomIdx = sc.getRoomId();
            if (roomIdx >= 0 && roomIdx < rooms.size()) {
                Room room = rooms.get(roomIdx);
                sc.setRoomName(room.getName());
                sc.setRoomCapacity(room.getCapacity());
            }
        }
    }

    private void enrichUnscheduled(List<UnscheduledCourse> list, List<Course> courses) {
        for (UnscheduledCourse uc : list) {
            courses.stream()
                .filter(c -> c.getId().intValue() == uc.getCourseId())
                .findFirst()
                .ifPresent(c -> {
                    uc.setCourseName(c.getName() + " (" + c.getCode() + ")");
                    if (c.getDepartment() != null)
                        uc.setDepartmentName(c.getDepartment().getName());
                });
        }
    }
}
