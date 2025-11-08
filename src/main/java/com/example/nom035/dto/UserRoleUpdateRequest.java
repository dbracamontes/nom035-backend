package com.example.nom035.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserRoleUpdateRequest {
    private List<Long> roleIds;
    private Boolean enabled;
    private String password;
    private Long companyId;

    @JsonIgnore
    private boolean companyIdSpecified;

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
        this.companyIdSpecified = true;
    }

    @JsonIgnore
    public boolean isCompanyIdSpecified() {
        return companyIdSpecified;
    }
}
