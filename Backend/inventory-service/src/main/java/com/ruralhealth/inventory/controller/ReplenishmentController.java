package com.ruralhealth.inventory.controller;

import com.ruralhealth.inventory.model.Replenishment;
import com.ruralhealth.inventory.repository.MedicineRepository;
import com.ruralhealth.inventory.repository.ReplenishmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// REST APIs for refill (replenishment) requests
@RestController
@RequestMapping("/replenishments")
@CrossOrigin(origins = "*")
public class ReplenishmentController {

    private final ReplenishmentRepository repo;
    private final MedicineRepository medicineRepo;

    public ReplenishmentController(ReplenishmentRepository repo, MedicineRepository medicineRepo) {
        this.repo = repo;
        this.medicineRepo = medicineRepo;
    }

    // GET /replenishments                 -> all
    // GET /replenishments?status=PENDING  -> only pending
    @GetMapping
    public List<Replenishment> getAll(@RequestParam(required = false) String status) {
        return repo.findAll(status);
    }

    // POST /replenishments   body: {"medicineId": 2, "quantityNeeded": 100, "requestedBy": "Nurse"}
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Replenishment r) {
        if (r.medicineId() == null || r.quantityNeeded() == null || r.quantityNeeded() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "medicineId and a quantityNeeded above 0 are required"));
        }
        if (medicineRepo.findById(r.medicineId()) == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "That medicine does not exist"));
        }
        int id = repo.save(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.findById(id));
    }

    // PUT /replenishments/1/status   body: {"status": "APPROVED"}  (PENDING / APPROVED / DELIVERED)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "status is required"));
        }
        if (!repo.updateStatus(id, status)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findById(id));
    }
}
