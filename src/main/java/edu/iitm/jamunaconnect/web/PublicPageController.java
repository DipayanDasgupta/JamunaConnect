package edu.iitm.jamunaconnect.web;

import edu.iitm.jamunaconnect.domain.ComplaintCategory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Module A: public pages. Read-only; no login required for any of these. */
@Controller
public class PublicPageController {

    @GetMapping("/")
    public String home() {
        return "pages/home";
    }

    @GetMapping("/contacts")
    public String contacts() {
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

    @GetMapping("/complaints/new")
    public String complaintForm(Model model) {
        model.addAttribute("categories", ComplaintCategory.values());
        return "pages/complaint-form";
    }
}