package edu.iitm.jamunaconnect.domain;

/**
 * Complaint lifecycle. Transitions are one-way and are recorded in
 * {@link ComplaintStatusHistory}; the current value is just a cache.
 */
public enum ComplaintStatus {
    OPEN,
    ACKNOWLEDGED,
    IN_PROGRESS,
    RESOLVED,
    WARDEN_REVIEW,
    CLOSED;

    /**
     * Allowed next states from this one. Office staff move work forward and
     * forward it to the Warden; only a Warden can close a forwarded complaint.
     */
    public boolean canTransitionTo(ComplaintStatus next) {
        return switch (this) {
            case OPEN -> next == ACKNOWLEDGED || next == IN_PROGRESS || next == WARDEN_REVIEW;
            case ACKNOWLEDGED -> next == IN_PROGRESS || next == RESOLVED || next == WARDEN_REVIEW;
            case IN_PROGRESS -> next == RESOLVED || next == WARDEN_REVIEW;
            case RESOLVED -> next == WARDEN_REVIEW;
            case WARDEN_REVIEW -> next == CLOSED;
            case CLOSED -> false;
        };
    }
}