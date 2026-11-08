package edu.iitm.jamunaconnect.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.domain.ComplaintStatus;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ComplaintResponse(Long id, String submitterName, String roomNumber,
                                String category, String description, ComplaintStatus status,
                                String trackingToken, String residentEmail,
                                Instant createdAt, Instant updatedAt) {

    public static ComplaintResponse from(Complaint c) {
        return new ComplaintResponse(c.getId(), c.getSubmitterName(), c.getRoomNumber(),
                c.getCategory() == null ? null : c.getCategory().name(),
                c.getDescription(), c.getStatus(), c.getTrackingToken(), c.getResidentEmail(),
                c.getCreatedAt(), c.getUpdatedAt());
    }

    /** Anonymous tracking must not echo the capability token or resident email. */
    public static ComplaintResponse publicView(Complaint c) {
        return new ComplaintResponse(c.getId(), c.getSubmitterName(), c.getRoomNumber(),
                c.getCategory() == null ? null : c.getCategory().name(),
                c.getDescription(), c.getStatus(), null, null,
                c.getCreatedAt(), c.getUpdatedAt());
    }
}