package edu.iitm.jamunaconnect.notify;

import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.domain.ComplaintCategory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MailNotificationServiceTest {

    @Test
    void sendsFromConfiguredVerifiedAddress() {
        JavaMailSender sender = mock(JavaMailSender.class);
        MailNotificationService service = new MailNotificationService(Optional.of(sender),
                "office@example.com", "warden@example.com", "jamuna@example.com");
        Complaint complaint = new Complaint();
        complaint.setId(1L);
        complaint.setRoomNumber("254");
        complaint.setCategory(ComplaintCategory.OTHER);
        complaint.setSubmitterName("Test");
        complaint.setDescription("Test complaint");

        service.complaintFiled(complaint);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isEqualTo("jamuna@example.com");
        assertThat(captor.getValue().getTo()).containsExactly("office@example.com");
    }
}
