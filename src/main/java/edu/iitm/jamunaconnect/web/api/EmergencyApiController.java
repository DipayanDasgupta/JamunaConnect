package edu.iitm.jamunaconnect.web.api;

import edu.iitm.jamunaconnect.domain.FirstAidInfo;
import edu.iitm.jamunaconnect.service.EmergencyService;
import edu.iitm.jamunaconnect.web.dto.EmergencyResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/** Public read of emergency numbers and the first-aid card. */
@RestController
@RequestMapping("/api/emergency")
public class EmergencyApiController {

    private final EmergencyService emergency;

    public EmergencyApiController(EmergencyService emergency) {
        this.emergency = emergency;
    }

    @GetMapping
    public ResponseEntity<EmergencyResponse> get() {
        FirstAidInfo info = emergency.firstAid();
        EmergencyResponse.FirstAidView view = info == null
                ? new EmergencyResponse.FirstAidView(null, null, null)
                : new EmergencyResponse.FirstAidView(
                        info.getContents(), info.getGuidelines(), info.getUpdatedAt());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(300, TimeUnit.SECONDS).cachePublic())
                .body(EmergencyResponse.from(view, emergency.contacts()));
    }
}
