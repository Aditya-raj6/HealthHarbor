package com.ruralhealth.inventory.repository;

import com.ruralhealth.inventory.model.Replenishment;
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

// SQL for replenishment (refill) requests
@Repository
public class ReplenishmentRepository {

    private final JdbcTemplate jdbc;

    public ReplenishmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT =
            "SELECT r.*, m.name AS medicine_name "
          + "FROM replenishment_requests r JOIN medicines m ON r.medicine_id = m.id ";

    private final RowMapper<Replenishment> mapper = (rs, i) -> new Replenishment(
            rs.getInt("id"),
            rs.getInt("medicine_id"),
            rs.getString("medicine_name"),
            rs.getInt("centre_id"),
            rs.getInt("quantity_needed"),
            rs.getString("requested_by"),
            rs.getObject("requested_on", LocalDate.class),
            rs.getString("status"));

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

    // status = null -> all requests
    public List<Replenishment> findAll(String status) {
        if (status == null || status.isBlank()) {
            return jdbc.query(SELECT + "ORDER BY r.id DESC", mapper);
        }
        return jdbc.query(SELECT + "WHERE r.status = ? ORDER BY r.id DESC", mapper, status);
    }

    public Replenishment findById(int id) {
        List<Replenishment> list = jdbc.query(SELECT + "WHERE r.id = ?", mapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    // The centre is taken from the medicine itself, so the caller only sends medicineId + quantity
    public int save(Replenishment r) {
        Integer centreId = jdbc.queryForObject(
                "SELECT centre_id FROM medicines WHERE id = ?", Integer.class, r.medicineId());
        LocalDate requestedOn = r.requestedOn() == null ? LocalDate.now() : r.requestedOn();
        return insert("INSERT INTO replenishment_requests "
                + "(medicine_id, centre_id, quantity_needed, requested_by, requested_on, status) "
                + "VALUES (?, ?, ?, ?, ?, 'PENDING')",
                r.medicineId(), centreId, r.quantityNeeded(), r.requestedBy(), requestedOn);
    }

    // Changes the status. When a request becomes DELIVERED the quantity is added to the stock.
    @Transactional
    public boolean updateStatus(int id, String status) {
        Replenishment r = findById(id);
        if (r == null) {
            return false;
        }
        if ("DELIVERED".equals(status) && !"DELIVERED".equals(r.status())) {
            jdbc.update("UPDATE medicines SET quantity = quantity + ? WHERE id = ?",
                    r.quantityNeeded(), r.medicineId());
        }
        jdbc.update("UPDATE replenishment_requests SET status = ? WHERE id = ?", status, id);
        return true;
    }
}
