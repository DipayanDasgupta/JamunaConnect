package edu.iitm.jamunaconnect.web;

import edu.iitm.jamunaconnect.domain.ComplaintCategory;
import edu.iitm.jamunaconnect.repository.ContactRepository;
import edu.iitm.jamunaconnect.service.EmergencyService;
import edu.iitm.jamunaconnect.service.NoticeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Module A: public pages. Read-only; no login required for any of these. */
@Controller
public class PublicPageController {

    private final ContactRepository contacts;
    private final NoticeService notices;
    private final EmergencyService emergency;

    public PublicPageController(ContactRepository contacts, NoticeService notices,
                                EmergencyService emergency) {
        this.contacts = contacts;
        this.notices = notices;
        this.emergency = emergency;
    }

    @GetMapping("/")
    public String home() {
        return "pages/home";
    }

    @GetMapping("/contacts")
    public String contacts(Model model) {
        model.addAttribute("contacts", contacts.findAllByOrderByDisplayOrderAsc());
        return "pages/contacts";
    }

    @GetMapping("/lookup")
    public String lookup() {
        return "pages/lookup";
    }

    @GetMapping("/map")
    public String map() {
        return "pages/map";
    }

    @GetMapping("/notices")
    public String notices(Model model) {
        model.addAttribute("notices", notices.list());
        return "pages/notices";
    }

    @GetMapping("/emergency")
    public String emergency(Model model) {
        model.addAttribute("firstAid", emergency.firstAid());
        model.addAttribute("emergencyContacts", emergency.contacts());
        return "pages/emergency";
    }

    @GetMapping("/wifi")
    public String wifi() {
        return "pages/wifi";
    }

    @GetMapping("/complaints/new")
    public String complaintForm(Model model) {
        model.addAttribute("categories", ComplaintCategory.values());
        return "pages/complaint-form";
    }

    @GetMapping("/complaints/track")
    public String trackComplaint() {
        return "pages/track";
    }
}