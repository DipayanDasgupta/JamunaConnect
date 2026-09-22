package edu.iitm.jamunaconnect.web.dto;

import java.time.Instant;

public record HistoryResponse(Long id, String oldStatus, String newStatus,
                              String changedBy, Instant changedAt) {
}