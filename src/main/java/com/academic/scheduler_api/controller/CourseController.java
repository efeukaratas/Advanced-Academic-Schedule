package com.academic.scheduler_api.controller;

import com.academic.scheduler_api.models.Course;
import com.academic.scheduler_api.models.Department;
import com.academic.scheduler_api.models.Instructor;
import com.academic.scheduler_api.repository.CourseRepository;
import com.academic.scheduler_api.repository.DepartmentRepository;
import com.academic.scheduler_api.repository.InstructorRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseRepository      courseRepo;
    private final InstructorRepository  instrRepo;
    private final DepartmentRepository  deptRepo;

    public CourseController(CourseRepository courseRepo,
                            InstructorRepository instrRepo,
                            DepartmentRepository deptRepo) {
        this.courseRepo = courseRepo;
        this.instrRepo  = instrRepo;
        this.deptRepo   = deptRepo;
    }

    @GetMapping
    public List<Course> getAll() { return courseRepo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) {
        return courseRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{deptId}")
    public List<Course> getByDepartment(@PathVariable Long deptId) {
        return courseRepo.findByDepartmentId(deptId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CourseDTO dto) {
        Course c = new Course();
        c.setName(dto.name);
        c.setCode(dto.code);
        c.setCredits(dto.credits);
        c.setMinRoomCapacity(dto.minRoomCapacity);
        if (dto.instructorId != null)
            instrRepo.findById(dto.instructorId).ifPresent(c::setInstructor);
        if (dto.departmentId != null)
            deptRepo.findById(dto.departmentId).ifPresent(c::setDepartment);
        return ResponseEntity.ok(courseRepo.save(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CourseDTO dto) {
        return courseRepo.findById(id).map(c -> {
            c.setName(dto.name);
            c.setCode(dto.code);
            c.setCredits(dto.credits);
            c.setMinRoomCapacity(dto.minRoomCapacity);
            if (dto.instructorId != null)
                instrRepo.findById(dto.instructorId).ifPresent(c::setInstructor);
            if (dto.departmentId != null)
                deptRepo.findById(dto.departmentId).ifPresent(c::setDepartment);
            return ResponseEntity.ok(courseRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!courseRepo.existsById(id)) return ResponseEntity.notFound().build();
        courseRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // DTO
    static class CourseDTO {
        public String name;
        public String code;
        public int credits;
        public int minRoomCapacity;
        public Long instructorId;
        public Long departmentId;
    }
}
