package com.ruralhealth.camp.controller;

import com.ruralhealth.camp.model.Beneficiary;
import com.ruralhealth.camp.model.Camp;
import com.ruralhealth.camp.model.CampStaff;
import com.ruralhealth.camp.repository.CampRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// REST APIs for health camps, their staff, beneficiaries and follow-ups
@RestController
@CrossOrigin(origins = "*")
public class CampController {

    private final CampRepository repo;

    public CampController(CampRepository repo) {
        this.repo = repo;
    }

    // GET /camps                 -> all camps
    // GET /camps?status=PLANNED  -> only planned camps
    @GetMapping("/camps")
    public List<Camp> getAll(@RequestParam(required = false) String status) {
        return repo.findAll(status);
    }

    // GET /camps/1
    @GetMapping("/camps/{id}")
    public ResponseEntity<Camp> getOne(@PathVariable int id) {
        Camp c = repo.findById(id);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    // POST /camps
    @PostMapping("/camps")
    public ResponseEntity<?> create(@RequestBody Camp c) {
        if (c.name() == null || c.name().isBlank() || c.campDate() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Camp name and date are required"));
        }
        int id = repo.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.findById(id));
    }

    // PUT /camps/1/status   body: {"status": "COMPLETED"}   (PLANNED / COMPLETED / CANCELLED)
    @PutMapping("/camps/{id}/status")
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

    // GET /camps/1/staff
    @GetMapping("/camps/{id}/staff")
    public ResponseEntity<?> getStaff(@PathVariable int id) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.staffOf(id));
    }

    // POST /camps/1/staff
    @PostMapping("/camps/{id}/staff")
    public ResponseEntity<?> addStaff(@PathVariable int id, @RequestBody CampStaff s) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        if (s.staffName() == null || s.staffName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Staff name is required"));
        }
        int newId = repo.saveStaff(new CampStaff(null, id, s.staffName(), s.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", newId));
    }

    // GET /camps/1/beneficiaries
    @GetMapping("/camps/{id}/beneficiaries")
    public ResponseEntity<?> getBeneficiaries(@PathVariable int id) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.beneficiariesOf(id));
    }

    // POST /camps/1/beneficiaries
    @PostMapping("/camps/{id}/beneficiaries")
    public ResponseEntity<?> addBeneficiary(@PathVariable int id, @RequestBody Beneficiary b) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        if (b.residentName() == null || b.residentName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name of the person is required"));
        }
        Beneficiary toSave = new Beneficiary(null, id, null, b.residentId(), b.residentName(),
                b.screeningType(), b.result(), b.followUpNeeded(), b.followUpDate(), null);
        int newId = repo.saveBeneficiary(toSave);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", newId));
    }

    // GET /followups/pending -> everyone who still needs a follow-up
    @GetMapping("/followups/pending")
    public List<Beneficiary> pendingFollowUps() {
        return repo.pendingFollowUps();
    }

    // PUT /beneficiaries/3/followup-done
    @PutMapping("/beneficiaries/{id}/followup-done")
    public ResponseEntity<?> followUpDone(@PathVariable int id) {
        if (repo.markFollowUpDone(id) == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Follow-up marked as done"));
    }
}
