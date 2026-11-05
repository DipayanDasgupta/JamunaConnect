package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByEventDateDescCreatedAtDesc();
}
