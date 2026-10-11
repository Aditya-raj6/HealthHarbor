package com.ruralhealth.inventory.controller;

import com.ruralhealth.inventory.model.IssueRecord;
import com.ruralhealth.inventory.model.Medicine;
import com.ruralhealth.inventory.repository.MedicineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// REST APIs for medicines, stock and issuing medicine
@RestController
@RequestMapping("/medicines")
@CrossOrigin(origins = "*")
public class MedicineController {

    private final MedicineRepository repo;

    public MedicineController(MedicineRepository repo) {
        this.repo = repo;
    }

    // GET /medicines              -> everything
    // GET /medicines?centreId=1   -> one health centre
    @GetMapping
    public List<Medicine> getAll(@RequestParam(required = false) Integer centreId) {
        return repo.findAll(centreId);
    }

    // GET /medicines/low-stock    -> items below their minimum level
    @GetMapping("/low-stock")
    public List<Medicine> lowStock() {
        return repo.findLowStock();
    }

    // GET /medicines/issues/recent -> last 20 issue records
    @GetMapping("/issues/recent")
    public List<IssueRecord> recentIssues() {
        return repo.recentIssues();
    }

    // GET /medicines/1
    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getOne(@PathVariable int id) {
        Medicine m = repo.findById(id);
        return m == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(m);
    }

    // POST /medicines
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Medicine m) {
        if (m.name() == null || m.name().isBlank() || m.centreId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "name and centreId are required"));
        }
        int id = repo.save(m);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.findById(id));
    }

    // PUT /medicines/1
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Medicine m) {
        if (m.name() == null || m.name().isBlank() || m.centreId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "name and centreId are required"));
        }
        if (repo.update(id, m) == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findById(id));
    }

    // POST /medicines/1/issue   body: {"residentId": 2, "quantity": 5, "issuedBy": "Nurse"}
    @PostMapping("/{id}/issue")
    public ResponseEntity<?> issue(@PathVariable int id, @RequestBody IssueRecord r) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        if (r.quantity() == null || r.quantity() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "quantity must be more than 0"));
        }
        IssueRecord toSave = new IssueRecord(null, id, null, r.residentId(), r.quantity(),
                LocalDate.now(), r.issuedBy());
        if (!repo.issue(id, toSave)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not enough stock to issue this quantity"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.findById(id));
    }

    // GET /medicines/1/issues
    @GetMapping("/{id}/issues")
    public ResponseEntity<?> issues(@PathVariable int id) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.issuesOf(id));
    }
}
