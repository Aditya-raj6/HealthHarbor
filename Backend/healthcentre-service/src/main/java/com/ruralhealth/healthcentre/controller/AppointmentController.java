package com.ruralhealth.healthcentre.controller;

import com.ruralhealth.healthcentre.model.Appointment;
import com.ruralhealth.healthcentre.model.Consultation;
import com.ruralhealth.healthcentre.repository.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// REST APIs for appointments and consultations
@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentRepository repo;

    public AppointmentController(AppointmentRepository repo) {
        this.repo = repo;
    }

    // GET /appointments            -> all
    // GET /appointments?status=SCHEDULED
    @GetMapping
    public List<Appointment> getAll(@RequestParam(required = false) String status) {
        return repo.findAll(status);
    }

    // GET /appointments/1
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getOne(@PathVariable int id) {
        Appointment a = repo.findById(id);
        return a == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(a);
    }

    // POST /appointments   (book an appointment)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Appointment a) {
        if (a.residentId() == null || a.centreId() == null || a.appointmentDate() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "residentId, centreId and appointmentDate are required"));
        }
        int id = repo.save(a);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.findById(id));
    }

    // PUT /appointments/1/status   body: {"status": "CANCELLED"}
    @PutMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "status is required"));
        }
        if (repo.updateStatus(id, status) == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findById(id));
    }

    // GET /appointments/1/consultations
    @GetMapping("/{id}/consultations")
    public ResponseEntity<?> getConsultations(@PathVariable int id) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.consultationsOf(id));
    }

    // POST /appointments/1/consultations   (also marks the appointment COMPLETED)
    @PostMapping("/{id}/consultations")
    public ResponseEntity<?> addConsultation(@PathVariable int id, @RequestBody Consultation c) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        LocalDate date = c.consultDate() == null ? LocalDate.now() : c.consultDate();
        Consultation toSave = new Consultation(null, id, c.symptoms(), c.diagnosis(), c.treatment(), date);
        int newId = repo.saveConsultation(toSave);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", newId));
    }
}
