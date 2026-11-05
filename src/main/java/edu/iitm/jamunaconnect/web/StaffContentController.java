package edu.iitm.jamunaconnect.web;

import edu.iitm.jamunaconnect.service.EmergencyService;
import edu.iitm.jamunaconnect.service.NoticeService;
import edu.iitm.jamunaconnect.service.ResidentAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Staff management for notices, emergency information and the resident directory. */
@Controller
@RequestMapping("/staff")
public class StaffContentController {

    private final NoticeService notices;
    private final EmergencyService emergency;
    private final ResidentAdminService residents;

    public StaffContentController(NoticeService notices, EmergencyService emergency,
                                  ResidentAdminService residents) {
        this.notices = notices;
        this.emergency = emergency;
        this.residents = residents;
    }

    // ---------- notices ----------

    @GetMapping("/notices")
    public String notices(Model model) {
        model.addAttribute("notices", notices.list());
        return "staff/notices";
    }

    @PostMapping("/notices")
    public String addNotice(@RequestParam String title,
                            @RequestParam String description,
                            @RequestParam(required = false) String eventDate,
                            @RequestParam(required = false) String linkUrl,
                            @RequestParam(required = false) String imageUrl,
                            Principal principal,
                            RedirectAttributes redirect) {
        try {
            notices.create(title, description, parseDate(eventDate), linkUrl, imageUrl,
                    principal == null ? "office" : principal.getName());
            redirect.addFlashAttribute("message", "Notice posted.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/notices";
    }

    @PostMapping("/notices/{id}/delete")
    public String deleteNotice(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            notices.delete(id);
            redirect.addFlashAttribute("message", "Notice removed.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/notices";
    }

    // ---------- emergency ----------

    @GetMapping("/emergency")
    public String emergency(Model model) {
        model.addAttribute("firstAid", emergency.firstAid());
        model.addAttribute("contacts", emergency.contacts());
        return "staff/emergency";
    }

    @PostMapping("/emergency/first-aid")
    public String updateFirstAid(@RequestParam(required = false) String contents,
                                 @RequestParam(required = false) String guidelines,
                                 RedirectAttributes redirect) {
        try {
            emergency.updateFirstAid(contents, guidelines);
            redirect.addFlashAttribute("message", "First-aid card updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/emergency";
    }

    @PostMapping("/emergency/contacts")
    public String addEmergencyContact(@RequestParam String roleTitle,
                                      @RequestParam String phone,
                                      RedirectAttributes redirect) {
        try {
            emergency.addContact(roleTitle, phone);
            redirect.addFlashAttribute("message", "Emergency contact added.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/emergency";
    }

    @PostMapping("/emergency/contacts/{id}/delete")
    public String deleteEmergencyContact(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            emergency.deleteContact(id);
            redirect.addFlashAttribute("message", "Emergency contact removed.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/emergency";
    }

    // ---------- residents ----------

    @GetMapping("/residents")
    public String residents(Model model) {
        model.addAttribute("residents", residents.list());
        return "staff/residents";
    }

    @PostMapping("/residents")
    public String saveResident(@RequestParam String rollNumber,
                               @RequestParam String name,
                               @RequestParam(required = false) String roomNumber,
                               @RequestParam(required = false) String contact,
                               RedirectAttributes redirect) {
        try {
            residents.save(rollNumber, name, roomNumber, contact);
            redirect.addFlashAttribute("message", "Resident saved.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/residents";
    }

    @PostMapping("/residents/bulk")
    public String bulkImport(@RequestParam(required = false) String rawText,
                             RedirectAttributes redirect) {
        ResidentAdminService.ImportResult result = residents.bulkImport(rawText);
        if (result.imported() > 0) {
            redirect.addFlashAttribute("message", "Imported " + result.imported() + " resident(s).");
        }
        if (!result.errors().isEmpty()) {
            redirect.addFlashAttribute("error", String.join("; ", result.errors()));
        }
        return "redirect:/staff/residents";
    }

    @PostMapping("/residents/{rollNumber}/delete")
    public String deleteResident(@PathVariable String rollNumber, RedirectAttributes redirect) {
        try {
            residents.delete(rollNumber);
            redirect.addFlashAttribute("message", "Resident removed.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/residents";
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Event date is required");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Event date must be a valid date");
        }
    }
}
