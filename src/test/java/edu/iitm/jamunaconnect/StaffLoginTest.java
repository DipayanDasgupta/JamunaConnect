package edu.iitm.jamunaconnect;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Staff sign-in must work through the form, not just HTTP Basic. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class StaffLoginTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void anonymousBrowserIsSentToSignInNotBare401() throws Exception {
        mvc.perform(get("/staff/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/staff/login"));
    }

    @Test
    void anonymousApiCallGetsBasicChallenge() throws Exception {
        mvc.perform(get("/api/complaints"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void formLoginLandsOnDashboard() throws Exception {
        mvc.perform(formLogin("/staff/login").user("office").password("office123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/dashboard"));
    }
}