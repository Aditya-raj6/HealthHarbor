-- sanitation_db : facilities, complaints, maintenance, water records
CREATE DATABASE IF NOT EXISTS sanitation_db;
USE sanitation_db;

DROP TABLE IF EXISTS water_records;
DROP TABLE IF EXISTS maintenance_records;
DROP TABLE IF EXISTS complaints;
DROP TABLE IF EXISTS sanitation_facilities;

CREATE TABLE sanitation_facilities (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    type               VARCHAR(50),      -- Public Toilet, Handpump, Drain ...
    village            VARCHAR(100),
    cleanliness_status VARCHAR(20) DEFAULT 'GOOD',   -- GOOD / AVERAGE / POOR
    last_maintenance   DATE
);

CREATE TABLE complaints (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    facility_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    reported_by VARCHAR(100),
    reported_on DATE,
    priority    VARCHAR(10) DEFAULT 'MEDIUM',        -- LOW / MEDIUM / HIGH
    status      VARCHAR(20) DEFAULT 'PENDING',       -- PENDING / IN_PROGRESS / RESOLVED
    FOREIGN KEY (facility_id) REFERENCES sanitation_facilities(id)
);

CREATE TABLE maintenance_records (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    facility_id INT NOT NULL,
    activity    VARCHAR(255),
    done_on     DATE,
    done_by     VARCHAR(100),
    FOREIGN KEY (facility_id) REFERENCES sanitation_facilities(id)
);

CREATE TABLE water_records (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    village           VARCHAR(100) NOT NULL,
    source            VARCHAR(50),       -- Handpump, Tank, Well, Pipeline
    quality_status    VARCHAR(20),       -- SAFE / UNSAFE
    ph_value          DECIMAL(3,1),
    observation       VARCHAR(255),
    issue             VARCHAR(255),
    corrective_action VARCHAR(255),
    action_status     VARCHAR(20) DEFAULT 'PENDING', -- PENDING / DONE
    recorded_on       DATE
);

INSERT INTO sanitation_facilities (name, type, village, cleanliness_status, last_maintenance) VALUES
('Rampur Community Toilet', 'Public Toilet', 'Rampur', 'POOR',    '2026-07-10'),
('Sonpur Main Handpump',    'Handpump',      'Sonpur', 'GOOD',    '2026-09-01'),
('Sonpur Market Drain',     'Drain',         'Sonpur', 'AVERAGE', '2026-08-15');

-- Example from the problem statement: health worker reports toilet needs maintenance
INSERT INTO complaints (facility_id, description, reported_by, reported_on, priority, status) VALUES
(1, 'Toilet needs maintenance - door broken and water leakage', 'Sunita Verma', '2026-10-09', 'HIGH',   'PENDING'),
(3, 'Drain blocked near market, water collecting',               'Kavita Rani',  '2026-10-07', 'MEDIUM', 'IN_PROGRESS');

INSERT INTO maintenance_records (facility_id, activity, done_on, done_by) VALUES
(2, 'Handpump repaired and cleaned', '2026-09-01', 'Village Panchayat team'),
(3, 'Drain cleaning',                '2026-08-15', 'Sanitation worker');

INSERT INTO water_records
(village, source, quality_status, ph_value, observation, issue, corrective_action, action_status, recorded_on) VALUES
('Sonpur', 'Handpump', 'SAFE',   7.2, 'Clear water, no smell',           NULL,                         NULL,                         'DONE',    '2026-10-02'),
('Rampur', 'Well',     'UNSAFE', 6.1, 'Cloudy water, bad smell',         'Possible contamination',     'Chlorination and re-testing', 'PENDING', '2026-10-08');
