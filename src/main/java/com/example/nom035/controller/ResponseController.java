package com.example.nom035.controller;

import com.example.nom035.dto.ResponseCreateDto;
import com.example.nom035.dto.ResponseDto;
import com.example.nom035.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/responses")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ResponseController {

    @Autowired
    private ResponseService responseService;

    @GetMapping
    public ResponseEntity<List<ResponseDto>> getAllResponses() {
        try {
            List<ResponseDto> responses = responseService.getAllResponses();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getResponseById(@PathVariable Long id) {
        try {
            Optional<ResponseDto> response = responseService.getResponseById(id);
            return response.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/survey-application/{surveyApplicationId}")
    public ResponseEntity<List<ResponseDto>> getResponsesBySurveyApplication(@PathVariable Long surveyApplicationId) {
        try {
            List<ResponseDto> responses = responseService.getResponsesBySurveyApplication(surveyApplicationId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createResponse(@RequestBody ResponseCreateDto responseCreateDto) {
        try {
            ResponseDto createdResponse = responseService.createResponse(responseCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateResponse(@PathVariable Long id, @RequestBody ResponseCreateDto responseCreateDto) {
        try {
            ResponseDto updatedResponse = responseService.updateResponse(id, responseCreateDto);
            return ResponseEntity.ok(updatedResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponse(@PathVariable Long id) {
        try {
            responseService.deleteResponse(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}