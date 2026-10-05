package edu.iitm.jamunaconnect;

import edu.iitm.jamunaconnect.domain.ComplaintCategory;
import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end over the local H2 profile: file a complaint, see it Open, move it. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class JamunaConnectApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void publicPagesLoad() {
        assertThat(rest.getForEntity("/", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.getForEntity("/lookup", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.getForEntity("/contacts", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void healthEndpointIsUp() {
        ResponseEntity<String> health = rest.getForEntity("/actuator/health", String.class);
        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(health.getBody()).contains("UP");
    }

    @Test
    void seededRoomsAreSearchable() {
        ResponseEntity<String> rooms = rest.getForEntity("/api/rooms?query=254", String.class);
        assertThat(rooms.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rooms.getBody()).contains("254");
    }

    @Test
    void fileDoubleSubmitYieldsOneComplaintAndATimeline() {
        Map<String, Object> body = Map.of(
                "submitterName", "Ravi",
                "roomNumber", "A101",
                "category", ComplaintCategory.WATER.name(),
                "description", "No water on the floor",
                "clientToken", "integration-token-1");

        ResponseEntity<Map> first = rest.postForEntity("/api/complaints", body, Map.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number id = (Number) first.getBody().get("id");
        assertThat(first.getBody().get("status")).isEqualTo(ComplaintStatus.OPEN.name());

        ResponseEntity<Map> second = rest.postForEntity("/api/complaints", body, Map.class);
        assertThat(((Number) second.getBody().get("id")).longValue()).isEqualTo(id.longValue());

        ResponseEntity<String> history = rest.withBasicAuth("office", "office123")
                .getForEntity("/api/complaints/{id}/history", String.class, id.longValue());
        assertThat(history.getBody()).contains("OPEN");
    }

    @Test
    void malformedRoomIsRejectedWith400() {
        Map<String, Object> body = Map.of(
                "submitterName", "Ravi",
                "roomNumber", "!!bad!!",
                "category", ComplaintCategory.OTHER.name(),
                "description", "bad room");
        ResponseEntity<String> response = rest.postForEntity("/api/complaints", body, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}