package edu.iitm.jamunaconnect.web.dto;

import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.domain.ComplaintStatus;

import java.time.Instant;

public record ComplaintResponse(Long id, String submitterName, String roomNumber,
                                String category, String description, ComplaintStatus status,
                                Instant createdAt, Instant updatedAt) {

    public static ComplaintResponse from(Complaint c) {
        return new ComplaintResponse(c.getId(), c.getSubmitterName(), c.getRoomNumber(),
                c.getCategory() == null ? null : c.getCategory().name(),
                c.getDescription(), c.getStatus(), c.getCreatedAt(), c.getUpdatedAt());
    }
}