package com.ruralhealth.healthcentre.controller;

import com.ruralhealth.healthcentre.model.HealthCentre;
import com.ruralhealth.healthcentre.model.Worker;
import com.ruralhealth.healthcentre.repository.HealthCentreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// REST APIs for health centres and their workers
@RestController
@RequestMapping("/centres")
@CrossOrigin(origins = "*")
public class HealthCentreController {

    private final HealthCentreRepository repo;

    public HealthCentreController(HealthCentreRepository repo) {
        this.repo = repo;
    }

    // GET /centres
    @GetMapping
    public List<HealthCentre> getAll() {
        return repo.findAll();
    }

    // GET /centres/1
    @GetMapping("/{id}")
    public ResponseEntity<HealthCentre> getOne(@PathVariable int id) {
        HealthCentre c = repo.findById(id);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    // POST /centres
    @PostMapping
    public ResponseEntity<?> create(@RequestBody HealthCentre c) {
        if (c.name() == null || c.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Centre name is required"));
        }
        int id = repo.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.findById(id));
    }

    // PUT /centres/1
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody HealthCentre c) {
        if (c.name() == null || c.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Centre name is required"));
        }
        if (repo.update(id, c) == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findById(id));
    }

    // GET /centres/1/workers
    @GetMapping("/{id}/workers")
    public ResponseEntity<?> getWorkers(@PathVariable int id) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.workersOf(id));
    }

    // POST /centres/1/workers
    @PostMapping("/{id}/workers")
    public ResponseEntity<?> addWorker(@PathVariable int id, @RequestBody Worker w) {
        if (repo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        if (w.name() == null || w.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Worker name is required"));
        }
        Worker toSave = new Worker(null, id, w.name(), w.role(), w.phone());
        int newId = repo.saveWorker(toSave);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", newId));
    }
}
