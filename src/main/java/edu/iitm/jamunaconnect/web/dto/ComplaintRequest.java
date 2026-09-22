package edu.iitm.jamunaconnect.web.dto;

import edu.iitm.jamunaconnect.domain.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Validated at the API boundary so malformed input never reaches the DB. */
public class ComplaintRequest {

    @NotBlank
    @Size(max = 128)
    private String submitterName;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z]{1,3}[- ]?\\d{2,4}$",
             message = "room must look like a block letter followed by digits, e.g. A101")
    private String roomNumber;

    @NotNull
    private ComplaintCategory category;

    @NotBlank
    @Size(max = 2000)
    private String description;

    /** Optional idempotency key from the browser (double-submit protection). */
    @Size(max = 64)
    private String clientToken;

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public void setCategory(ComplaintCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }
}