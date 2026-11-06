package edu.iitm.jamunaconnect.service;

import edu.iitm.jamunaconnect.domain.BudgetItem;
import edu.iitm.jamunaconnect.domain.BudgetPortfolio;
import edu.iitm.jamunaconnect.domain.BudgetSetting;
import edu.iitm.jamunaconnect.repository.BudgetItemRepository;
import edu.iitm.jamunaconnect.repository.BudgetPortfolioRepository;
import edu.iitm.jamunaconnect.repository.BudgetSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Staff budget view and office-only maintenance. */
@Service
public class BudgetService {

    private final BudgetSettingRepository settings;
    private final BudgetPortfolioRepository portfolios;
    private final BudgetItemRepository items;

    public BudgetService(BudgetSettingRepository settings, BudgetPortfolioRepository portfolios,
                         BudgetItemRepository items) {
        this.settings = settings;
        this.portfolios = portfolios;
        this.items = items;
    }

    @Transactional(readOnly = true)
    public BudgetView view() {
        BudgetSetting setting = settings.findById(1).orElse(null);
        List<PortfolioView> views = new ArrayList<>();
        BigDecimal totalAllocated = zero();
        BigDecimal totalItems = zero();
        for (BudgetPortfolio portfolio : portfolios.findAllByOrderByDisplayOrderAscIdAsc()) {
            List<BudgetItem> portfolioItems =
                    items.findByPortfolioIdOrderByDisplayOrderAscIdAsc(portfolio.getId());
            BigDecimal itemTotal = portfolioItems.stream()
                    .map(BudgetItem::getPlannedCost)
                    .map(BudgetService::nz)
                    .reduce(zero(), BigDecimal::add);
            BigDecimal allocated = nz(portfolio.getAllocatedAmount());
            views.add(new PortfolioView(portfolio, portfolioItems, itemTotal,
                    allocated.subtract(itemTotal)));
            totalAllocated = totalAllocated.add(allocated);
            totalItems = totalItems.add(itemTotal);
        }
        BigDecimal perHeadTotal = zero();
        if (setting != null) {
            perHeadTotal = nz(setting.getPerHeadAmount())
                    .multiply(BigDecimal.valueOf(setting.getResidentCount() == null
                            ? 0 : setting.getResidentCount()));
        }
        return new BudgetView(setting, views, totalAllocated, totalItems, perHeadTotal,
                totalAllocated.subtract(totalItems));
    }

    @Transactional
    public void saveSetting(String title, String subtitle, BigDecimal perHeadAmount,
                            Integer residentCount, String updatedBy) {
        BudgetSetting setting = settings.findById(1).orElseGet(() -> {
            BudgetSetting fresh = new BudgetSetting();
            fresh.setId(1);
            return fresh;
        });
        setting.setTitle(required(title, "Title", 160));
        setting.setSubtitle(optional(subtitle, 160));
        setting.setPerHeadAmount(money(perHeadAmount, "Per-head amount"));
        setting.setResidentCount(count(residentCount));
        setting.setUpdatedAt(Instant.now());
        setting.setUpdatedBy(required(updatedBy, "Updated by", 64));
        settings.save(setting);
    }

    @Transactional
    public BudgetPortfolio addPortfolio(String code, String name, BigDecimal allocatedAmount,
                                        Integer displayOrder, String notes) {
        return savePortfolio(null, code, name, allocatedAmount, displayOrder, notes);
    }

    @Transactional
    public BudgetPortfolio savePortfolio(Long id, String code, String name,
                                         BigDecimal allocatedAmount, Integer displayOrder,
                                         String notes) {
        String cleanCode = required(code, "Code", 32);
        if (!cleanCode.matches("[A-Za-z0-9 _-]+")) {
            throw new IllegalArgumentException("Code can only contain letters, numbers, spaces, _ or -");
        }
        BudgetPortfolio portfolio = id == null
                ? new BudgetPortfolio()
                : portfolios.findById(id).orElseThrow(() ->
                        new IllegalArgumentException("No portfolio " + id));
        portfolios.findByCodeIgnoreCase(cleanCode)
                .filter(existing -> !existing.getId().equals(portfolio.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Portfolio code already exists");
                });
        portfolio.setCode(cleanCode.toUpperCase());
        portfolio.setName(required(name, "Name", 160));
        portfolio.setAllocatedAmount(money(allocatedAmount, "Allocated amount"));
        portfolio.setDisplayOrder(displayOrder == null ? 0 : displayOrder);
        portfolio.setNotes(optional(notes, 1000));
        return portfolios.save(portfolio);
    }

