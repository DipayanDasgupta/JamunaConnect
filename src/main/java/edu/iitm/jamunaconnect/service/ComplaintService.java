package edu.iitm.jamunaconnect.service;

import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import edu.iitm.jamunaconnect.domain.ComplaintStatusHistory;
import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.domain.ComplaintCategory;
import edu.iitm.jamunaconnect.notify.NotificationService;
import edu.iitm.jamunaconnect.repository.ComplaintRepository;
import edu.iitm.jamunaconnect.repository.ComplaintStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Module E: filing, listing and the complaint state machine. Every transition is
 * appended to history; a retried submit is deduplicated by client token.
 */
@Service
public class ComplaintService {

    private final ComplaintRepository complaints;
    private final ComplaintStatusHistoryRepository history;
    private final NotificationService notifier;

    public ComplaintService(ComplaintRepository complaints,
                            ComplaintStatusHistoryRepository history,
                            NotificationService notifier) {
        this.complaints = complaints;
        this.history = history;
        this.notifier = notifier;
    }

    /**
     * Files a complaint plus its first history row. Idempotent on clientToken,
     * so the "fire two identical submits" test yields exactly one record.
     */
    @Transactional
    public Complaint submit(String submitterName, String roomNumber, ComplaintCategory category,
                            String description, String residentEmail, String clientToken) {
        if (clientToken != null) {
            Optional<Complaint> existing = complaints.findByClientToken(clientToken);
            if (existing.isPresent()) {
                Complaint found = existing.get();
                if (found.getTrackingToken() == null) {
                    found.setTrackingToken(TrackingTokens.newToken());
                    complaints.save(found);
                }
                return found;
            }
        }
        Complaint complaint = new Complaint();
        complaint.setSubmitterName(submitterName.trim());
        complaint.setRoomNumber(roomNumber.trim().toUpperCase());
        complaint.setCategory(category);
        complaint.setDescription(description.trim());
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setClientToken(clientToken);
        complaint.setTrackingToken(TrackingTokens.newToken());
        complaint.setResidentEmail(normalizeEmail(residentEmail));
        Instant now = Instant.now();
        complaint.setCreatedAt(now);
        complaint.setUpdatedAt(now);
        complaint = complaints.save(complaint);

        ComplaintStatusHistory first = new ComplaintStatusHistory();
        first.setComplaint(complaint);
        first.setOldStatus(null);
        first.setNewStatus(ComplaintStatus.OPEN);
        first.setChangedBy("resident");
        first.setChangedAt(now);
        history.save(first);

        notifier.complaintFiled(complaint);
        return complaint;
    }

    /**
     * Moves a complaint to {@code target}, rejecting illegal jumps so the audit
     * trail can never contain an impossible transition.
     */
    @Transactional
    public Complaint changeStatus(Long id, ComplaintStatus target, String changedBy) {
        Complaint complaint = complaints.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No complaint " + id));
        ComplaintStatus current = complaint.getStatus();
        if (current == target) {
            return complaint;
        }
        if (!current.canTransitionTo(target)) {
            throw new IllegalStateException(
                    "Illegal transition " + current + " -> " + target);
        }
        complaint.setStatus(target);
        complaint.setUpdatedAt(Instant.now());
        complaints.save(complaint);

        ComplaintStatusHistory entry = new ComplaintStatusHistory();
        entry.setComplaint(complaint);
        entry.setOldStatus(current);
        entry.setNewStatus(target);
        entry.setChangedBy(changedBy == null || changedBy.isBlank() ? "staff" : changedBy);
        entry.setChangedAt(Instant.now());
        history.save(entry);
        notifier.statusChanged(complaint);
        return complaint;
    }

    @Transactional(readOnly = true)
    public List<ComplaintStatusHistory> timeline(Long complaintId) {
        return history.findByComplaintIdOrderByChangedAtAsc(complaintId);
    }

    /**
     * Resident-facing status check: the random tracking token is the shared
     * secret, so an enumerable complaint id or room number is not enough.
     */
    @Transactional(readOnly = true)
    public Complaint getForResident(Long id, String trackingToken) {
        Complaint complaint = get(id);
        String expected = complaint.getTrackingToken();
        if (expected == null || trackingToken == null || trackingToken.isBlank()
                || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                        trackingToken.trim().getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("No complaint " + id + " for that tracking code");
        }
        return complaint;
    }

    private static String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    @Transactional(readOnly = true)
    public List<Complaint> list(Optional<ComplaintStatus> status) {
        return status.map(complaints::findByStatusOrderByCreatedAtAsc)
                .orElseGet(complaints::findAllByOrderByCreatedAtDesc);
    }

    @Transactional(readOnly = true)
    public Complaint get(Long id) {
        return complaints.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No complaint " + id));
    }
}