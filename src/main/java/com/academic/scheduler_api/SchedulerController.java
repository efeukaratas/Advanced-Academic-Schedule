package com.academic.scheduler_api;

import com.academic.scheduler_api.implementations.DAGCourseGraph;
import com.academic.scheduler_api.implementations.FastInstructorManager;
import com.academic.scheduler_api.implementations.FastRoomManager;
import com.academic.scheduler_api.implementations.KahnScheduler;
import com.academic.scheduler_api.models.*;
import com.academic.scheduler_api.repository.*;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class SchedulerController {

    private final CourseRepository            courseRepo;
    private final EnrollmentRepository        enrollRepo;
    private final DepartmentRepository        deptRepo;
    private final FacultyPreferenceRepository prefRepo;

    private static final int   NUM_TIME_SLOTS  = 50;
    private static final int   NUM_INSTRUCTORS = 10;
    private static final int[] ROOM_CAPACITIES = {25, 30, 40, 50, 60, 100};

    public SchedulerController(CourseRepository courseRepo,
                               EnrollmentRepository enrollRepo,
                               DepartmentRepository deptRepo,
                               FacultyPreferenceRepository prefRepo) {
        this.courseRepo = courseRepo;
        this.enrollRepo = enrollRepo;
        this.deptRepo   = deptRepo;
        this.prefRepo   = prefRepo;
    }

    // ── GET /api/schedule/generate ──────────────────────────────────────────
    @GetMapping("/generate")
    public Map<String, Object> generate() {
        return scheduleToResponse(buildSchedule(courseRepo.findAll()));
    }

    // ── GET /api/schedule/generate/department/{id} ──────────────────────────
    @GetMapping("/generate/department/{deptId}")
    public Map<String, Object> generateByDepartment(@PathVariable Long deptId) {
        return scheduleToResponse(buildSchedule(courseRepo.findByDepartmentId(deptId)));
    }

    // ── POST /api/schedule/update ────────────────────────────────────────────
    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody UpdateRequest req) {
        long start = System.currentTimeMillis();
        List<Course> affectedCourses;
        if ("enrollment".equals(req.getType())) {
            Optional<Course> opt = courseRepo.findById((long) req.getCourseId());
            affectedCourses = opt.map(List::of).orElse(Collections.emptyList());
        } else {
            affectedCourses = courseRepo.findAll().stream()
                .filter(c -> c.getInstructorId() == req.getEntityId())
                .toList();
        }
        Map<String, Object> response = scheduleToResponse(buildSchedule(affectedCourses));
        response.put("type",            req.getType());
        response.put("affectedCourses", affectedCourses.size());
        response.put("elapsedMs",       System.currentTimeMillis() - start);
        return response;
    }

    // ── GET /api/schedule/departments ───────────────────────────────────────
    @GetMapping("/departments")
    public List<Department> getDepartments() {
        return deptRepo.findAll();
    }

    // ── GET /api/schedule/stats ──────────────────────────────────────────────
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCourses",     courseRepo.count());
        stats.put("totalDepartments", deptRepo.count());
        stats.put("totalEnrollments", enrollRepo.count());
        return stats;
    }

    // ── Yardimci: ScheduleResult -> Map ─────────────────────────────────────
    private Map<String, Object> scheduleToResponse(ScheduleResult sr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scheduled",        sr.getScheduled());
        m.put("unscheduled",      sr.getUnscheduled());
        m.put("scheduledCount",   sr.getScheduled().size());
        m.put("unscheduledCount", sr.getUnscheduled().size());
        m.put("elapsedMs",        sr.getElapsedMs());
        return m;
    }

    // ── Ortak zamanlama motoru ───────────────────────────────────────────────
    private ScheduleResult buildSchedule(List<Course> courses) {
        if (courses.isEmpty()) return new ScheduleResult(
            Collections.emptyList(), Collections.emptyList(), 0);

        int n = courses.size();

        Map<Long, Long> enrollMap = new HashMap<>();
        for (Object[] row : enrollRepo.countEnrollmentsPerCourse()) {
            enrollMap.put((Long) row[0], (Long) row[1]);
        }

        int[] courseIds     = new int[n];
        int[] instructorIds = new int[n];
        int[] enrollCounts  = new int[n];
        Map<Long, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            Course c = courses.get(i);
            courseIds[i]     = c.getId().intValue();
            instructorIds[i] = c.getInstructorId();
            enrollCounts[i]  = enrollMap.getOrDefault(c.getId(), 20L).intValue();
            idToIndex.put(c.getId(), i);
        }

        DAGCourseGraph dag = new DAGCourseGraph();
        dag.initialize(n);
        for (int i = 0; i < n; i++) {
            Course c = courses.get(i);
            for (Long prereqId : c.getPrerequisiteIds()) {
                Integer idx = idToIndex.get(prereqId);
                if (idx != null) dag.addPrerequisite(idx, i);
            }
        }

        FastInstructorManager im = new FastInstructorManager();
        im.initialize(NUM_INSTRUCTORS, NUM_TIME_SLOTS);
        prefRepo.findAll().forEach(p -> { if (!p.isAvailable()) im.markBusy(p.getInstructorId(), p.getSlot()); });

        FastRoomManager rm = new FastRoomManager();
        rm.initialize(ROOM_CAPACITIES.length, NUM_TIME_SLOTS, ROOM_CAPACITIES);

        KahnScheduler scheduler = new KahnScheduler();
        scheduler.setManagers(im, rm, dag);
        scheduler.setCourseData(courseIds, instructorIds, n, NUM_INSTRUCTORS, NUM_TIME_SLOTS);
        scheduler.setEnrollmentCounts(enrollCounts);

        long t0 = System.currentTimeMillis();
        ScheduleResult sr = scheduler.generateSchedule();
        long elapsed = System.currentTimeMillis() - t0;
        System.out.printf("OK: %d zamanlandı, %d zamanlanamadı - %d ms%n",
            sr.getScheduled().size(), sr.getUnscheduled().size(), elapsed);

        for (ScheduledCourse sc : sr.getScheduled()) {
            courses.stream().filter(c -> c.getId().intValue() == sc.getCourseId()).findFirst()
                .ifPresent(c -> {
                    sc.setCourseName(c.getName() + " (" + c.getCode() + ")");
                    if (c.getDepartment() != null) sc.setDepartmentName(c.getDepartment().getName());
                });
        }
        for (UnscheduledCourse uc : sr.getUnscheduled()) {
            courses.stream().filter(c -> c.getId().intValue() == uc.getCourseId()).findFirst()
                .ifPresent(c -> {
                    uc.setCourseName(c.getName() + " (" + c.getCode() + ")");
                    if (c.getDepartment() != null) uc.setDepartmentName(c.getDepartment().getName());
                });
        }

        return new ScheduleResult(sr.getScheduled(), sr.getUnscheduled(), elapsed);
    }
}