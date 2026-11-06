package edu.iitm.jamunaconnect.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/** Singleton header for the council budget page. */
@Entity
@Table(name = "budget_setting")
public class BudgetSetting {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", length = 160, nullable = false)
    private String title;

    @Column(name = "subtitle", length = 160)
    private String subtitle;

    @Column(name = "per_head_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal perHeadAmount;

    @Column(name = "resident_count", nullable = false)
    private Integer residentCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", length = 64, nullable = false)
    private String updatedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public BigDecimal getPerHeadAmount() {
        return perHeadAmount;
    }

    public void setPerHeadAmount(BigDecimal perHeadAmount) {
        this.perHeadAmount = perHeadAmount;
    }

    public Integer getResidentCount() {
        return residentCount;
    }

    public void setResidentCount(Integer residentCount) {
        this.residentCount = residentCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
