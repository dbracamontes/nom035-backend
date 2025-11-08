package com.example.nom035.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.example.nom035.entity.Role;

public class RoleDto {
    private Long id;
    private String name;
    private List<PrivilegeDto> privileges;

    public static RoleDto fromEntity(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        if (role.getPrivileges() != null) {
            dto.setPrivileges(role.getPrivileges().stream()
                .map(priv -> new PrivilegeDto(priv.getId(), priv.getName()))
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PrivilegeDto> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<PrivilegeDto> privileges) {
        this.privileges = privileges;
    }
}
