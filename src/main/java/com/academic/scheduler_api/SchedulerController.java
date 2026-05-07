package com.academic.scheduler_api;

import com.academic.scheduler_api.models.*;
import com.academic.scheduler_api.repository.*;
import com.academic.scheduler_api.service.ScheduleService;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class SchedulerController {

    private final ScheduleService      service;
    private final DepartmentRepository deptRepo;
    private final CourseRepository     courseRepo;
    private final EnrollmentRepository enrollRepo;
    private final InstructorRepository instrRepo;
    private final RoomRepository       roomRepo;

    public SchedulerController(ScheduleService service,
                               DepartmentRepository deptRepo,
                               CourseRepository courseRepo,
                               EnrollmentRepository enrollRepo,
                               InstructorRepository instrRepo,
                               RoomRepository roomRepo) {
        this.service    = service;
        this.deptRepo   = deptRepo;
        this.courseRepo = courseRepo;
        this.enrollRepo = enrollRepo;
        this.instrRepo  = instrRepo;
        this.roomRepo   = roomRepo;
    }

    // ── Cizelge Uretimi ─────────────────────────────────────────────────────

    @GetMapping("/generate")
    public Map<String, Object> generate() {
        return toMap(service.generateAll());
    }

    @GetMapping("/generate/department/{deptId}")
    public Map<String, Object> generateByDepartment(@PathVariable Long deptId) {
        return toMap(service.generateByDepartment(deptId));
    }

    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody UpdateRequest req) {
        long start = System.currentTimeMillis();
        ScheduleResult sr = service.updateSchedule(req);
        Map<String, Object> resp = toMap(sr);
        resp.put("type",            req.getType());
        resp.put("elapsedMs",       System.currentTimeMillis() - start);
        return resp;
    }

    // ── CRUD / Lookup ───────────────────────────────────────────────────────

    @GetMapping("/departments")
    public List<Department> getDepartments() { return deptRepo.findAll(); }

    @GetMapping("/instructors")
    public List<Instructor> getInstructors() { return instrRepo.findAll(); }

    @GetMapping("/rooms")
    public List<Room> getRooms() { return roomRepo.findAll(); }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("totalCourses",      courseRepo.count());
        s.put("totalDepartments",  deptRepo.count());
        s.put("totalEnrollments",  enrollRepo.count());
        s.put("totalInstructors",  instrRepo.count());
        s.put("totalRooms",        roomRepo.count());
        return s;
    }

    // ── Yardimci ────────────────────────────────────────────────────────────

    private Map<String, Object> toMap(ScheduleResult sr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("scheduled",        sr.getScheduled());
        m.put("unscheduled",      sr.getUnscheduled());
        m.put("scheduledCount",   sr.getScheduled().size());
        m.put("unscheduledCount", sr.getUnscheduled().size());
        m.put("elapsedMs",        sr.getElapsedMs());
        return m;
    }
}