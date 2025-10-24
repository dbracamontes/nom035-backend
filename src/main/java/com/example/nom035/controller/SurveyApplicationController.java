package com.example.nom035.controller;

import com.example.nom035.dto.SurveyApplicationCreateDto;
import com.example.nom035.dto.SurveyApplicationDto;
import com.example.nom035.dto.SurveyApplicationCheckDto;
import com.example.nom035.entity.SurveyApplication;
import com.example.nom035.service.SurveyApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.annotation.Secured;

@RestController
@RequestMapping("/api/survey-applications")
@CrossOrigin(origins = "*")
public class SurveyApplicationController {

    private final SurveyApplicationService service;

    public SurveyApplicationController(SurveyApplicationService service) {
        this.service = service;
    }

    @GetMapping
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN"})
    public List<SurveyApplicationDto> list() {
        return service.getAll().stream().map(SurveyApplicationDto::fromEntity).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN"})
    public ResponseEntity<SurveyApplicationDto> get(@PathVariable Long id) {
        SurveyApplication sa = service.getById(id);
        if (sa == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(SurveyApplicationDto.fromEntity(sa));
    }

    @GetMapping("/check")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN"})
    public ResponseEntity<SurveyApplicationCheckDto> check(@RequestParam("employeeId") Long employeeId,
                                                           @RequestParam("surveyId") Long surveyId) {
        try {
            SurveyApplicationCheckDto dto = service.check(employeeId, surveyId);
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN"})
    public ResponseEntity<SurveyApplicationDto> create(@RequestBody SurveyApplicationCreateDto dto) {
        try {
            SurveyApplication created = service.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(SurveyApplicationDto.fromEntity(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}