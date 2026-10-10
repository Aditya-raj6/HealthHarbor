-- healthcentre_db : centres, workers, appointments, consultations
CREATE DATABASE IF NOT EXISTS healthcentre_db;
USE healthcentre_db;

DROP TABLE IF EXISTS consultations;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS workers;
DROP TABLE IF EXISTS health_centres;

CREATE TABLE health_centres (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    village      VARCHAR(100),
    facilities   VARCHAR(255),           -- e.g. "OPD, Lab, Pharmacy"
    open_time    TIME,
    close_time   TIME,
    working_days VARCHAR(50)             -- e.g. "Mon-Sat"
);

CREATE TABLE workers (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    centre_id INT NOT NULL,
    name      VARCHAR(100) NOT NULL,
    role      VARCHAR(50),               -- Doctor, Nurse, ANM, ASHA ...
    phone     VARCHAR(15),
    FOREIGN KEY (centre_id) REFERENCES health_centres(id)
);

CREATE TABLE appointments (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    resident_id      INT NOT NULL,       -- id from resident_db (other service)
    centre_id        INT NOT NULL,
    worker_id        INT,
    appointment_date DATE NOT NULL,
    appointment_time TIME,
    reason           VARCHAR(255),
    status           VARCHAR(20) DEFAULT 'SCHEDULED',   -- SCHEDULED / COMPLETED / CANCELLED
    FOREIGN KEY (centre_id) REFERENCES health_centres(id),
    FOREIGN KEY (worker_id) REFERENCES workers(id)
);

CREATE TABLE consultations (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NOT NULL,
    symptoms       VARCHAR(255),
    diagnosis      VARCHAR(255),
    treatment      VARCHAR(255),
    consult_date   DATE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

INSERT INTO health_centres (name, village, facilities, open_time, close_time, working_days) VALUES
('Rampur Primary Health Centre', 'Rampur', 'OPD, Pharmacy, Vaccination', '09:00:00', '17:00:00', 'Mon-Sat'),
('Sonpur Sub-Centre',            'Sonpur', 'OPD, Lab, Pharmacy',         '08:00:00', '14:00:00', 'Mon-Fri');

INSERT INTO workers (centre_id, name, role, phone) VALUES
(1, 'Dr. Meera Sharma', 'Doctor', '9800000001'),
(1, 'Sunita Verma',     'Nurse',  '9800000002'),
(2, 'Dr. Arun Singh',   'Doctor', '9800000003'),
(2, 'Kavita Rani',      'ANM',    '9800000004');

INSERT INTO appointments (resident_id, centre_id, worker_id, appointment_date, appointment_time, reason, status) VALUES
(1, 1, 1, '2026-09-20', '10:00:00', 'Headache and dizziness', 'COMPLETED'),
(2, 1, 1, '2026-09-25', '11:00:00', 'Fever',                  'COMPLETED'),
(3, 2, 3, '2026-10-01', '09:00:00', 'Sugar check',            'COMPLETED'),
(5, 1, 1, '2026-10-12', '10:30:00', 'General checkup',        'SCHEDULED'),
(4, 2, 3, '2026-10-13', '09:30:00', 'Follow-up for stomach infection', 'SCHEDULED');

INSERT INTO consultations (appointment_id, symptoms, diagnosis, treatment, consult_date) VALUES
(1, 'Headache, dizziness', 'High blood pressure',  'BP tablets for 30 days', '2026-09-20'),
(2, 'Fever, body ache',    'Viral fever',          'Paracetamol, rest',      '2026-09-25'),
(3, 'None',                'Diabetes - controlled','Continue medicines',     '2026-10-01');
