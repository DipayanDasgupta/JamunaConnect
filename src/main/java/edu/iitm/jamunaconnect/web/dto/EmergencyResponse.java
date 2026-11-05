package edu.iitm.jamunaconnect.web.dto;

import edu.iitm.jamunaconnect.domain.EmergencyContact;

import java.time.Instant;
import java.util.List;

public record EmergencyResponse(FirstAidView firstAid, List<ContactView> contacts) {

    public record FirstAidView(String contents, String guidelines, Instant updatedAt) {
    }

    public record ContactView(String roleTitle, String phone) {
    }

    public static EmergencyResponse from(FirstAidView firstAid, List<EmergencyContact> contacts) {
        return new EmergencyResponse(firstAid, contacts.stream()
                .map(c -> new ContactView(c.getRoleTitle(), c.getPhone()))
                .toList());
    }
}
