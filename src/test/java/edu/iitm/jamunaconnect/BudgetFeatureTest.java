package edu.iitm.jamunaconnect;

import edu.iitm.jamunaconnect.domain.BudgetItem;
import edu.iitm.jamunaconnect.domain.BudgetPortfolio;
import edu.iitm.jamunaconnect.repository.BudgetItemRepository;
import edu.iitm.jamunaconnect.repository.BudgetPortfolioRepository;
import edu.iitm.jamunaconnect.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Council budget view and office-only maintenance. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class BudgetFeatureTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BudgetService budget;

    @Autowired
    private BudgetPortfolioRepository portfolios;

    @Autowired
    private BudgetItemRepository items;

    @Test
    void budgetPageIsStaffOnly() throws Exception {
        mvc.perform(get("/staff/budget"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/login"));
    }

    @Test
    @WithMockUser(username = "office", roles = "OFFICE_STAFF")
    void officeSeesSeededBudget() throws Exception {
        mvc.perform(get("/staff/budget"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Council budget")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("General Secretary (S1)")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Washing Machine Repair")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("498,000.00")));
    }

    @Test
    @WithMockUser(username = "warden", roles = "WARDEN")
    void wardenCanViewButNotEdit() throws Exception {
        mvc.perform(get("/staff/budget"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Warden")));
        mvc.perform(post("/staff/budget/items").with(csrf())
                        .param("portfolioId", "1")
                        .param("itemName", "Warden should not add this")
                        .param("plannedCost", "1.00"))
                .andExpect(status().isForbidden());
    }

    @Test
    void seededTotalsMatchReference() {
        BudgetService.BudgetView view = budget.view();
        assertThat(view.portfolios()).hasSize(8);
        assertThat(view.totalAllocated()).isEqualByComparingTo("498000.00");
        assertThat(view.totalItems()).isEqualByComparingTo("498000.00");
        assertThat(view.perHeadTotal()).isEqualByComparingTo("498000.00");
        assertThat(view.headroom()).isEqualByComparingTo("0.00");
    }

    @Test
    @WithMockUser(username = "office", roles = "OFFICE_STAFF")
    void officeCanAddAndRemoveBudgetItem() throws Exception {
        BudgetPortfolio portfolio = portfolios.findByCodeIgnoreCase("HL").orElseThrow();
        String name = "Test budget item " + System.nanoTime();
        mvc.perform(post("/staff/budget/items").with(csrf())
                        .param("portfolioId", portfolio.getId().toString())
                        .param("itemName", name)
                        .param("category", "Test")
                        .param("quantity", "1")
                        .param("unitCost", "1234.50")
                        .param("plannedCost", "1234.50")
                        .param("remarks", "Temporary test item")
                        .param("linkUrl", "https://example.iitm.ac.in/budget"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/staff/budget"));

        BudgetItem created = items.findByPortfolioIdOrderByDisplayOrderAscIdAsc(portfolio.getId())
                .stream()
                .filter(item -> name.equals(item.getItemName()))
                .findFirst()
                .orElseThrow();
        assertThat(created.getPlannedCost()).isEqualByComparingTo("1234.50");

        mvc.perform(post("/staff/budget/items/{id}/delete", created.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(items.findById(created.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "office", roles = "OFFICE_STAFF")
    void officeCanUpdateAllocationAndHeader() throws Exception {
        BudgetPortfolio portfolio = portfolios.findByCodeIgnoreCase("HL").orElseThrow();
        BigDecimal original = portfolio.getAllocatedAmount();
        try {
            mvc.perform(post("/staff/budget/portfolios/{id}/allocation", portfolio.getId()).with(csrf())
                            .param("allocatedAmount", "7600.00"))
                    .andExpect(status().is3xxRedirection());
            assertThat(portfolios.findById(portfolio.getId()).orElseThrow().getAllocatedAmount())
                    .isEqualByComparingTo("7600.00");

            mvc.perform(post("/staff/budget/settings").with(csrf())
                            .param("title", "Jamuna Hostel Council Budget")
                            .param("subtitle", "Odd semester")
                            .param("perHeadAmount", "332.00")
                            .param("residentCount", "1500"))
                    .andExpect(status().is3xxRedirection());
        } finally {
            portfolio.setAllocatedAmount(original);
            portfolios.save(portfolio);
        }
    }
}
