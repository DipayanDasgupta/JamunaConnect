package edu.iitm.jamunaconnect;

import edu.iitm.jamunaconnect.repository.ResidentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Public content pages plus staff-only notice/resident management. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ContentFeatureTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ResidentRepository residents;

    @Test
    void publicContentPagesLoad() throws Exception {
        mvc.perform(get("/notices")).andExpect(status().isOk());
        mvc.perform(get("/emergency")).andExpect(status().isOk());
        mvc.perform(get("/wifi")).andExpect(status().isOk());
        mvc.perform(get("/map")).andExpect(status().isOk());
    }

    @Test
    void hostelMapLoadsWithoutLeaflet() throws Exception {
        String html = mvc.perform(get("/map"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Jamuna hostel map")))
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("hostel-map.js", "data-floor=\"1\"");
        assertThat(html.toLowerCase()).doesNotContain("leaflet", "openstreetmap", "blocks.geojson");
    }

    @Test
    void emergencyApiServesSeededSecurityDesk() throws Exception {
        mvc.perform(get("/api/emergency"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hostel Security Desk")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Adhesive bandages")));
    }

    @Test
    void anonymousCannotPostNotice() throws Exception {
        mvc.perform(post("/staff/notices").with(csrf())
                        .param("title", "Anonymous")
                        .param("description", "Should not exist")
                        .param("eventDate", "2026-12-01"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void staffNavDoesNotLeakOntoPublicPages() throws Exception {
        String html = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();
        assertThat(count(html, "href=\"/staff/budget\"")).isZero();
    }

    @Test
    @WithMockUser(username = "office", roles = "OFFICE_STAFF")
    void staffNavRendersOnce() throws Exception {
        String html = mvc.perform(get("/staff/residents"))
                .andReturn().getResponse().getContentAsString();
        assertThat(count(html, "href=\"/staff/budget\"")).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "office", roles = "OFFICE_STAFF")
    void staffManagementPagesRender() throws Exception {
        mvc.perform(get("/staff/notices")).andExpect(status().isOk());
        mvc.perform(get("/staff/emergency")).andExpect(status().isOk());
        mvc.perform(get("/staff/residents")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "office", roles = "OFFICE_STAFF")
    void officeCanPostNoticeAndPublicApiSeesIt() throws Exception {
        String title = "Test notice " + System.nanoTime();
        mvc.perform(post("/staff/notices").with(csrf())
                        .param("title", title)
                        .param("description", "A notice from the test suite")
                        .param("eventDate", "2026-12-01")
                        .param("linkUrl", "https://example.iitm.ac.in/event"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/notices"));

        mvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(title)));
    }

    @Test
    @WithMockUser(username = "office", roles = "OFFICE_STAFF")
    void officeCanBulkImportResidents() throws Exception {
        String roll = "ZZ24B998";
        mvc.perform(post("/staff/residents/bulk").with(csrf())
                        .param("rawText", roll + ",Bulk Resident,254\n"
                                + "not valid line\n"
                                + "# comment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/residents"));

        assertThat(residents.findById(roll)).isPresent();
        residents.deleteById(roll);
    }

    @Test
    @WithMockUser(username = "office", roles = "OFFICE_STAFF")
    void officeCanAddAndRemoveResident() throws Exception {
        String roll = "ZZ24B999";
        mvc.perform(post("/staff/residents").with(csrf())
                        .param("rollNumber", roll)
                        .param("name", "Test Resident")
                        .param("roomNumber", "254"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/residents"));

        assertThat(residents.findById(roll)).isPresent();
        assertThat(residents.findById(roll).orElseThrow().getRoomNumber()).isEqualTo("254");

        mvc.perform(post("/staff/residents/{roll}/delete", roll).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(residents.findById(roll)).isEmpty();
    }

    private static int count(String haystack, String needle) {
        int count = 0;
        int from = 0;
        while ((from = haystack.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }
}
