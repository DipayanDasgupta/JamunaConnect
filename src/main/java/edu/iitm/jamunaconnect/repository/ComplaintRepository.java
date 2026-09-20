package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import edu.iitm.jamunaconnect.domain.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByClientToken(String clientToken);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    List<Complaint> findByStatusOrderByCreatedAtAsc(ComplaintStatus status);

    /**
     * Complaints stuck in Open/Acknowledged past the escalation threshold.
     * Used by the daily scheduler.
     */
    @Query("""
            SELECT c FROM Complaint c
            WHERE c.status IN :statuses
              AND c.createdAt < :threshold
            ORDER BY c.createdAt
            """)
    List<Complaint> findStale(@Param("statuses") List<ComplaintStatus> statuses,
                              @Param("threshold") Instant threshold);
}