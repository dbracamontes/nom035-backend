package com.example.nom035.dto;

import jakarta.validation.constraints.NotBlank;

public class ConsultoriaDraftUpsertRequest {

    @NotBlank
    private String payload;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
