package edu.iitm.jamunaconnect.web.dto;

import java.util.List;

/** Resident-facing complaint status plus its audit timeline. */
public record PublicComplaintResponse(ComplaintResponse complaint,
                                      List<HistoryResponse> history) {
}