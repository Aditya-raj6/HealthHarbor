package com.ruralhealth.sanitation.controller;

import com.ruralhealth.sanitation.model.Complaint;
import com.ruralhealth.sanitation.model.Facility;
import com.ruralhealth.sanitation.repository.SanitationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// REST APIs for sanitation facilities and complaints
@RestController
@CrossOrigin(origins = "*")
public class SanitationController {

    private final SanitationRepository repo;

    public SanitationController(SanitationRepository repo) {
        this.repo = repo;
    }

    // ===== facilities =====

    // GET /facilities
    @GetMapping("/facilities")
    public List<Facility> getFacilities() {
        return repo.findAllFacilities();
    }

    // GET /facilities/1
    @GetMapping("/facilities/{id}")
    public ResponseEntity<Facility> getFacility(@PathVariable int id) {
        Facility f = repo.findFacility(id);
        return f == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(f);
    }

    // POST /facilities
    @PostMapping("/facilities")
    public ResponseEntity<?> createFacility(@RequestBody Facility f) {
        if (f.name() == null || f.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Facility name is required"));
        }
        int id = repo.saveFacility(f);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.findFacility(id));
    }

    // PUT /facilities/1
    @PutMapping("/facilities/{id}")
    public ResponseEntity<?> updateFacility(@PathVariable int id, @RequestBody Facility f) {
        if (f.name() == null || f.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Facility name is required"));
        }
        if (repo.updateFacility(id, f) == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findFacility(id));
    }

    // GET /facilities/1/maintenance -> repair and cleaning history
    @GetMapping("/facilities/{id}/maintenance")
    public ResponseEntity<?> getMaintenance(@PathVariable int id) {
        if (repo.findFacility(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.maintenanceOf(id));
    }

    // ===== complaints =====

    // GET /complaints               -> all
    // GET /complaints?status=OPEN   -> everything not resolved yet
    // GET /complaints?status=PENDING (or IN_PROGRESS / RESOLVED)
    @GetMapping("/complaints")
    public List<Complaint> getComplaints(@RequestParam(required = false) String status) {
        return repo.findComplaints(status);
    }

    // POST /complaints   body: {"facilityId": 1, "description": "...", "priority": "HIGH", "reportedBy": "..."}
    @PostMapping("/complaints")
    public ResponseEntity<?> createComplaint(@RequestBody Complaint c) {
        if (c.facilityId() == null || c.description() == null || c.description().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "facilityId and a description are required"));
        }
        if (repo.findFacility(c.facilityId()) == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "That facility does not exist"));
        }
        Complaint toSave = new Complaint(null, c.facilityId(), null, null, c.description(),
                c.reportedBy(), LocalDate.now(), c.priority(), "PENDING");
        int id = repo.saveComplaint(toSave);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.findComplaint(id));
    }

    // PUT /complaints/1/status   body: {"status": "RESOLVED", "note": "Door replaced", "doneBy": "Panchayat team"}
    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<?> changeComplaintStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "status is required"));
        }
        if (!repo.updateComplaintStatus(id, status, body.get("note"), body.get("doneBy"))) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findComplaint(id));
    }
}
