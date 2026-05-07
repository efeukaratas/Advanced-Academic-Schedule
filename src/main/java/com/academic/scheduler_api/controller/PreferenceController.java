package com.academic.scheduler_api.controller;

import com.academic.scheduler_api.models.FacultyPreference;
import com.academic.scheduler_api.repository.FacultyPreferenceRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferences")
@CrossOrigin(origins = "*")
public class PreferenceController {

    private final FacultyPreferenceRepository prefRepo;

    public PreferenceController(FacultyPreferenceRepository prefRepo) {
        this.prefRepo = prefRepo;
    }

    @GetMapping
    public List<FacultyPreference> getAll() { return prefRepo.findAll(); }

    @GetMapping("/instructor/{instrId}")
    public List<FacultyPreference> getByInstructor(@PathVariable int instrId) {
        return prefRepo.findByInstructorId(instrId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PrefDTO dto) {
        FacultyPreference fp = new FacultyPreference(dto.instructorId, dto.slot, dto.available, dto.preferenceScore);
        return ResponseEntity.ok(prefRepo.save(fp));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PrefDTO dto) {
        return prefRepo.findById(id).map(fp -> {
            fp.setInstructorId(dto.instructorId);
            fp.setSlot(dto.slot);
            fp.setAvailable(dto.available);
            fp.setPreferenceScore(dto.preferenceScore);
            return ResponseEntity.ok(prefRepo.save(fp));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!prefRepo.existsById(id)) return ResponseEntity.notFound().build();
        prefRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    static class PrefDTO {
        public int instructorId;
        public int slot;
        public boolean available;
        public int preferenceScore;
    }
}
