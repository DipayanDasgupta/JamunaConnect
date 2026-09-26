package edu.iitm.jamunaconnect.web;

import edu.iitm.jamunaconnect.domain.ComplaintStatus;
import edu.iitm.jamunaconnect.service.ComplaintService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Module C: staff dashboard (triage + transitions) and Warden oversight. */
@Controller
@RequestMapping("/staff")
public class StaffController {

    private final ComplaintService complaints;

    public StaffController(ComplaintService complaints) {
        this.complaints = complaints;
    }

    @GetMapping("/login")
    public String login() {
        return "staff/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(name = "status", required = false) ComplaintStatus status,
                            Model model) {
        model.addAttribute("complaints", complaints.list(java.util.Optional.ofNullable(status)));
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("selectedStatus", status);
        return "staff/dashboard";
    }
}