package edu.iitm.jamunaconnect;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Server-rendered pages are proxied through the static site, where /css and
 * /js belong to the frontend. Their own assets must stay reachable anonymously
 * under /staff-assets so the dashboard does not 404 its script in production.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class StaticAssetsTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void backendStylesheetIsServedAnonymously() throws Exception {
        mvc.perform(get("/staff-assets/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }

    @Test
    void hostelMapScriptIsServedAnonymously() throws Exception {
        String script = mvc.perform(get("/staff-assets/js/hostel-map.js"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(script).contains("const FLOORS", "/api/rooms", "extRightTop");
    }

    @Test
    void dashboardScriptIsServedAnonymously() throws Exception {
        mvc.perform(get("/staff-assets/js/dashboard.js"))
                .andExpect(status().isOk());
    }
}
