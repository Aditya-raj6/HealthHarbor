package com.ruralhealth.resident.repository;

import com.ruralhealth.resident.model.Resident;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

// All SQL for the "residents" table lives here (using JDBC through JdbcTemplate)
@Repository
public class ResidentRepository {

    private final JdbcTemplate jdbc;

    public ResidentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Converts one database row into a Resident object
    private final RowMapper<Resident> mapper = (rs, rowNum) -> {
        Resident r = new Resident();
        r.setId(rs.getInt("id"));
        r.setName(rs.getString("name"));
        r.setAge((Integer) rs.getObject("age"));
        r.setGender(rs.getString("gender"));
        r.setVillage(rs.getString("village"));
        r.setPhone(rs.getString("phone"));
        r.setBloodGroup(rs.getString("blood_group"));
        r.setMedicalNotes(rs.getString("medical_notes"));
        return r;
    };

    public List<Resident> findAll() {
        return jdbc.query("SELECT * FROM residents ORDER BY id", mapper);
    }

    public List<Resident> findByVillage(String village) {
        return jdbc.query("SELECT * FROM residents WHERE village = ? ORDER BY id", mapper, village);
    }

    public Resident findById(int id) {
        List<Resident> list = jdbc.query("SELECT * FROM residents WHERE id = ?", mapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    // Inserts a new resident and returns the new id
    public int save(Resident r) {
        String sql = "INSERT INTO residents (name, age, gender, village, phone, blood_group, medical_notes) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.getName());
            ps.setObject(2, r.getAge());
            ps.setString(3, r.getGender());
            ps.setString(4, r.getVillage());
            ps.setString(5, r.getPhone());
            ps.setString(6, r.getBloodGroup());
            ps.setString(7, r.getMedicalNotes());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    // Returns how many rows were updated (0 = resident not found)
    public int update(int id, Resident r) {
        String sql = "UPDATE residents SET name=?, age=?, gender=?, village=?, phone=?, "
                   + "blood_group=?, medical_notes=? WHERE id=?";
        return jdbc.update(sql, r.getName(), r.getAge(), r.getGender(), r.getVillage(),
                r.getPhone(), r.getBloodGroup(), r.getMedicalNotes(), id);
    }

    public int delete(int id) {
        // visits belong to the resident, so remove them first
        jdbc.update("DELETE FROM visits WHERE resident_id = ?", id);
        return jdbc.update("DELETE FROM residents WHERE id = ?", id);
    }

    public int count() {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM residents", Integer.class);
        return c == null ? 0 : c;
    }
}
