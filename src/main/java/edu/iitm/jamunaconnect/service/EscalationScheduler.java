package edu.iitm.jamunaconnect.service;

import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.notify.NotificationService;
import edu.iitm.jamunaconnect.repository.ComplaintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Module F: daily scan for complaints stuck in Open/Acknowledged past the
 * threshold, so a query cannot silently sit unread the way a WhatsApp thread can.
 */
@Component
public class EscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EscalationScheduler.class);

    private final ComplaintRepository complaints;
    private final NotificationService notifier;
    private final long afterHours;

    public EscalationScheduler(ComplaintRepository complaints,
                               NotificationService notifier,
                               @Value("${app.notify.after-hours:72}") long afterHours) {
        this.complaints = complaints;
        this.notifier = notifier;
        this.afterHours = afterHours;
    }

    @Scheduled(cron = "${app.escalation.cron}")
    @Transactional(readOnly = true)
    public void escalateStaleComplaints() {
        Instant threshold = Instant.now().minus(afterHours, ChronoUnit.HOURS);
        List<Complaint> stale = complaints.findStale(
                List.of(ComplaintStatus.OPEN, ComplaintStatus.ACKNOWLEDGED), threshold);
        if (stale.isEmpty()) {
            log.debug("Escalation scan found nothing stale");
            return;
        }
        log.info("Escalation scan: {} stale complaint(s) past {}h", stale.size(), afterHours);
        stale.forEach(notifier::escalate);
    }
}