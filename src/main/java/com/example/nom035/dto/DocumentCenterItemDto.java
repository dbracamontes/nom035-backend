package com.example.nom035.dto;

import java.util.List;

public class DocumentCenterItemDto {
    private Long id;
    private Long employeeId;
    private Long companyId;
    private Long companyDocId;
    private String documentKey;
    private String title;
    private String division;
    private String module;
    private String category;
    private String status;
    private String owner;
    private String uploadedAt;
    private String fileType;
    private String size;
    private String relatedTo;
    private List<String> tags;
    private String security;
    private String previewText;
    private String source;
    private boolean requiresApproval;
    private String downloadUrl;
    private String previewUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getCompanyDocId() { return companyDocId; }
    public void setCompanyDocId(Long companyDocId) { this.companyDocId = companyDocId; }
    public String getDocumentKey() { return documentKey; }
    public void setDocumentKey(String documentKey) { this.documentKey = documentKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getRelatedTo() { return relatedTo; }
    public void setRelatedTo(String relatedTo) { this.relatedTo = relatedTo; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getSecurity() { return security; }
    public void setSecurity(String security) { this.security = security; }
    public String getPreviewText() { return previewText; }
    public void setPreviewText(String previewText) { this.previewText = previewText; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public boolean isRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(boolean requiresApproval) { this.requiresApproval = requiresApproval; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
}
