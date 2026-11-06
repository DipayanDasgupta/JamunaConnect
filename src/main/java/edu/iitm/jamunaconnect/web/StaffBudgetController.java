package edu.iitm.jamunaconnect.web;

import edu.iitm.jamunaconnect.service.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/** Council budget: all staff can view, office staff can maintain. */
@Controller
@RequestMapping("/staff/budget")
public class StaffBudgetController {

    private final BudgetService budget;

    public StaffBudgetController(BudgetService budget) {
        this.budget = budget;
    }

    @GetMapping
    public String budget(Model model, Authentication authentication) {
        model.addAttribute("budget", budget.view());
        model.addAttribute("canEdit", isOffice(authentication));
        return "staff/budget";
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam String title,
                               @RequestParam(required = false) String subtitle,
                               @RequestParam(required = false) BigDecimal perHeadAmount,
                               @RequestParam(required = false) Integer residentCount,
                               Authentication authentication,
                               RedirectAttributes redirect) {
        requireOffice(authentication);
        try {
            budget.saveSetting(title, subtitle, perHeadAmount, residentCount,
                    authentication.getName());
            redirect.addFlashAttribute("message", "Budget summary updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/budget";
    }

    @PostMapping("/portfolios")
    public String addPortfolio(@RequestParam String code,
                               @RequestParam String name,
                               @RequestParam(required = false) BigDecimal allocatedAmount,
                               @RequestParam(required = false) Integer displayOrder,
                               @RequestParam(required = false) String notes,
                               Authentication authentication,
                               RedirectAttributes redirect) {
        requireOffice(authentication);
        try {
            budget.addPortfolio(code, name, allocatedAmount, displayOrder, notes);
            redirect.addFlashAttribute("message", "Portfolio added.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/budget";
    }

    @PostMapping("/portfolios/{id}/allocation")
    public String updateAllocation(@PathVariable Long id,
                                   @RequestParam BigDecimal allocatedAmount,
                                   Authentication authentication,
                                   RedirectAttributes redirect) {
        requireOffice(authentication);
        try {
            budget.updateAllocation(id, allocatedAmount);
            redirect.addFlashAttribute("message", "Allocation updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/budget";
    }

    @PostMapping("/items")
    public String addItem(@RequestParam(required = false) Long id,
                          @RequestParam Long portfolioId,
                          @RequestParam String itemName,
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) String quantity,
                          @RequestParam(required = false) BigDecimal unitCost,
                          @RequestParam BigDecimal plannedCost,
                          @RequestParam(required = false) String remarks,
                          @RequestParam(required = false) String linkUrl,
                          Authentication authentication,
                          RedirectAttributes redirect) {
        requireOffice(authentication);
        try {
            budget.saveItem(id, portfolioId, itemName, category, quantity, unitCost,
                    plannedCost, remarks, linkUrl);
            redirect.addFlashAttribute("message", "Budget item saved.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/budget";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id, Authentication authentication,
                             RedirectAttributes redirect) {
        requireOffice(authentication);
        try {
            budget.deleteItem(id);
            redirect.addFlashAttribute("message", "Budget item removed.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/budget";
    }

    private static boolean isOffice(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_OFFICE_STAFF".equals(authority.getAuthority()));
    }

    private static void requireOffice(Authentication authentication) {
        if (!isOffice(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only office staff can edit the budget");
        }
    }
}
