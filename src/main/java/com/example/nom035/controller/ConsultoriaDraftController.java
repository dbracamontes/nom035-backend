package com.example.nom035.controller;

import com.example.nom035.dto.ConsultoriaDraftDto;
import com.example.nom035.dto.ConsultoriaDraftUpsertRequest;
import com.example.nom035.entity.ConsultoriaDraft;
import com.example.nom035.entity.User;
import com.example.nom035.repository.UserRepository;
import com.example.nom035.service.ConsultoriaDraftService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/consultoria-drafts")
@Secured({"ROLE_ADMIN", "ROLE_COMPANY", "ROLE_GENERADOR", "ROLE_COTIZADOR"})
public class ConsultoriaDraftController {

    private final ConsultoriaDraftService consultoriaDraftService;
    private final UserRepository userRepository;

    public ConsultoriaDraftController(ConsultoriaDraftService consultoriaDraftService, UserRepository userRepository) {
        this.consultoriaDraftService = consultoriaDraftService;
        this.userRepository = userRepository;
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ConsultoriaDraftDto> getByCompany(@PathVariable Long companyId) {
        validateAccessToCompany(companyId);
        Optional<ConsultoriaDraft> draftOpt = consultoriaDraftService.getByCompanyId(companyId);
        return draftOpt.map(draft -> ResponseEntity.ok(ConsultoriaDraftDto.fromEntity(draft)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/company/{companyId}")
    public ResponseEntity<ConsultoriaDraftDto> upsertByCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody ConsultoriaDraftUpsertRequest request
    ) {
        validateAccessToCompany(companyId);
        ConsultoriaDraft saved = consultoriaDraftService.upsertByCompanyId(companyId, request.getPayload());
        return ResponseEntity.ok(ConsultoriaDraftDto.fromEntity(saved));
    }

    private void validateAccessToCompany(Long companyId) {
        if (isAdmin()) {
            return;
        }
        Long currentCompanyId = getCompanyIdForCurrentUser();
        if (currentCompanyId == null || !currentCompanyId.equals(companyId)) {
            throw new ResponseStatusException(FORBIDDEN, "No autorizado para acceder a esta empresa");
        }
    }

    private Long getCompanyIdForCurrentUser() {
        User user = getCurrentUser();
        return user != null ? user.getCompanyId() : null;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
