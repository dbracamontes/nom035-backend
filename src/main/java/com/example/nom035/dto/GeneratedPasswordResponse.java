package com.example.nom035.dto;

public class GeneratedPasswordResponse {
    private Long userId;
    private String temporaryPassword;

    public GeneratedPasswordResponse(Long userId, String temporaryPassword) {
        this.userId = userId;
        this.temporaryPassword = temporaryPassword;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }
}
