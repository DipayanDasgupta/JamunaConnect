-- V2: seed data. Rooms are the redacted/dummy allocation list agreed with the
-- stakeholder until the office shares a real one; no real personal data.
-- Staff hashes below are dev-only and must be rotated before any deployment.

INSERT INTO room (room_number, block, floor, occupants) VALUES
    ('A101', 'A', 1, 'Rahul Verma'),
    ('A102', 'A', 1, 'Arjun Nair, Karthik Raja'),
    ('A201', 'A', 2, 'Vikram Shah'),
    ('B101', 'B', 1, 'Aditya Menon'),
    ('B102', 'B', 1, 'Surya Prasad, Nikhil Reddy'),
    ('B203', 'B', 2, 'Farhan Ali'),
    ('C301', 'C', 3, 'Rohan Gupta'),
    ('C302', 'C', 3, 'Meera Iyer');

INSERT INTO resident (roll_number, name, room_number, contact) VALUES
    ('CE24B059', 'Dipayan Dasgupta', 'C301', '+91-8928315649'),
    ('CS26M036', 'S. Saathvik', 'C302', '+91-9000000000'),
    ('ME24B001', 'Rahul Verma', 'A101', '+91-9000000001'),
    ('CE24B044', 'Arjun Nair', 'A102', '+91-9000000002'),
    ('CS24B012', 'Karthik Raja', 'A102', '+91-9000000003');

INSERT INTO contact (role, name, phone, email, display_order) VALUES
    ('Warden',              'Warden Godavari',       '+91-44-2257-8000', 'wardengodavari@example.iitm.ac.in', 1),
    ('General Secretary',   'Nizammuddin',          '+91-9000000010',   'jamunagensec@example.iitm.ac.in',   2),
    ('Hostel Office',       'Jamuna Office',        '+91-44-2257-8001', 'jamuna-office@example.iitm.ac.in',  3),
    ('Maintenance',         'Estate Maintenance',   '+91-44-2257-8002', 'maintenance@example.iitm.ac.in',    4);

-- Dev credentials: office / office123, warden / warden123 (BCrypt cost 10).
INSERT INTO staff_user (username, password_hash, role, display_name) VALUES
    ('office', '$2b$10$8P.m3tuiX6b70uoeAeWhG.WAl812KR8NgPt.hm0Usntxgw6KVFZoO', 'OFFICE_STAFF', 'Hostel Office'),
    ('warden', '$2b$10$qp.yR0Zgmxnq8TQQV2ph1OqZPSqncOhcdYz2ntbMVwRr7sPLqt0Tu', 'WARDEN', 'Warden');