package com.ruralhealth.sanitation.controller;

import com.ruralhealth.sanitation.model.WaterRecord;
import com.ruralhealth.sanitation.repository.WaterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// REST APIs for water supply and quality monitoring
@RestController
@RequestMapping("/water")
@CrossOrigin(origins = "*")
public class WaterController {

    private final WaterRepository repo;

    public WaterController(WaterRepository repo) {
        this.repo = repo;
    }

    // GET /water                -> all records
    // GET /water?pending=true   -> only records whose corrective action is still pending
    @GetMapping
    public List<WaterRecord> getAll(@RequestParam(defaultValue = "false") boolean pending) {
        return repo.findAll(pending);
    }

    // POST /water
    @PostMapping
    public ResponseEntity<?> create(@RequestBody WaterRecord w) {
        if (w.village() == null || w.village().isBlank() || w.qualityStatus() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "village and qualityStatus are required"));
        }
        int id = repo.save(w);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.findById(id));
    }

    // PUT /water/1/action-done -> corrective action has been carried out
    @PutMapping("/{id}/action-done")
    public ResponseEntity<?> actionDone(@PathVariable int id) {
        if (repo.markActionDone(id) == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findById(id));
    }
}
