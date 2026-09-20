package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.Resident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResidentRepository extends JpaRepository<Resident, String> {

    List<Resident> findByRoomNumberOrderByName(String roomNumber);
}