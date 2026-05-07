package com.academic.scheduler_api.repository;

import com.academic.scheduler_api.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByBuildingOrderByCapacityAsc(String building);
}
