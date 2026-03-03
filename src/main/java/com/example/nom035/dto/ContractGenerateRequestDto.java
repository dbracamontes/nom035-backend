package com.example.nom035.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

public class ContractGenerateRequestDto {

    @NotBlank
    private String templateType;

    private Map<String, String> fields = new HashMap<>();

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
