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
    CLOSED;

    /** Allowed next states from this one. Any non-closed state can be closed directly. */
    public boolean canTransitionTo(ComplaintStatus next) {
        return switch (this) {
            case OPEN -> next == ACKNOWLEDGED || next == IN_PROGRESS || next == CLOSED;
            case ACKNOWLEDGED -> next == IN_PROGRESS || next == RESOLVED || next == CLOSED;
            case IN_PROGRESS -> next == RESOLVED || next == CLOSED;
            case RESOLVED -> next == CLOSED;
            case CLOSED -> false;
        };
    }
}