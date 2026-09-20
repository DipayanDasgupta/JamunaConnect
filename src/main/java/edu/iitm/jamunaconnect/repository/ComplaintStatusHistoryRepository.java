package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.ComplaintStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {

    List<ComplaintStatusHistory> findByComplaintIdOrderByChangedAtAsc(Long complaintId);
}