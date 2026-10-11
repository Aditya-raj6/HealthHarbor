package com.ruralhealth.resident.controller;

import com.ruralhealth.resident.model.Resident;
import com.ruralhealth.resident.model.Visit;
import com.ruralhealth.resident.repository.ResidentRepository;
import com.ruralhealth.resident.repository.VisitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// REST APIs for residents and their visit history
// @CrossOrigin lets our HTML pages (a different port) call these APIs
@RestController
@RequestMapping("/residents")
@CrossOrigin(origins = "*")
public class ResidentController {

    private final ResidentRepository residentRepo;
    private final VisitRepository visitRepo;

    public ResidentController(ResidentRepository residentRepo, VisitRepository visitRepo) {
        this.residentRepo = residentRepo;
        this.visitRepo = visitRepo;
    }

    // GET /residents            -> all residents
    // GET /residents?village=X  -> residents of one village
    @GetMapping
    public List<Resident> getAll(@RequestParam(required = false) String village) {
        if (village != null && !village.isBlank()) {
            return residentRepo.findByVillage(village);
        }
        return residentRepo.findAll();
    }

    // GET /residents/count -> {"count": 5}   (used by the dashboard)
    @GetMapping("/count")
    public Map<String, Integer> count() {
        return Map.of("count", residentRepo.count());
    }

    // GET /residents/1
    @GetMapping("/{id}")
    public ResponseEntity<Resident> getOne(@PathVariable int id) {
        Resident r = residentRepo.findById(id);
        return r == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(r);
    }

    // POST /residents   (JSON body)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Resident resident) {
        if (resident.getName() == null || resident.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
        }
        int newId = residentRepo.save(resident);
        resident.setId(newId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resident);
    }

    // PUT /residents/1   (JSON body)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Resident resident) {
        if (residentRepo.update(id, resident) == 0) {
            return ResponseEntity.notFound().build();
        }
        resident.setId(id);
        return ResponseEntity.ok(resident);
    }

    // DELETE /residents/1
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        if (residentRepo.delete(id) == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Resident deleted"));
    }

    // GET /residents/1/visits -> visit history
    @GetMapping("/{id}/visits")
    public ResponseEntity<?> getVisits(@PathVariable int id) {
        if (residentRepo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(visitRepo.findByResident(id));
    }

    // POST /residents/1/visits   (JSON body)
    @PostMapping("/{id}/visits")
    public ResponseEntity<?> addVisit(@PathVariable int id, @RequestBody Visit visit) {
        if (residentRepo.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        visit.setResidentId(id);
        int newId = visitRepo.save(visit);
        visit.setId(newId);
        return ResponseEntity.status(HttpStatus.CREATED).body(visit);
    }
}
