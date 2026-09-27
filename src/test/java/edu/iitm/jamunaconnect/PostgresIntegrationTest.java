package edu.iitm.jamunaconnect;

import edu.iitm.jamunaconnect.domain.Complaint;
import edu.iitm.jamunaconnect.domain.ComplaintCategory;
import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import edu.iitm.jamunaconnect.repository.ComplaintRepository;
import edu.iitm.jamunaconnect.service.RoomSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration against a real PostgreSQL engine so Flyway + pg_trgm are tested
 * for real, never against H2's approximation. Skipped automatically when Docker
 * is unavailable (Testcontainers' @Testcontainers handles the lifecycle).
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgresIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("jamuna")
            .withUsername("jamuna")
            .withPassword("jamuna");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.search.fuzzy", () -> "true");
    }

    @Autowired
    private RoomSearchService search;

    @Autowired
    private ComplaintRepository complaints;

    @Test
    void pgTrgmFindsMisspeltOccupants() {
        assertThat(search.search("Rahull")).extracting("roomNumber").contains("A101");
        assertThat(search.search("Arjn")).extracting("roomNumber").contains("A102");
    }

    @Test
    void staleComplaintsAreFoundPastThreshold() {
        Complaint stale = new Complaint();
        stale.setSubmitterName("Ravi");
        stale.setRoomNumber("A101");
        stale.setCategory(ComplaintCategory.WATER);
        stale.setDescription("stale");
        stale.setStatus(ComplaintStatus.OPEN);
        stale.setCreatedAt(Instant.now().minus(80, ChronoUnit.HOURS));
        stale.setUpdatedAt(Instant.now().minus(80, ChronoUnit.HOURS));
        complaints.save(stale);

        List<Complaint> found = complaints.findStale(
                List.of(ComplaintStatus.OPEN, ComplaintStatus.ACKNOWLEDGED),
                Instant.now().minus(72, ChronoUnit.HOURS));
        assertThat(found).extracting("description").contains("stale");
    }
}