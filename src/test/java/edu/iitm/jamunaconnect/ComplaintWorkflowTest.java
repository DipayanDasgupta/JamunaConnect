package edu.iitm.jamunaconnect;

import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.domain.ComplaintCategory;
import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import edu.iitm.jamunaconnect.repository.ComplaintRepository;
import edu.iitm.jamunaconnect.repository.ComplaintStatusHistoryRepository;
import edu.iitm.jamunaconnect.service.ComplaintService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Only the Warden can close; office staff forward for approval. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ComplaintWorkflowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ComplaintService complaints;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintStatusHistoryRepository history;

    @Test
    void officeCannotCloseAComplaint() throws Exception {
        Long id = createComplaint();
        try {
            mvc.perform(patch("/api/complaints/{id}", id)
                            .with(user("office").roles("OFFICE_STAFF"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"CLOSED\"}"))
                    .andExpect(status().isForbidden());
            assertThat(complaintRepository.findById(id).orElseThrow().getStatus())
                    .isEqualTo(ComplaintStatus.OPEN);
        } finally {
            cleanup(id);
        }
    }

    @Test
    void officeForwardsAndWardenCloses() throws Exception {
        Long id = createComplaint();
        try {
            mvc.perform(patch("/api/complaints/{id}", id)
                            .with(user("office").roles("OFFICE_STAFF"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"WARDEN_REVIEW\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WARDEN_REVIEW"));

            mvc.perform(patch("/api/complaints/{id}", id)
                            .with(user("warden").roles("WARDEN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"CLOSED\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CLOSED"));
            assertThat(complaintRepository.findById(id).orElseThrow().getStatus())
                    .isEqualTo(ComplaintStatus.CLOSED);
        } finally {
            cleanup(id);
        }
    }

    @Test
    void wardenCannotCloseBeforeOfficeForwards() throws Exception {
        Long id = createComplaint();
        try {
            mvc.perform(patch("/api/complaints/{id}", id)
                            .with(user("warden").roles("WARDEN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"CLOSED\"}"))
                    .andExpect(status().isConflict());
        } finally {
            cleanup(id);
        }
    }

    private Long createComplaint() {
        Complaint complaint = complaints.submit("Workflow Test", "254", ComplaintCategory.OTHER,
                "Workflow test complaint", "workflow-" + System.nanoTime());
        return complaint.getId();
    }

    private void cleanup(Long id) {
        history.deleteAll(history.findByComplaintIdOrderByChangedAtAsc(id));
        complaintRepository.deleteById(id);
    }
}
