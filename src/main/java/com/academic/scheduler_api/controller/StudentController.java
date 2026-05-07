package com.academic.scheduler_api.controller;

import com.academic.scheduler_api.models.Student;
import com.academic.scheduler_api.repository.DepartmentRepository;
import com.academic.scheduler_api.repository.StudentRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentRepository    studentRepo;
    private final DepartmentRepository deptRepo;

    public StudentController(StudentRepository studentRepo, DepartmentRepository deptRepo) {
        this.studentRepo = studentRepo;
        this.deptRepo    = deptRepo;
    }

    @GetMapping
    public List<Student> getAll() { return studentRepo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getById(@PathVariable Long id) {
        return studentRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{deptId}")
    public List<Student> getByDepartment(@PathVariable Long deptId) {
        return studentRepo.findByDepartmentId(deptId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody StudentDTO dto) {
        Student s = new Student();
        s.setName(dto.name);
        s.setEmail(dto.email);
        s.setStudyYear(dto.studyYear);
        if (dto.departmentId != null)
            deptRepo.findById(dto.departmentId).ifPresent(s::setDepartment);
        return ResponseEntity.ok(studentRepo.save(s));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody StudentDTO dto) {
        return studentRepo.findById(id).map(s -> {
            s.setName(dto.name);
            s.setEmail(dto.email);
            s.setStudyYear(dto.studyYear);
            if (dto.departmentId != null)
                deptRepo.findById(dto.departmentId).ifPresent(s::setDepartment);
            return ResponseEntity.ok(studentRepo.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!studentRepo.existsById(id)) return ResponseEntity.notFound().build();
        studentRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    static class StudentDTO {
        public String name;
        public String email;
        public int studyYear;
        public Long departmentId;
    }
}
