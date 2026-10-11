package com.ruralhealth.camp.repository;

import com.ruralhealth.camp.model.Beneficiary;
import com.ruralhealth.camp.model.Camp;
import com.ruralhealth.camp.model.CampStaff;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

// SQL for camps, camp staff and beneficiaries (screenings + follow-ups)
@Repository
public class CampRepository {

    private final JdbcTemplate jdbc;

    public CampRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Camp> campMapper = (rs, i) -> new Camp(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("village"),
            rs.getObject("camp_date", LocalDate.class),
            (Integer) rs.getObject("centre_id"),
            rs.getString("description"),
            rs.getString("status"));

    private final RowMapper<CampStaff> staffMapper = (rs, i) -> new CampStaff(
            rs.getInt("id"),
            rs.getInt("camp_id"),
            rs.getString("staff_name"),
            rs.getString("role"));

    private static final String BENEFICIARY_SELECT =
            "SELECT b.*, c.name AS camp_name "
          + "FROM camp_beneficiaries b JOIN camps c ON b.camp_id = c.id ";

    private final RowMapper<Beneficiary> beneficiaryMapper = (rs, i) -> new Beneficiary(
            rs.getInt("id"),
            rs.getInt("camp_id"),
            rs.getString("camp_name"),
            (Integer) rs.getObject("resident_id"),
            rs.getString("resident_name"),
            rs.getString("screening_type"),
            rs.getString("result"),
            rs.getBoolean("follow_up_needed"),
            rs.getObject("follow_up_date", LocalDate.class),
            rs.getString("follow_up_status"));

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

    // ----- camps -----

    // status = null -> all camps
    public List<Camp> findAll(String status) {
        if (status == null || status.isBlank()) {
            return jdbc.query("SELECT * FROM camps ORDER BY camp_date DESC, id DESC", campMapper);
        }
        return jdbc.query("SELECT * FROM camps WHERE status = ? ORDER BY camp_date, id", campMapper, status);
    }

    public Camp findById(int id) {
        List<Camp> list = jdbc.query("SELECT * FROM camps WHERE id = ?", campMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int save(Camp c) {
        String status = (c.status() == null || c.status().isBlank()) ? "PLANNED" : c.status();
        return insert("INSERT INTO camps (name, village, camp_date, centre_id, description, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                c.name(), c.village(), c.campDate(), c.centreId(), c.description(), status);
    }

    public int updateStatus(int id, String status) {
        return jdbc.update("UPDATE camps SET status = ? WHERE id = ?", status, id);
    }

    // ----- staff -----

    public List<CampStaff> staffOf(int campId) {
        return jdbc.query("SELECT * FROM camp_staff WHERE camp_id = ? ORDER BY id", staffMapper, campId);
    }

    public int saveStaff(CampStaff s) {
        return insert("INSERT INTO camp_staff (camp_id, staff_name, role) VALUES (?, ?, ?)",
                s.campId(), s.staffName(), s.role());
    }

    // ----- beneficiaries -----

    public List<Beneficiary> beneficiariesOf(int campId) {
        return jdbc.query(BENEFICIARY_SELECT + "WHERE b.camp_id = ? ORDER BY b.id", beneficiaryMapper, campId);
    }

    public int saveBeneficiary(Beneficiary b) {
        boolean needed = Boolean.TRUE.equals(b.followUpNeeded());
        String followUpStatus = needed ? "PENDING" : null;
        LocalDate followUpDate = needed ? b.followUpDate() : null;
        return insert("INSERT INTO camp_beneficiaries (camp_id, resident_id, resident_name, screening_type, "
                + "result, follow_up_needed, follow_up_date, follow_up_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                b.campId(), b.residentId(), b.residentName(), b.screeningType(), b.result(),
                needed, followUpDate, followUpStatus);
    }

    // People who still need a follow-up (used by the dashboard)
    public List<Beneficiary> pendingFollowUps() {
        return jdbc.query(BENEFICIARY_SELECT
                + "WHERE b.follow_up_needed = TRUE AND b.follow_up_status = 'PENDING' "
                + "ORDER BY b.follow_up_date, b.id", beneficiaryMapper);
    }

    public int markFollowUpDone(int beneficiaryId) {
        return jdbc.update("UPDATE camp_beneficiaries SET follow_up_status = 'DONE' WHERE id = ?", beneficiaryId);
    }
}
