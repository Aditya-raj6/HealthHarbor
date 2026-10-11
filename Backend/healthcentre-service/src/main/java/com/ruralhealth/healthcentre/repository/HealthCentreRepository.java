package com.ruralhealth.healthcentre.repository;

import com.ruralhealth.healthcentre.model.HealthCentre;
import com.ruralhealth.healthcentre.model.Worker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.List;

// SQL for health_centres and workers (plain JDBC through JdbcTemplate)
@Repository
public class HealthCentreRepository {

    private final JdbcTemplate jdbc;

    public HealthCentreRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<HealthCentre> centreMapper = (rs, i) -> new HealthCentre(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("village"),
            rs.getString("facilities"),
            rs.getObject("open_time", LocalTime.class),
            rs.getObject("close_time", LocalTime.class),
            rs.getString("working_days"));

    private final RowMapper<Worker> workerMapper = (rs, i) -> new Worker(
            rs.getInt("id"),
            rs.getInt("centre_id"),
            rs.getString("name"),
            rs.getString("role"),
            rs.getString("phone"));

    // Runs an INSERT and returns the id MySQL generated
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

    // ----- centres -----

    public List<HealthCentre> findAll() {
        return jdbc.query("SELECT * FROM health_centres ORDER BY name", centreMapper);
    }

    public HealthCentre findById(int id) {
        List<HealthCentre> list = jdbc.query("SELECT * FROM health_centres WHERE id = ?", centreMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int save(HealthCentre c) {
        return insert("INSERT INTO health_centres (name, village, facilities, open_time, close_time, working_days) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                c.name(), c.village(), c.facilities(), c.openTime(), c.closeTime(), c.workingDays());
    }

    public int update(int id, HealthCentre c) {
        return jdbc.update("UPDATE health_centres SET name=?, village=?, facilities=?, open_time=?, "
                + "close_time=?, working_days=? WHERE id=?",
                c.name(), c.village(), c.facilities(), c.openTime(), c.closeTime(), c.workingDays(), id);
    }

    // ----- workers -----

    public List<Worker> workersOf(int centreId) {
        return jdbc.query("SELECT * FROM workers WHERE centre_id = ? ORDER BY name", workerMapper, centreId);
    }

    public int saveWorker(Worker w) {
        return insert("INSERT INTO workers (centre_id, name, role, phone) VALUES (?, ?, ?, ?)",
                w.centreId(), w.name(), w.role(), w.phone());
    }
}
