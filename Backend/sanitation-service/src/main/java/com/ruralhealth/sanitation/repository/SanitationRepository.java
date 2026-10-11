package com.ruralhealth.sanitation.repository;

import com.ruralhealth.sanitation.model.Complaint;
import com.ruralhealth.sanitation.model.Facility;
import com.ruralhealth.sanitation.model.MaintenanceRecord;
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

// SQL for sanitation facilities, complaints and maintenance records
@Repository
public class SanitationRepository {

    private final JdbcTemplate jdbc;

    public SanitationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Facility> facilityMapper = (rs, i) -> new Facility(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("type"),
            rs.getString("village"),
            rs.getString("cleanliness_status"),
            rs.getObject("last_maintenance", LocalDate.class));

    // Complaint + the name and village of the facility it is about (JOIN)
    private static final String COMPLAINT_SELECT =
            "SELECT c.*, f.name AS facility_name, f.village AS village "
          + "FROM complaints c JOIN sanitation_facilities f ON c.facility_id = f.id ";

    private final RowMapper<Complaint> complaintMapper = (rs, i) -> new Complaint(
            rs.getInt("id"),
            rs.getInt("facility_id"),
            rs.getString("facility_name"),
            rs.getString("village"),
            rs.getString("description"),
            rs.getString("reported_by"),
            rs.getObject("reported_on", LocalDate.class),
            rs.getString("priority"),
            rs.getString("status"));

    private final RowMapper<MaintenanceRecord> maintenanceMapper = (rs, i) -> new MaintenanceRecord(
            rs.getInt("id"),
            rs.getInt("facility_id"),
            rs.getString("activity"),
            rs.getObject("done_on", LocalDate.class),
            rs.getString("done_by"));

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

    // ----- facilities -----

    public List<Facility> findAllFacilities() {
        return jdbc.query("SELECT * FROM sanitation_facilities ORDER BY village, name", facilityMapper);
    }

    public Facility findFacility(int id) {
        List<Facility> list = jdbc.query("SELECT * FROM sanitation_facilities WHERE id = ?", facilityMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int saveFacility(Facility f) {
        String cleanliness = (f.cleanlinessStatus() == null || f.cleanlinessStatus().isBlank())
                ? "GOOD" : f.cleanlinessStatus();
        return insert("INSERT INTO sanitation_facilities (name, type, village, cleanliness_status, last_maintenance) "
                + "VALUES (?, ?, ?, ?, ?)",
                f.name(), f.type(), f.village(), cleanliness, f.lastMaintenance());
    }

    public int updateFacility(int id, Facility f) {
        String cleanliness = (f.cleanlinessStatus() == null || f.cleanlinessStatus().isBlank())
                ? "GOOD" : f.cleanlinessStatus();
        return jdbc.update("UPDATE sanitation_facilities SET name=?, type=?, village=?, cleanliness_status=?, "
                + "last_maintenance=? WHERE id=?",
                f.name(), f.type(), f.village(), cleanliness, f.lastMaintenance(), id);
    }

    public List<MaintenanceRecord> maintenanceOf(int facilityId) {
        return jdbc.query("SELECT * FROM maintenance_records WHERE facility_id = ? ORDER BY done_on DESC, id DESC",
                maintenanceMapper, facilityId);
    }

    // ----- complaints -----

    // status = null/blank -> all, "OPEN" -> everything not resolved, otherwise that exact status
    public List<Complaint> findComplaints(String status) {
        String order = "ORDER BY FIELD(c.priority, 'HIGH', 'MEDIUM', 'LOW'), c.reported_on DESC, c.id DESC";
        if (status == null || status.isBlank()) {
            return jdbc.query(COMPLAINT_SELECT + order, complaintMapper);
        }
        if ("OPEN".equals(status)) {
            return jdbc.query(COMPLAINT_SELECT + "WHERE c.status <> 'RESOLVED' " + order, complaintMapper);
        }
        return jdbc.query(COMPLAINT_SELECT + "WHERE c.status = ? " + order, complaintMapper, status);
    }

    public Complaint findComplaint(int id) {
        List<Complaint> list = jdbc.query(COMPLAINT_SELECT + "WHERE c.id = ?", complaintMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int saveComplaint(Complaint c) {
        String priority = (c.priority() == null || c.priority().isBlank()) ? "MEDIUM" : c.priority();
        LocalDate reportedOn = c.reportedOn() == null ? LocalDate.now() : c.reportedOn();
        return insert("INSERT INTO complaints (facility_id, description, reported_by, reported_on, priority, status) "
                + "VALUES (?, ?, ?, ?, ?, 'PENDING')",
                c.facilityId(), c.description(), c.reportedBy(), reportedOn, priority);
    }

    // Changes a complaint's status. When it becomes RESOLVED we also write a maintenance record
    // and update the facility's "last maintenance" date and cleanliness. All or nothing.
    @Transactional
    public boolean updateComplaintStatus(int id, String status, String note, String doneBy) {
        List<Integer> facilityIds = jdbc.queryForList(
                "SELECT facility_id FROM complaints WHERE id = ?", Integer.class, id);
        if (facilityIds.isEmpty()) {
            return false;
        }
        jdbc.update("UPDATE complaints SET status = ? WHERE id = ?", status, id);
        if ("RESOLVED".equals(status)) {
            int facilityId = facilityIds.get(0);
            String activity = (note == null || note.isBlank()) ? "Complaint resolved" : note;
            insert("INSERT INTO maintenance_records (facility_id, activity, done_on, done_by) VALUES (?, ?, ?, ?)",
                    facilityId, activity, LocalDate.now(), doneBy);
            jdbc.update("UPDATE sanitation_facilities SET last_maintenance = ?, cleanliness_status = 'GOOD' WHERE id = ?",
                    LocalDate.now(), facilityId);
        }
        return true;
    }
}
