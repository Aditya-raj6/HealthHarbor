package com.ruralhealth.sanitation.repository;

import com.ruralhealth.sanitation.model.WaterRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

// SQL for water supply / quality records
@Repository
public class WaterRepository {

    private final JdbcTemplate jdbc;

    public WaterRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<WaterRecord> mapper = (rs, i) -> {
        BigDecimal ph = rs.getBigDecimal("ph_value");
        return new WaterRecord(
                rs.getInt("id"),
                rs.getString("village"),
                rs.getString("source"),
                rs.getString("quality_status"),
                ph == null ? null : ph.doubleValue(),
                rs.getString("observation"),
                rs.getString("issue"),
                rs.getString("corrective_action"),
                rs.getString("action_status"),
                rs.getObject("recorded_on", LocalDate.class));
    };

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

    // pendingOnly = true -> only records whose corrective action is still PENDING
    public List<WaterRecord> findAll(boolean pendingOnly) {
        if (pendingOnly) {
            return jdbc.query("SELECT * FROM water_records WHERE action_status = 'PENDING' "
                    + "ORDER BY recorded_on DESC, id DESC", mapper);
        }
        return jdbc.query("SELECT * FROM water_records ORDER BY recorded_on DESC, id DESC", mapper);
    }

    public WaterRecord findById(int id) {
        List<WaterRecord> list = jdbc.query("SELECT * FROM water_records WHERE id = ?", mapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    // If nobody reported an issue there is nothing to fix, so the action is NOT_REQUIRED
    public int save(WaterRecord w) {
        boolean hasIssue = w.issue() != null && !w.issue().isBlank();
        String actionStatus = hasIssue ? "PENDING" : "NOT_REQUIRED";
        LocalDate recordedOn = w.recordedOn() == null ? LocalDate.now() : w.recordedOn();
        return insert("INSERT INTO water_records (village, source, quality_status, ph_value, observation, "
                + "issue, corrective_action, action_status, recorded_on) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                w.village(), w.source(), w.qualityStatus(), w.phValue(), w.observation(),
                w.issue(), w.correctiveAction(), actionStatus, recordedOn);
    }

    public int markActionDone(int id) {
        return jdbc.update("UPDATE water_records SET action_status = 'DONE' WHERE id = ?", id);
    }
}
