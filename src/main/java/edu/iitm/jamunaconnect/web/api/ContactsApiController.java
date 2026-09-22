package edu.iitm.jamunaconnect.web.api;

import edu.iitm.jamunaconnect.domain.Contact;
import edu.iitm.jamunaconnect.repository.ContactRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Module E: serves the editable contact directory. */
@RestController
@RequestMapping("/api/contacts")
public class ContactsApiController {

    private final ContactRepository contacts;

    public ContactsApiController(ContactRepository contacts) {
        this.contacts = contacts;
    }

    @GetMapping
    public List<Contact> list() {
        return contacts.findAllByOrderByDisplayOrderAsc();
    }
}