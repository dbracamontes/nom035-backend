package com.example.nom035.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EmployeeDocsCreateDto {
    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private Long employeeId;

    @NotNull
    private Long typeId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
}
