package com.ruralhealth.inventory.repository;

import com.ruralhealth.inventory.model.IssueRecord;
import com.ruralhealth.inventory.model.Medicine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

// SQL for medicines and issue records
@Repository
public class MedicineRepository {

    private final JdbcTemplate jdbc;

    public MedicineRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Medicine> medicineMapper = (rs, i) -> new Medicine(
            rs.getInt("id"),
            rs.getInt("centre_id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getInt("quantity"),
            rs.getInt("min_level"),
            rs.getString("unit"),
            rs.getObject("expiry_date", LocalDate.class));

    private static final String ISSUE_SELECT =
            "SELECT i.*, m.name AS medicine_name "
          + "FROM issue_records i JOIN medicines m ON i.medicine_id = m.id ";

    private final RowMapper<IssueRecord> issueMapper = (rs, i) -> new IssueRecord(
            rs.getInt("id"),
            rs.getInt("medicine_id"),
            rs.getString("medicine_name"),
            (Integer) rs.getObject("resident_id"),
            rs.getInt("quantity"),
            rs.getObject("issue_date", LocalDate.class),
            rs.getString("issued_by"));

    private int insert(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    // ----- medicines -----

    // centreId = null -> all centres
    public List<Medicine> findAll(Integer centreId) {
        if (centreId == null) {
            return jdbc.query("SELECT * FROM medicines ORDER BY name, centre_id", medicineMapper);
        }
        return jdbc.query("SELECT * FROM medicines WHERE centre_id = ? ORDER BY name", medicineMapper, centreId);
    }

    // Items whose quantity is below their minimum level
    public List<Medicine> findLowStock() {
        return jdbc.query("SELECT * FROM medicines WHERE quantity < min_level ORDER BY quantity", medicineMapper);
    }

    public Medicine findById(int id) {
        List<Medicine> list = jdbc.query("SELECT * FROM medicines WHERE id = ?", medicineMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int save(Medicine m) {
        int quantity = m.quantity() == null ? 0 : m.quantity();
        int minLevel = m.minLevel() == null ? 10 : m.minLevel();
        return insert("INSERT INTO medicines (centre_id, name, category, quantity, min_level, unit, expiry_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                m.centreId(), m.name(), m.category(), quantity, minLevel, m.unit(), m.expiryDate());
    }

    public int update(int id, Medicine m) {
        int quantity = m.quantity() == null ? 0 : m.quantity();
        int minLevel = m.minLevel() == null ? 10 : m.minLevel();
        return jdbc.update("UPDATE medicines SET centre_id=?, name=?, category=?, quantity=?, min_level=?, "
                + "unit=?, expiry_date=? WHERE id=?",
                m.centreId(), m.name(), m.category(), quantity, minLevel, m.unit(), m.expiryDate(), id);
    }

    // ----- issuing medicine -----

    // Reduces the stock and writes an issue record. Returns false if there is not enough stock.
    @Transactional
    public boolean issue(int medicineId, IssueRecord r) {
        int updated = jdbc.update(
                "UPDATE medicines SET quantity = quantity - ? WHERE id = ? AND quantity >= ?",
                r.quantity(), medicineId, r.quantity());
        if (updated == 0) {
            return false;
        }
        insert("INSERT INTO issue_records (medicine_id, resident_id, quantity, issue_date, issued_by) "
                + "VALUES (?, ?, ?, ?, ?)",
                medicineId, r.residentId(), r.quantity(), r.issueDate(), r.issuedBy());
        return true;
    }

    public List<IssueRecord> issuesOf(int medicineId) {
        return jdbc.query(ISSUE_SELECT + "WHERE i.medicine_id = ? ORDER BY i.issue_date DESC, i.id DESC",
                issueMapper, medicineId);
    }

    public List<IssueRecord> recentIssues() {
        return jdbc.query(ISSUE_SELECT + "ORDER BY i.issue_date DESC, i.id DESC LIMIT 20", issueMapper);
    }
}
