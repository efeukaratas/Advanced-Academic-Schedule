package com.academic.scheduler_api.controller;

import com.academic.scheduler_api.models.Room;
import com.academic.scheduler_api.repository.RoomRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomRepository roomRepo;

    public RoomController(RoomRepository roomRepo) {
        this.roomRepo = roomRepo;
    }

    @GetMapping
    public List<Room> getAll() { return roomRepo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getById(@PathVariable Long id) {
        return roomRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody RoomDTO dto) {
        Room r = new Room(dto.name, dto.building, dto.capacity, dto.roomType);
        return ResponseEntity.ok(roomRepo.save(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RoomDTO dto) {
        return roomRepo.findById(id).map(r -> {
            r.setName(dto.name);
            r.setBuilding(dto.building);
            r.setCapacity(dto.capacity);
            r.setRoomType(dto.roomType);
            return ResponseEntity.ok(roomRepo.save(r));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!roomRepo.existsById(id)) return ResponseEntity.notFound().build();
        roomRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    static class RoomDTO {
        public String name;
        public String building;
        public int capacity;
        public String roomType;
    }
}
