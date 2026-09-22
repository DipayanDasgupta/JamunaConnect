package edu.iitm.jamunaconnect.notify;

import edu.iitm.jamunaconnect.domain.Complaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Module F: one notification service for both the submit path and the daily
 * escalation. Mail is notification-only; if SMTP is absent or fails the
 * complaint still files and appears on the dashboard (in-app queue fallback).
 */
@Service
public class MailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(MailNotificationService.class);

    private final Optional<JavaMailSender> mailSender;
    private final String officeEmail;
    private final String escalationEmail;

    public MailNotificationService(
            Optional<JavaMailSender> mailSender,
            @Value("${app.office-email}") String officeEmail,
            @Value("${app.escalation-email}") String escalationEmail) {
        this.mailSender = mailSender;
        this.officeEmail = officeEmail;
        this.escalationEmail = escalationEmail;
    }

    @Override
    @Async("notificationExecutor")
    public void complaintFiled(Complaint complaint) {
        send(officeEmail,
                "[JamunaConnect] New complaint #" + complaint.getId() + " (" + complaint.getCategory() + ")",
                """
                A new complaint has been filed.

                Id:       #%d
                Room:     %s
                Category: %s
                Filed by: %s
                Status:   %s

                %s
                """.formatted(complaint.getId(), complaint.getRoomNumber(), complaint.getCategory(),
                        complaint.getSubmitterName(), complaint.getStatus(), complaint.getDescription()));
    }

    @Override
    @Async("notificationExecutor")
    public void escalate(Complaint complaint) {
        send(escalationEmail,
                "[JamunaConnect] Escalation: complaint #" + complaint.getId() + " stale over threshold",
                """
                A complaint has been left in %s past the escalation threshold.

                Id:       #%d
                Room:     %s
                Category: %s
                Filed at: %s

                Please acknowledge or progress it on the staff dashboard.
                """.formatted(complaint.getStatus(), complaint.getId(), complaint.getRoomNumber(),
                        complaint.getCategory(), complaint.getCreatedAt()));
    }

    private void send(String to, String subject, String body) {
        if (mailSender.isEmpty() || !StringUtils.hasText(to)) {
            log.warn("Mail unavailable; complaint notification kept in-app only: {}", subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.get().send(message);
            log.info("Notification sent to {}: {}", to, subject);
        } catch (MailException ex) {
            // Never propagate: the complaint is already persisted.
            log.error("Mail send failed ({}); complaint remains on the dashboard queue", subject, ex);
        }
    }
}