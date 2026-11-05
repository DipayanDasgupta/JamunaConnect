package edu.iitm.jamunaconnect.web.api;

import edu.iitm.jamunaconnect.service.NoticeService;
import edu.iitm.jamunaconnect.web.dto.NoticeResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** Public read of the office-managed notice board. */
@RestController
@RequestMapping("/api/notices")
public class NoticesApiController {

    private final NoticeService notices;

    public NoticesApiController(NoticeService notices) {
        this.notices = notices;
    }

    @GetMapping
    public ResponseEntity<List<NoticeResponse>> list() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(120, TimeUnit.SECONDS).cachePublic())
                .body(notices.list().stream().map(NoticeResponse::from).toList());
    }
}
