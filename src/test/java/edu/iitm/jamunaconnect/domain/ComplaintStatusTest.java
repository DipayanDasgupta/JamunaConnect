package edu.iitm.jamunaconnect.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintStatusTest {

    @Test
    void openCanBeAcknowledgedOrStartedButNotResolvedDirectly() {
        assertThat(ComplaintStatus.OPEN.canTransitionTo(ComplaintStatus.ACKNOWLEDGED)).isTrue();
        assertThat(ComplaintStatus.OPEN.canTransitionTo(ComplaintStatus.IN_PROGRESS)).isTrue();
        assertThat(ComplaintStatus.OPEN.canTransitionTo(ComplaintStatus.RESOLVED)).isFalse();
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
        assertThat(ComplaintStatus.RESOLVED.canTransitionTo(ComplaintStatus.CLOSED)).isTrue();
    }
}