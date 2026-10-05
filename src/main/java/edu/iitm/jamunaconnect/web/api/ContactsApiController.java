package edu.iitm.jamunaconnect.web.api;

import edu.iitm.jamunaconnect.domain.Contact;
import edu.iitm.jamunaconnect.repository.ContactRepository;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** Module E: serves the editable contact directory. */
@RestController
@RequestMapping("/api/contacts")
public class ContactsApiController {

    private final ContactRepository contacts;

    public ContactsApiController(ContactRepository contacts) {
        this.contacts = contacts;
    }

    @GetMapping
    public ResponseEntity<List<Contact>> list() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(300, TimeUnit.SECONDS).cachePublic())
                .body(contacts.findAllByOrderByDisplayOrderAsc());
    }
}