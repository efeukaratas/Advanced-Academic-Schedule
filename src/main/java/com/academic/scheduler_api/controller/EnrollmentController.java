package com.academic.scheduler_api.controller;

import com.academic.scheduler_api.models.EnrollmentRequest;
import com.academic.scheduler_api.repository.CourseRepository;
import com.academic.scheduler_api.repository.EnrollmentRepository;
import com.academic.scheduler_api.repository.StudentRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    private final EnrollmentRepository enrollRepo;
    private final StudentRepository    studentRepo;
    private final CourseRepository     courseRepo;

    public EnrollmentController(EnrollmentRepository enrollRepo,
                                StudentRepository studentRepo,
                                CourseRepository courseRepo) {
        this.enrollRepo  = enrollRepo;
        this.studentRepo = studentRepo;
        this.courseRepo   = courseRepo;
    }

    @GetMapping
    public List<EnrollmentRequest> getAll() { return enrollRepo.findAll(); }

    @GetMapping("/course/{courseId}")
    public List<EnrollmentRequest> getByCourse(@PathVariable Long courseId) {
        return enrollRepo.findByCourseId(courseId);
    }

    @GetMapping("/student/{studentId}")
    public List<EnrollmentRequest> getByStudent(@PathVariable Long studentId) {
        return enrollRepo.findByStudentId(studentId);
    }

    @GetMapping("/counts")
    public List<Object[]> getCounts() { return enrollRepo.countEnrollmentsPerCourse(); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EnrollmentDTO dto) {
        var studentOpt = studentRepo.findById(dto.studentId);
        var courseOpt   = courseRepo.findById(dto.courseId);
        if (studentOpt.isEmpty() || courseOpt.isEmpty())
            return ResponseEntity.badRequest().body("Ogrenci veya ders bulunamadi.");

        EnrollmentRequest er = new EnrollmentRequest(studentOpt.get(), courseOpt.get(), dto.priority);
        er.setApproved(dto.approved);
        return ResponseEntity.ok(enrollRepo.save(er));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody EnrollmentDTO dto) {
        return enrollRepo.findById(id).map(er -> {
            studentRepo.findById(dto.studentId).ifPresent(er::setStudent);
            courseRepo.findById(dto.courseId).ifPresent(er::setCourse);
            er.setPriority(dto.priority);
            er.setApproved(dto.approved);
            return ResponseEntity.ok(enrollRepo.save(er));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        return enrollRepo.findById(id).map(er -> {
            er.setApproved(true);
            return ResponseEntity.ok(enrollRepo.save(er));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!enrollRepo.existsById(id)) return ResponseEntity.notFound().build();
        enrollRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    static class EnrollmentDTO {
        public Long studentId;
        public Long courseId;
        public int priority;
        public boolean approved;
    }
}
