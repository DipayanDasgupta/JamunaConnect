-- V8: privacy-preserving public lookup, random complaint tracking tokens,
-- optional resident email updates, and one-shot escalation notifications.

ALTER TABLE complaint ADD COLUMN tracking_token VARCHAR(32);
ALTER TABLE complaint ADD COLUMN resident_email VARCHAR(128);
ALTER TABLE complaint ADD COLUMN escalated_at TIMESTAMP(6) WITH TIME ZONE;

CREATE UNIQUE INDEX uq_complaint_tracking_token ON complaint (tracking_token);
CREATE INDEX idx_complaint_escalation ON complaint (status, escalated_at, created_at);

-- Fill the directory with values that are actually verified: the hostel office
-- address used for production notifications and the phone numbers already
-- published on the emergency card. Unknown fields stay NULL and render as "-".
UPDATE contact SET name = 'Jamuna Hostel Office', email = 'jamuna@smail.iitm.ac.in'
WHERE role = 'Hostel Office';
UPDATE contact SET phone = '044-22578682' WHERE role = 'Warden';
UPDATE contact SET phone = '044-22578681' WHERE role = 'Hostel Manager';

INSERT INTO contact (role, name, phone, email, display_order) VALUES
    ('Hostel Security Desk', 'Hostel Security Desk', '044 22578680', NULL, 0);

-- One real, non-fabricated notice so the board is not empty on launch.
INSERT INTO notice (title, description, event_date, posted_by, created_at) VALUES
    ('Welcome to JamunaConnect',
     'Contacts, room occupancy, notices and complaint tracking are now in one place. File a complaint from the public form; keep the random tracking code shown after submission and use it with the complaint number to follow the audit timeline.',
     CURRENT_DATE, 'office', CURRENT_TIMESTAMP);
