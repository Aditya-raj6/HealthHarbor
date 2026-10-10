-- camp_db : health camps, staff, beneficiaries (screening + follow-up)
CREATE DATABASE IF NOT EXISTS camp_db;
USE camp_db;

DROP TABLE IF EXISTS camp_beneficiaries;
DROP TABLE IF EXISTS camp_staff;
DROP TABLE IF EXISTS camps;

CREATE TABLE camps (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    village     VARCHAR(100),
    camp_date   DATE NOT NULL,
    centre_id   INT,                     -- id from healthcentre_db (other service)
    description VARCHAR(255),
    status      VARCHAR(20) DEFAULT 'PLANNED'   -- PLANNED / COMPLETED / CANCELLED
);

CREATE TABLE camp_staff (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    camp_id    INT NOT NULL,
    staff_name VARCHAR(100) NOT NULL,
    role       VARCHAR(50),
    FOREIGN KEY (camp_id) REFERENCES camps(id)
);

CREATE TABLE camp_beneficiaries (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    camp_id          INT NOT NULL,
    resident_id      INT,                -- id from resident_db (other service)
    resident_name    VARCHAR(100),
    screening_type   VARCHAR(100),       -- BP, Sugar, Eye, Anaemia ...
    result           VARCHAR(255),
    follow_up_needed BOOLEAN DEFAULT FALSE,
    follow_up_date   DATE,
    follow_up_status VARCHAR(20),        -- PENDING / DONE
    FOREIGN KEY (camp_id) REFERENCES camps(id)
);

INSERT INTO camps (name, village, camp_date, centre_id, description, status) VALUES
('Diabetes & BP Screening Camp', 'Sonpur', '2026-10-03', 2, 'Free sugar and BP checkup',        'COMPLETED'),
('Child Health & Vaccination Camp', 'Rampur', '2026-10-18', 1, 'Vaccination and growth checks', 'PLANNED');

INSERT INTO camp_staff (camp_id, staff_name, role) VALUES
(1, 'Dr. Arun Singh', 'Doctor'),
(1, 'Kavita Rani',    'ANM'),
(2, 'Dr. Meera Sharma','Doctor'),
(2, 'Sunita Verma',   'Nurse');

INSERT INTO camp_beneficiaries
(camp_id, resident_id, resident_name, screening_type, result, follow_up_needed, follow_up_date, follow_up_status) VALUES
(1, 3, 'Suresh Yadav', 'Blood sugar', 'High - 210 mg/dL', TRUE,  '2026-10-15', 'PENDING'),
(1, 5, 'Mohan Lal',    'Blood pressure', 'Normal',         FALSE, NULL,         NULL);
