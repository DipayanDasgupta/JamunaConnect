package edu.iitm.jamunaconnect.config;

import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.repository.ComplaintRepository;
import edu.iitm.jamunaconnect.service.TrackingTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Complaints filed before V8 have no tracking token. Give them one on startup
 * so an id alone can never be used to read a resident's report.
 */
@Component
public class TrackingTokenBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TrackingTokenBackfill.class);

    private final ComplaintRepository complaints;

    public TrackingTokenBackfill(ComplaintRepository complaints) {
        this.complaints = complaints;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Complaint> missing = complaints.findByTrackingTokenIsNull();
        missing.forEach(complaint -> {
            complaint.setTrackingToken(TrackingTokens.newToken());
            complaints.save(complaint);
        });
        if (!missing.isEmpty()) {
            log.info("Backfilled tracking tokens for {} complaint(s)", missing.size());
        }
    }
}
