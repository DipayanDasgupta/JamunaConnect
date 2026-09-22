package edu.iitm.jamunaconnect.web.api;

import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import edu.iitm.jamunaconnect.service.ComplaintService;
import edu.iitm.jamunaconnect.service.RateLimiter;
import edu.iitm.jamunaconnect.web.dto.ComplaintRequest;
import edu.iitm.jamunaconnect.web.dto.ComplaintResponse;
import edu.iitm.jamunaconnect.web.dto.HistoryResponse;
import edu.iitm.jamunaconnect.web.dto.StatusUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/** Module E: file, list, patch status, and expose the audit timeline. */
@RestController
@RequestMapping("/api/complaints")
public class ComplaintsApiController {

    private final ComplaintService complaints;
    private final RateLimiter rateLimiter;

    public ComplaintsApiController(ComplaintService complaints, RateLimiter rateLimiter) {
        this.complaints = complaints;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    public ResponseEntity<ComplaintResponse> file(@Valid @RequestBody ComplaintRequest request,
                                                   HttpServletRequest http) {
        if (!rateLimiter.allow(clientIp(http))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        Complaint saved = complaints.submit(request.getSubmitterName(), request.getRoomNumber(),
                request.getCategory(), request.getDescription(), request.getClientToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(ComplaintResponse.from(saved));
    }

    @GetMapping
    public List<ComplaintResponse> list(@RequestParam(name = "status", required = false) ComplaintStatus status) {
        return complaints.list(Optional.ofNullable(status)).stream()
                .map(ComplaintResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ComplaintResponse get(@PathVariable Long id) {
        return ComplaintResponse.from(complaints.get(id));
    }

    @GetMapping("/{id}/history")
    public List<HistoryResponse> history(@PathVariable Long id) {
        return complaints.timeline(id).stream()
                .map(h -> new HistoryResponse(h.getId(),
                        h.getOldStatus() == null ? null : h.getOldStatus().name(),
                        h.getNewStatus().name(), h.getChangedBy(), h.getChangedAt()))
                .toList();
    }

    @PatchMapping("/{id}")
    public ComplaintResponse patch(@PathVariable Long id,
                                   @Valid @RequestBody StatusUpdateRequest request,
                                   Authentication authentication) {
        String changedBy = authentication == null ? "staff" : authentication.getName();
        return ComplaintResponse.from(complaints.changeStatus(id, request.getStatus(), changedBy));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}