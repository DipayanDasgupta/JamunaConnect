package edu.iitm.jamunaconnect.config;

import edu.iitm.jamunaconnect.domain.Role;
import edu.iitm.jamunaconnect.domain.StaffUser;
import edu.iitm.jamunaconnect.repository.StaffUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Rotates the seeded development staff accounts without putting a BCrypt hash
 * or plaintext password in the repository. When the environment supplies a
 * hash, the account is created or overwritten on startup.
 */
@Component
public class StaffCredentialsSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StaffCredentialsSeeder.class);

    private final StaffUserRepository staffUsers;

    @Value("${app.staff.office.username:}")
    private String officeUsername;
    @Value("${app.staff.office.password-hash:}")
    private String officeHash;
    @Value("${app.staff.office.display-name:Hostel Office}")
    private String officeDisplayName;

    @Value("${app.staff.warden.username:}")
    private String wardenUsername;
    @Value("${app.staff.warden.password-hash:}")
    private String wardenHash;
    @Value("${app.staff.warden.display-name:Warden}")
    private String wardenDisplayName;

    public StaffCredentialsSeeder(StaffUserRepository staffUsers) {
        this.staffUsers = staffUsers;
    }

    @Override
    public void run(ApplicationArguments args) {
        upsert(officeUsername, officeHash, Role.OFFICE_STAFF, officeDisplayName);
        upsert(wardenUsername, wardenHash, Role.WARDEN, wardenDisplayName);
    }

    private void upsert(String username, String hash, Role role, String displayName) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(hash)) {
            return;
        }
        if (!hash.startsWith("$2")) {
            log.warn("Ignoring staff hash for {}: not a BCrypt hash", username);
            return;
        }
        StaffUser user = staffUsers.findByUsername(username.trim()).orElseGet(StaffUser::new);
        user.setUsername(username.trim());
        user.setPasswordHash(hash.trim());
        user.setRole(role);
        user.setDisplayName(displayName);
        staffUsers.save(user);
        log.info("Staff account {} seeded from environment", username);
    }
}
