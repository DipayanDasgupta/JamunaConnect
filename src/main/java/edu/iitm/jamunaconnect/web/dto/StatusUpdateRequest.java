package edu.iitm.jamunaconnect.web.dto;

import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull
    private ComplaintStatus status;

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }
}