package edu.iitm.jamunaconnect.web.api;

import edu.iitm.jamunaconnect.service.RoomSearchService;
import edu.iitm.jamunaconnect.web.dto.RoomResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Module B: the single rooms endpoint shared by search box and map click.
 * The directory is public by product decision, so responses carry resident
 * names and roll numbers; they must never be cached by a shared CDN.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomsApiController {

    private final RoomSearchService search;

    public RoomsApiController(RoomSearchService search) {
        this.search = search;
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> search(
            @RequestParam(name = "query", required = false) String query) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(search.search(query));
    }
}