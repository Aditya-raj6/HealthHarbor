-- inventory_db : medicines, issue records, replenishment requests
CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

DROP TABLE IF EXISTS replenishment_requests;
DROP TABLE IF EXISTS issue_records;
DROP TABLE IF EXISTS medicines;

CREATE TABLE medicines (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    centre_id   INT NOT NULL,            -- id from healthcentre_db (other service)
    name        VARCHAR(100) NOT NULL,
    category    VARCHAR(50),             -- Medicine / Supply
    quantity    INT NOT NULL DEFAULT 0,
    min_level   INT NOT NULL DEFAULT 10, -- below this = LOW STOCK
    unit        VARCHAR(20),             -- tablets, strips, bottles ...
    expiry_date DATE
);

CREATE TABLE issue_records (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    resident_id INT,                     -- id from resident_db (other service)
    quantity    INT NOT NULL,
    issue_date  DATE NOT NULL,
    issued_by   VARCHAR(100),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

CREATE TABLE replenishment_requests (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id     INT NOT NULL,
    centre_id       INT NOT NULL,
    quantity_needed INT NOT NULL,
    requested_by    VARCHAR(100),
    requested_on    DATE,
    status          VARCHAR(20) DEFAULT 'PENDING',   -- PENDING / APPROVED / DELIVERED
    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
);

INSERT INTO medicines (centre_id, name, category, quantity, min_level, unit, expiry_date) VALUES
(1, 'Paracetamol 500mg', 'Medicine', 200, 50, 'tablets',  '2027-06-30'),
(1, 'ORS Packets',       'Medicine', 8,   30, 'packets',  '2027-01-31'),   -- LOW STOCK (example)
(1, 'Amoxicillin 250mg', 'Medicine', 60,  40, 'capsules', '2027-03-15'),
(1, 'Bandages',          'Supply',   100, 25, 'rolls',    NULL),
(2, 'Paracetamol 500mg', 'Medicine', 150, 50, 'tablets',  '2027-05-20'),
(2, 'Metformin 500mg',   'Medicine', 25,  40, 'tablets',  '2027-02-28'),   -- LOW STOCK
(2, 'Syringes',          'Supply',   300, 50, 'pieces',   NULL);

INSERT INTO issue_records (medicine_id, resident_id, quantity, issue_date, issued_by) VALUES
(1, 2, 10, '2026-09-25', 'Sunita Verma'),
(2, 4, 6,  '2026-10-05', 'Kavita Rani'),
(6, 3, 30, '2026-10-01', 'Kavita Rani');

INSERT INTO replenishment_requests (medicine_id, centre_id, quantity_needed, requested_by, requested_on, status) VALUES
(2, 1, 100, 'Sunita Verma', '2026-10-09', 'PENDING');
