package edu.iitm.jamunaconnect.service;

import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.domain.ComplaintCategory;
import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import edu.iitm.jamunaconnect.domain.ComplaintStatusHistory;
import edu.iitm.jamunaconnect.notify.NotificationService;
import edu.iitm.jamunaconnect.repository.ComplaintRepository;
import edu.iitm.jamunaconnect.repository.ComplaintStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ComplaintServiceTest {

    private ComplaintRepository complaints;
    private ComplaintStatusHistoryRepository history;
    private NotificationService notifier;
    private ComplaintService service;

    @BeforeEach
    void setUp() {
        complaints = mock(ComplaintRepository.class);
        history = mock(ComplaintStatusHistoryRepository.class);
        notifier = mock(NotificationService.class);
        service = new ComplaintService(complaints, history, notifier);
        when(complaints.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));
        when(history.save(any(ComplaintStatusHistory.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void submitPersistsOneComplaintAndFirstHistoryRowAndNotifies() {
        when(complaints.findByClientToken("token-1")).thenReturn(Optional.empty());

        Complaint saved = service.submit("Ravi", "a101", ComplaintCategory.WATER,
                "No water on the floor", "token-1");

        assertThat(saved.getRoomNumber()).isEqualTo("A101");
        assertThat(saved.getStatus()).isEqualTo(ComplaintStatus.OPEN);
        verify(history, times(1)).save(any(ComplaintStatusHistory.class));
        verify(notifier, times(1)).complaintFiled(saved);
    }

    @Test
    void retriedSubmitReturnsExistingRowWithoutDuplicating() {
        Complaint existing = new Complaint();
        existing.setId(42L);
        when(complaints.findByClientToken("token-dup")).thenReturn(Optional.of(existing));

        Complaint result = service.submit("Ravi", "A101", ComplaintCategory.WATER,
                "No water", "token-dup");

        assertThat(result.getId()).isEqualTo(42L);
        verify(complaints, never()).save(any());
        verify(notifier, never()).complaintFiled(any());
    }

    @Test
    void illegalTransitionIsRejectedAndWritesNoHistory() {
        Complaint c = new Complaint();
        c.setId(7L);
        c.setStatus(ComplaintStatus.OPEN);
        when(complaints.findById(7L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.changeStatus(7L, ComplaintStatus.RESOLVED, "office"))
                .isInstanceOf(IllegalStateException.class);
        verify(history, never()).save(any());
    }

    @Test
    void legalTransitionAppendsExactlyOneHistoryRow() {
        Complaint c = new Complaint();
        c.setId(9L);
        c.setStatus(ComplaintStatus.OPEN);
        when(complaints.findById(9L)).thenReturn(Optional.of(c));

        service.changeStatus(9L, ComplaintStatus.ACKNOWLEDGED, "office");

        assertThat(c.getStatus()).isEqualTo(ComplaintStatus.ACKNOWLEDGED);
        verify(history, times(1)).save(any(ComplaintStatusHistory.class));
    }
}