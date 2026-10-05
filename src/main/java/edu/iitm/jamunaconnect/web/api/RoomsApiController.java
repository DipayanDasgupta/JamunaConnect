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
import java.util.concurrent.TimeUnit;

/**
 * Module B: the single rooms endpoint shared by search box and map click.
 * Read-only and safe to cache briefly at the edge; the directory changes
 * only when the office shares a new allocation.
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
                .cacheControl(CacheControl.maxAge(120, TimeUnit.SECONDS).cachePublic())
                .body(search.search(query));
    }
}