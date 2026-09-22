package edu.iitm.jamunaconnect.notify;

import edu.iitm.jamunaconnect.domain.Complaint;

public interface NotificationService {

    /** Called on file; must never make the request wait on SMTP. */
    void complaintFiled(Complaint complaint);

    /** Called by the daily escalation scan for a stale complaint. */
    void escalate(Complaint complaint);
}