-- V5: drop unverified placeholder phones/emails from the V2 seed.
-- Only the IITM Elec Maintenance line (shared vCard in the hostel group) is kept.
-- The office fills in real details; the directory renders blanks as "-".
UPDATE contact SET phone = NULL, email = NULL
WHERE role IN ('Warden', 'General Secretary', 'Hostel Office', 'Maintenance');
