package com.ruralhealth.healthcentre.repository;

import com.ruralhealth.healthcentre.model.Appointment;
import com.ruralhealth.healthcentre.model.Consultation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// SQL for appointments and consultations
@Repository
public class AppointmentRepository {

    private final JdbcTemplate jdbc;

    public AppointmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Appointment + centre name + worker name in one query (JOIN)
    private static final String SELECT =
            "SELECT a.*, c.name AS centre_name, w.name AS worker_name "
          + "FROM appointments a "
          + "JOIN health_centres c ON a.centre_id = c.id "
          + "LEFT JOIN workers w ON a.worker_id = w.id ";

    private final RowMapper<Appointment> appointmentMapper = (rs, i) -> new Appointment(
            rs.getInt("id"),
            rs.getInt("resident_id"),
            rs.getInt("centre_id"),
            (Integer) rs.getObject("worker_id"),
            rs.getObject("appointment_date", LocalDate.class),
            rs.getObject("appointment_time", LocalTime.class),
            rs.getString("reason"),
            rs.getString("status"),
            rs.getString("centre_name"),
            rs.getString("worker_name"));

    private final RowMapper<Consultation> consultationMapper = (rs, i) -> new Consultation(
            rs.getInt("id"),
            rs.getInt("appointment_id"),
            rs.getString("symptoms"),
            rs.getString("diagnosis"),
            rs.getString("treatment"),
            rs.getObject("consult_date", LocalDate.class));

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

    // status = null -> all appointments, otherwise only that status
    public List<Appointment> findAll(String status) {
        String order = "ORDER BY a.appointment_date DESC, a.appointment_time DESC, a.id DESC";
        if (status == null || status.isBlank()) {
            return jdbc.query(SELECT + order, appointmentMapper);
        }
        return jdbc.query(SELECT + "WHERE a.status = ? " + order, appointmentMapper, status);
    }

    public Appointment findById(int id) {
        List<Appointment> list = jdbc.query(SELECT + "WHERE a.id = ?", appointmentMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int save(Appointment a) {
        String status = (a.status() == null || a.status().isBlank()) ? "SCHEDULED" : a.status();
        return insert("INSERT INTO appointments (resident_id, centre_id, worker_id, appointment_date, "
                + "appointment_time, reason, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                a.residentId(), a.centreId(), a.workerId(), a.appointmentDate(),
                a.appointmentTime(), a.reason(), status);
    }

    public int updateStatus(int id, String status) {
        return jdbc.update("UPDATE appointments SET status = ? WHERE id = ?", status, id);
    }

    public List<Consultation> consultationsOf(int appointmentId) {
        return jdbc.query("SELECT * FROM consultations WHERE appointment_id = ? ORDER BY id DESC",
                consultationMapper, appointmentId);
    }

    // Saves the consultation AND marks the appointment as COMPLETED (both or none)
    @Transactional
    public int saveConsultation(Consultation c) {
        int id = insert("INSERT INTO consultations (appointment_id, symptoms, diagnosis, treatment, consult_date) "
                + "VALUES (?, ?, ?, ?, ?)",
                c.appointmentId(), c.symptoms(), c.diagnosis(), c.treatment(), c.consultDate());
        jdbc.update("UPDATE appointments SET status = 'COMPLETED' WHERE id = ?", c.appointmentId());
        return id;
    }
}
