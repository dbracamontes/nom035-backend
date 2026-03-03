package com.example.nom035.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractPrepareResponseDto {

    private String templateType;
    private List<Long> sourceJobIds = new ArrayList<>();
    private List<DocumentTemplateFieldDto> fields = new ArrayList<>();
    private Map<String, String> suggestedValues = new HashMap<>();
    private String combinedPreview;

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public List<Long> getSourceJobIds() {
        return sourceJobIds;
    }

    public void setSourceJobIds(List<Long> sourceJobIds) {
        this.sourceJobIds = sourceJobIds;
    }

    public List<DocumentTemplateFieldDto> getFields() {
        return fields;
    }

    public void setFields(List<DocumentTemplateFieldDto> fields) {
        this.fields = fields;
    }

    public Map<String, String> getSuggestedValues() {
        return suggestedValues;
    }

    public void setSuggestedValues(Map<String, String> suggestedValues) {
        this.suggestedValues = suggestedValues;
    }

    public String getCombinedPreview() {
        return combinedPreview;
    }

    public void setCombinedPreview(String combinedPreview) {
        this.combinedPreview = combinedPreview;
    }
}
