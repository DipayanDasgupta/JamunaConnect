package edu.iitm.jamunaconnect.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintStatusTest {

    @Test
    void openCanBeAcknowledgedStartedOrForwardedButNotResolvedOrClosed() {
        assertThat(ComplaintStatus.OPEN.canTransitionTo(ComplaintStatus.ACKNOWLEDGED)).isTrue();
        assertThat(ComplaintStatus.OPEN.canTransitionTo(ComplaintStatus.IN_PROGRESS)).isTrue();
        assertThat(ComplaintStatus.OPEN.canTransitionTo(ComplaintStatus.WARDEN_REVIEW)).isTrue();
        assertThat(ComplaintStatus.OPEN.canTransitionTo(ComplaintStatus.RESOLVED)).isFalse();
        assertThat(ComplaintStatus.OPEN.canTransitionTo(ComplaintStatus.CLOSED)).isFalse();
    }

    @Test
    void onlyWardenReviewCanBeClosed() {
        assertThat(ComplaintStatus.WARDEN_REVIEW.canTransitionTo(ComplaintStatus.CLOSED)).isTrue();
        for (ComplaintStatus status : ComplaintStatus.values()) {
            if (status != ComplaintStatus.WARDEN_REVIEW) {
                assertThat(status.canTransitionTo(ComplaintStatus.CLOSED)).isFalse();
            }
        }
    }

    @Test
    void closedIsTerminal() {
        for (ComplaintStatus next : ComplaintStatus.values()) {
            assertThat(ComplaintStatus.CLOSED.canTransitionTo(next)).isFalse();
        }
    }

    @Test
    void fullHappyPathIsLegal() {
        assertThat(ComplaintStatus.OPEN.canTransitionTo(ComplaintStatus.ACKNOWLEDGED)).isTrue();
        assertThat(ComplaintStatus.ACKNOWLEDGED.canTransitionTo(ComplaintStatus.IN_PROGRESS)).isTrue();
        assertThat(ComplaintStatus.IN_PROGRESS.canTransitionTo(ComplaintStatus.RESOLVED)).isTrue();
        assertThat(ComplaintStatus.RESOLVED.canTransitionTo(ComplaintStatus.WARDEN_REVIEW)).isTrue();
        assertThat(ComplaintStatus.WARDEN_REVIEW.canTransitionTo(ComplaintStatus.CLOSED)).isTrue();
    }
}
