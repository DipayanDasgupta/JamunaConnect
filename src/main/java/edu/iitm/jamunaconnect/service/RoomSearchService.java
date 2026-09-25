package edu.iitm.jamunaconnect.service;

import edu.iitm.jamunaconnect.domain.Room;
import edu.iitm.jamunaconnect.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Module B: one fuzzy lookup endpoint, shared by the search box and the map
 * popup, so the two never drift apart.
 */
@Service
public class RoomSearchService {

    private final RoomRepository rooms;
    private final boolean fuzzy;

    public RoomSearchService(RoomRepository rooms,
                             @Value("${app.search.fuzzy:true}") boolean fuzzy) {
        this.rooms = rooms;
        this.fuzzy = fuzzy;
    }

    @Transactional(readOnly = true)
    public List<Room> search(String query) {
        if (query == null || query.isBlank()) {
            return rooms.findAll();
        }
        String cleaned = query.trim();
        return fuzzy ? rooms.searchFuzzy(cleaned) : rooms.searchLike(cleaned);
    }
}