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

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.nom035.repository.UserRepository;
import com.example.nom035.entity.User;

@RestController
@RequestMapping("/api/responses")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ResponseController {

    @Autowired
    private ResponseService responseService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    private boolean isCompany() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY"));
    }

    @GetMapping
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<List<ResponseDto>> getAllResponses() {
        try {
            if (isAdmin()) {
                return ResponseEntity.ok(responseService.getAllResponses());
            } else if (isCompany()) {
                User u = getCurrentUser();
                if (u == null || u.getCompanyId() == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                return ResponseEntity.ok(responseService.getAllResponsesByCompany(u.getCompanyId()));
            } else {
                // ROLE_EMPLOYEE (legacy behavior: return all)
                List<ResponseDto> responses = responseService.getAllResponses();
                return ResponseEntity.ok(responses);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_COMPANY"})
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
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<List<ResponseDto>> getResponsesBySurveyApplication(@PathVariable Long surveyApplicationId) {
        try {
            List<ResponseDto> responses = responseService.getResponsesBySurveyApplication(surveyApplicationId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filtered")
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN", "ROLE_COMPANY"})
    public ResponseEntity<List<ResponseDto>> getFilteredResponses(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long surveyId) {
        try {
            List<ResponseDto> responses = responseService.getFilteredResponses(employeeId, surveyId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Secured({"ROLE_EMPLOYEE", "ROLE_ADMIN"})
    public ResponseEntity<?> createResponse(@RequestBody List<ResponseCreateDto> responseCreateDtoList) {
        try {
            if (responseCreateDtoList == null || responseCreateDtoList.isEmpty()) {
                return ResponseEntity.badRequest().body("Request body cannot be empty");
            }
            
            // Si solo hay una respuesta, devolver un objeto único
            if (responseCreateDtoList.size() == 1) {
                ResponseDto createdResponse = responseService.createResponse(responseCreateDtoList.get(0));
                return ResponseEntity.status(HttpStatus.CREATED).body(createdResponse);
            }
            
            // Si hay múltiples respuestas, procesarlas todas
            List<ResponseDto> createdResponses = new java.util.ArrayList<>();
            for (ResponseCreateDto dto : responseCreateDtoList) {
                ResponseDto createdResponse = responseService.createResponse(dto);
                createdResponses.add(createdResponse);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(createdResponses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
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