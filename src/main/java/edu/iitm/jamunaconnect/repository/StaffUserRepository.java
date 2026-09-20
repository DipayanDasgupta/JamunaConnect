package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffUserRepository extends JpaRepository<StaffUser, String> {

    Optional<StaffUser> findByUsername(String username);
}