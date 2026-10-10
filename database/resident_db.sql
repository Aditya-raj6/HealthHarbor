-- resident_db : resident profiles + visit history
CREATE DATABASE IF NOT EXISTS resident_db;
USE resident_db;

DROP TABLE IF EXISTS visits;
DROP TABLE IF EXISTS residents;

CREATE TABLE residents (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    age           INT,
    gender        VARCHAR(10),
    village       VARCHAR(100),
    phone         VARCHAR(15),
    blood_group   VARCHAR(5),
    medical_notes VARCHAR(255),          -- e.g. allergies, known conditions
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE visits (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    resident_id      INT NOT NULL,
    health_centre_id INT,                -- id from healthcentre_db (other service)
    visit_date       DATE NOT NULL,
    reason           VARCHAR(255),
    diagnosis        VARCHAR(255),
    treatment        VARCHAR(255),
    FOREIGN KEY (resident_id) REFERENCES residents(id)
);

INSERT INTO residents (name, age, gender, village, phone, blood_group, medical_notes) VALUES
('Ramesh Kumar', 45, 'Male',   'Rampur',  '9876500001', 'B+',  'High blood pressure'),
('Lakshmi Devi', 32, 'Female', 'Rampur',  '9876500002', 'O+',  'None'),
('Suresh Yadav', 60, 'Male',   'Sonpur',  '9876500003', 'A+',  'Diabetic'),
('Anita Kumari', 8,  'Female', 'Sonpur',  '9876500004', 'AB+', 'Allergic to penicillin'),
('Mohan Lal',    52, 'Male',   'Rampur',  '9876500005', 'O-',  'None');

INSERT INTO visits (resident_id, health_centre_id, visit_date, reason, diagnosis, treatment) VALUES
(1, 1, '2026-09-20', 'Headache and dizziness', 'High blood pressure', 'BP tablets for 30 days'),
(2, 1, '2026-09-25', 'Fever',                  'Viral fever',         'Paracetamol, rest'),
(3, 2, '2026-10-01', 'Routine sugar check',    'Diabetes - controlled','Continue medicines'),
(4, 2, '2026-10-05', 'Stomach pain',           'Water-borne infection','ORS and antibiotics');
