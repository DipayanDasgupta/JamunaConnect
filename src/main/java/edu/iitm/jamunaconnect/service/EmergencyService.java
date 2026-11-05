package edu.iitm.jamunaconnect.service;

import edu.iitm.jamunaconnect.domain.EmergencyContact;
import edu.iitm.jamunaconnect.domain.FirstAidInfo;
import edu.iitm.jamunaconnect.repository.EmergencyContactRepository;
import edu.iitm.jamunaconnect.repository.FirstAidInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Emergency numbers plus the first-aid reference card. */
@Service
public class EmergencyService {

    private final EmergencyContactRepository contacts;
    private final FirstAidInfoRepository firstAid;

    public EmergencyService(EmergencyContactRepository contacts, FirstAidInfoRepository firstAid) {
        this.contacts = contacts;
        this.firstAid = firstAid;
    }

    @Transactional(readOnly = true)
    public List<EmergencyContact> contacts() {
        return contacts.findAllByOrderByDisplayOrderAscIdAsc();
    }

    @Transactional(readOnly = true)
    public FirstAidInfo firstAid() {
        return firstAid.findById(1).orElse(null);
    }

    @Transactional
    public FirstAidInfo updateFirstAid(String contents, String guidelines) {
        FirstAidInfo info = firstAid.findById(1).orElseGet(() -> {
            FirstAidInfo created = new FirstAidInfo();
            created.setId(1);
            return created;
        });
        info.setContents(optional(contents, 4000));
        info.setGuidelines(optional(guidelines, 4000));
        info.setUpdatedAt(Instant.now());
        return firstAid.save(info);
    }

    @Transactional
    public EmergencyContact addContact(String roleTitle, String phone) {
        EmergencyContact contact = new EmergencyContact();
        contact.setRoleTitle(required(roleTitle, "Role", 128));
        contact.setPhone(required(phone, "Phone", 64));
        contact.setDisplayOrder(nextOrder());
        return contacts.save(contact);
    }

    @Transactional
    public void deleteContact(Long id) {
        if (!contacts.existsById(id)) {
            throw new IllegalArgumentException("No emergency contact " + id);
        }
        contacts.deleteById(id);
    }

    private int nextOrder() {
        return contacts.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .mapToInt(EmergencyContact::getDisplayOrder)
                .max()
                .orElse(0) + 1;
    }

    private static String required(String value, String field, int max) {
        String cleaned = optional(value, max);
        if (cleaned == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return cleaned;
    }

    private static String optional(String value, int max) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        if (cleaned.length() > max) {
            throw new IllegalArgumentException("Value is too long");
        }
        return cleaned;
    }
}
