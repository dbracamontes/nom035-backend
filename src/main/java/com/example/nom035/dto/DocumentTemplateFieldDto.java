package com.example.nom035.dto;

public class DocumentTemplateFieldDto {
    private String key;
    private String label;
    private boolean required;

    public DocumentTemplateFieldDto() {
    }

    public DocumentTemplateFieldDto(String key, String label, boolean required) {
        this.key = key;
        this.label = label;
        this.required = required;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
