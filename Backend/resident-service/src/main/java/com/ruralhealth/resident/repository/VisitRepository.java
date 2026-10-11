package com.ruralhealth.resident.repository;

import com.ruralhealth.resident.model.Visit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

// All SQL for the "visits" table lives here
@Repository
public class VisitRepository {

    private final JdbcTemplate jdbc;

    public VisitRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Visit> mapper = (rs, rowNum) -> {
        Visit v = new Visit();
        v.setId(rs.getInt("id"));
        v.setResidentId(rs.getInt("resident_id"));
        v.setHealthCentreId((Integer) rs.getObject("health_centre_id"));
        Date d = rs.getDate("visit_date");
        v.setVisitDate(d == null ? null : d.toLocalDate());
        v.setReason(rs.getString("reason"));
        v.setDiagnosis(rs.getString("diagnosis"));
        v.setTreatment(rs.getString("treatment"));
        return v;
    };

    // Visit history of one resident, newest first
    public List<Visit> findByResident(int residentId) {
        return jdbc.query(
                "SELECT * FROM visits WHERE resident_id = ? ORDER BY visit_date DESC, id DESC",
                mapper, residentId);
    }

    public int save(Visit v) {
        String sql = "INSERT INTO visits (resident_id, health_centre_id, visit_date, reason, diagnosis, treatment) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, v.getResidentId());
            ps.setObject(2, v.getHealthCentreId());
            ps.setDate(3, v.getVisitDate() == null ? null : Date.valueOf(v.getVisitDate()));
            ps.setString(4, v.getReason());
            ps.setString(5, v.getDiagnosis());
            ps.setString(6, v.getTreatment());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }
}
