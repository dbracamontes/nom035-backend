package com.example.nom035.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.example.nom035.entity.User;

public class UserWithRolesDto {
    private Long id;
    private String username;
    private String email;
    private Long companyId;
    private String companyName;
    private Long employeeId;
    private String employeeName;
    private boolean enabled;
    private List<RoleDto> roles;

    public static UserWithRolesDto fromEntity(User user, String companyName, String employeeName) {
        UserWithRolesDto dto = new UserWithRolesDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCompanyId(user.getCompanyId());
        dto.setCompanyName(companyName);
        dto.setEmployeeId(user.getEmployeeId());
        dto.setEmployeeName(employeeName);
        dto.setEnabled(user.isEnabled());
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                .map(RoleDto::fromEntity)
                .collect(Collectors.toList()));
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }
}
