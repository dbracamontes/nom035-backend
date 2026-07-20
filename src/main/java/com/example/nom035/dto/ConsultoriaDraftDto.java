package com.example.nom035.dto;

import com.example.nom035.entity.ConsultoriaDraft;

import java.time.LocalDateTime;

public class ConsultoriaDraftDto {

    private Long id;
    private Long companyId;
    private String payload;
    private LocalDateTime updatedAt;

    public static ConsultoriaDraftDto fromEntity(ConsultoriaDraft entity) {
        ConsultoriaDraftDto dto = new ConsultoriaDraftDto();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompanyId());
        dto.setPayload(entity.getPayload());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
