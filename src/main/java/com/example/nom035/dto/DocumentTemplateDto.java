package com.example.nom035.dto;

import java.util.ArrayList;
import java.util.List;

public class DocumentTemplateDto {
    private String type;
    private String name;
    private boolean enabled;
    private List<DocumentTemplateFieldDto> fields = new ArrayList<>();

    public DocumentTemplateDto() {
    }

    public DocumentTemplateDto(String type, String name, boolean enabled) {
        this.type = type;
        this.name = name;
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<DocumentTemplateFieldDto> getFields() {
        return fields;
    }

    public void setFields(List<DocumentTemplateFieldDto> fields) {
        this.fields = fields;
    }
}
