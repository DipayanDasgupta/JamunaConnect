package edu.iitm.jamunaconnect.service;

import edu.iitm.jamunaconnect.domain.Resident;
import edu.iitm.jamunaconnect.domain.Room;
import edu.iitm.jamunaconnect.repository.ResidentRepository;
import edu.iitm.jamunaconnect.repository.RoomRepository;
import edu.iitm.jamunaconnect.web.dto.RoomResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Module B: one fuzzy lookup endpoint, shared by the search box and the map
 * popup, so the two never drift apart. Residents are fetched in a single
 * query and grouped, so roll numbers come along with every room.
 */
@Service
public class RoomSearchService {

    private final RoomRepository rooms;
    private final ResidentRepository residents;
    private final boolean fuzzy;

    public RoomSearchService(RoomRepository rooms,
                             ResidentRepository residents,
                             @Value("${app.search.fuzzy:true}") boolean fuzzy) {
        this.rooms = rooms;
        this.residents = residents;
        this.fuzzy = fuzzy;
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> search(String query) {
        List<Room> found;
        if (query == null || query.isBlank()) {
            found = rooms.findAll();
        } else {
            String cleaned = query.trim();
            found = fuzzy ? rooms.searchFuzzy(cleaned) : rooms.searchLike(cleaned);
        }
        if (found.isEmpty()) {
            return List.of();
        }
        List<String> numbers = found.stream().map(Room::getRoomNumber).toList();
        Map<String, List<Resident>> byRoom = residents.findByRoomNumberIn(numbers).stream()
                .collect(Collectors.groupingBy(Resident::getRoomNumber));
        return found.stream()
                .map(r -> new RoomResponse(
                        r.getRoomNumber(), r.getBlock(), r.getFloor(),
                        byRoom.getOrDefault(r.getRoomNumber(), List.of()).stream()
                                .map(res -> new RoomResponse.ResidentView(
                                        res.getRollNumber(), res.getName()))
                                .toList()))
                .toList();
    }
}