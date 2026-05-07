package com.academic.scheduler_api.controller;

import com.academic.scheduler_api.models.Instructor;
import com.academic.scheduler_api.repository.DepartmentRepository;
import com.academic.scheduler_api.repository.InstructorRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructors")
@CrossOrigin(origins = "*")
public class InstructorController {

    private final InstructorRepository instrRepo;
    private final DepartmentRepository deptRepo;

    public InstructorController(InstructorRepository instrRepo, DepartmentRepository deptRepo) {
        this.instrRepo = instrRepo;
        this.deptRepo  = deptRepo;
    }

    @GetMapping
    public List<Instructor> getAll() { return instrRepo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Instructor> getById(@PathVariable Long id) {
        return instrRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InstructorDTO dto) {
        Instructor i = new Instructor();
        i.setName(dto.name);
        i.setTitle(dto.title);
        i.setEmail(dto.email);
        if (dto.departmentId != null)
            deptRepo.findById(dto.departmentId).ifPresent(i::setDepartment);
        return ResponseEntity.ok(instrRepo.save(i));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody InstructorDTO dto) {
        return instrRepo.findById(id).map(i -> {
            i.setName(dto.name);
            i.setTitle(dto.title);
            i.setEmail(dto.email);
            if (dto.departmentId != null)
                deptRepo.findById(dto.departmentId).ifPresent(i::setDepartment);
            return ResponseEntity.ok(instrRepo.save(i));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!instrRepo.existsById(id)) return ResponseEntity.notFound().build();
        instrRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    static class InstructorDTO {
        public String name;
        public String title;
        public String email;
        public Long departmentId;
    }
}