    @Transactional
    public void updateAllocation(Long id, BigDecimal allocatedAmount) {
        BudgetPortfolio portfolio = portfolios.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No portfolio " + id));
        portfolio.setAllocatedAmount(money(allocatedAmount, "Allocated amount"));
        portfolios.save(portfolio);
    }

    @Transactional
    public void deletePortfolio(Long id) {
        if (!portfolios.existsById(id)) {
            throw new IllegalArgumentException("No portfolio " + id);
        }
        items.deleteByPortfolioId(id);
        portfolios.deleteById(id);
    }

    @Transactional
    public BudgetItem saveItem(Long id, Long portfolioId, String itemName, String category,
                               String quantity, BigDecimal unitCost, BigDecimal plannedCost,
                               String remarks, String linkUrl) {
        if (portfolioId == null || !portfolios.existsById(portfolioId)) {
            throw new IllegalArgumentException("Choose a portfolio");
        }
        BudgetItem item = id == null
                ? new BudgetItem()
                : items.findById(id).orElseThrow(() ->
                        new IllegalArgumentException("No budget item " + id));
        item.setPortfolioId(portfolioId);
        item.setItemName(required(itemName, "Item", 240));
        item.setCategory(optional(category, 80));
        item.setQuantity(optional(quantity, 80));
        item.setUnitCost(unitCost == null ? null : money(unitCost, "Unit cost"));
        item.setPlannedCost(money(plannedCost, "Planned cost"));
        item.setRemarks(optional(remarks, 1000));
        item.setLinkUrl(httpUrl(linkUrl));
        if (item.getId() == null) {
            item.setDisplayOrder(nextOrder(portfolioId));
        }
        return items.save(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!items.existsById(id)) {
            throw new IllegalArgumentException("No budget item " + id);
        }
        items.deleteById(id);
    }

    private int nextOrder(Long portfolioId) {
        return items.findByPortfolioIdOrderByDisplayOrderAscIdAsc(portfolioId).stream()
                .map(BudgetItem::getDisplayOrder)
                .filter(order -> order != null)
                .max(Integer::compareTo)
                .map(order -> order + 10)
                .orElse(10);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? zero() : value;
    }

    private static BigDecimal money(BigDecimal value, String field) {
        BigDecimal clean = value == null ? zero() : value.setScale(2, RoundingMode.HALF_UP);
        if (clean.signum() < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
        return clean;
    }

    private static int count(Integer value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Resident count cannot be negative");
        }
        return value;
    }

    private static String required(String value, String field, int max) {
        String cleaned = optional(value, max);
        if (cleaned == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return cleaned;
    }

    private static String optional(String value, int max) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        if (cleaned.length() > max) {
            throw new IllegalArgumentException("Value is too long (max " + max + ")");
        }
        return cleaned;
    }

    private static String httpUrl(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            URI uri = URI.create(cleaned);
            if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
                return cleaned;
            }
        } catch (IllegalArgumentException ignored) {
            // fall through to the friendly error below
        }
        throw new IllegalArgumentException("Link must start with http:// or https://");
    }

    public record PortfolioView(BudgetPortfolio portfolio, List<BudgetItem> items,
                                BigDecimal itemTotal, BigDecimal balance) {
    }

    public record BudgetView(BudgetSetting setting, List<PortfolioView> portfolios,
                             BigDecimal totalAllocated, BigDecimal totalItems,
                             BigDecimal perHeadTotal, BigDecimal headroom) {
    }
}
