package edu.iitm.jamunaconnect.web.api;

import edu.iitm.jamunaconnect.domain.Room;
import edu.iitm.jamunaconnect.service.RoomSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Module B: the single rooms endpoint shared by search box and map click. */
@RestController
@RequestMapping("/api/rooms")
public class RoomsApiController {

    private final RoomSearchService search;

    public RoomsApiController(RoomSearchService search) {
        this.search = search;
    }

    @GetMapping
    public List<Room> search(@RequestParam(name = "query", required = false) String query) {
        return search.search(query);
    }
}