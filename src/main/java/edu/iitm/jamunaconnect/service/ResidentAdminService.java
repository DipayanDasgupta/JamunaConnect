package edu.iitm.jamunaconnect.service;

import edu.iitm.jamunaconnect.domain.Resident;
import edu.iitm.jamunaconnect.repository.ResidentRepository;
import edu.iitm.jamunaconnect.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Office maintenance of the resident directory, including bulk import. */
@Service
public class ResidentAdminService {

    private static final int MAX_ROLL = 16;
    private static final int MAX_NAME = 128;
    private static final int MAX_CONTACT = 64;

    private final ResidentRepository residents;
    private final RoomRepository rooms;

    public ResidentAdminService(ResidentRepository residents, RoomRepository rooms) {
        this.residents = residents;
        this.rooms = rooms;
    }

    @Transactional(readOnly = true)
    public List<Resident> list() {
        return residents.findAll(org.springframework.data.domain.Sort.by("rollNumber"));
    }

    @Transactional
    public Resident save(String rollNumber, String name, String roomNumber, String contact) {
        String roll = roll(rollNumber);
        String cleanName = required(name, "Name", MAX_NAME);
        String room = room(roomNumber);
        Resident resident = residents.findById(roll).orElseGet(() -> {
            Resident created = new Resident();
            created.setRollNumber(roll);
            return created;
        });
        resident.setName(cleanName);
        resident.setRoomNumber(room);
        resident.setContact(optional(contact, MAX_CONTACT));
        return residents.save(resident);
    }

    @Transactional
    public void delete(String rollNumber) {
        String roll = roll(rollNumber);
        if (!residents.existsById(roll)) {
            throw new IllegalArgumentException("No resident " + roll);
        }
        residents.deleteById(roll);
    }

    /**
     * One resident per line: {@code roll,name,room[,contact]}. Blank lines and
     * lines starting with '#' are ignored; valid rows are saved even if others fail.
     */
    @Transactional
    public ImportResult bulkImport(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ImportResult(0, List.of("Nothing to import"));
        }
        List<String> errors = new ArrayList<>();
        java.util.Map<String, Resident> parsed = new java.util.LinkedHashMap<>();
        int lineNumber = 0;
        for (String line : rawText.split("\\R")) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            String[] parts = trimmed.split("\\s*,\\s*", -1);
            if (parts.length < 2) {
                errors.add("Line " + lineNumber + ": expected roll,name,room");
                continue;
            }
            try {
                Resident resident = new Resident();
                resident.setRollNumber(roll(parts[0]));
                resident.setName(required(parts[1], "Name", MAX_NAME));
                resident.setRoomNumber(room(parts.length > 2 ? parts[2] : null));
                resident.setContact(optional(parts.length > 3 ? parts[3] : null, MAX_CONTACT));
                parsed.put(resident.getRollNumber(), resident);
            } catch (IllegalArgumentException ex) {
                errors.add("Line " + lineNumber + ": " + ex.getMessage());
            }
        }
        if (!parsed.isEmpty()) {
            residents.saveAll(parsed.values());
        }
        return new ImportResult(parsed.size(), errors);
    }

    public record ImportResult(int imported, List<String> errors) {
    }

    private String roll(String value) {
        String cleaned = required(value, "Roll number", MAX_ROLL);
        if (!cleaned.matches("[A-Za-z0-9]+")) {
            throw new IllegalArgumentException("Roll number can only contain letters and numbers");
        }
        return cleaned.toUpperCase();
    }

    private String room(String value) {
        String cleaned = optional(value, 16);
        if (cleaned != null && !rooms.existsById(cleaned.toUpperCase())) {
            throw new IllegalArgumentException("Unknown room " + cleaned);
        }
        return cleaned == null ? null : cleaned.toUpperCase();
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
