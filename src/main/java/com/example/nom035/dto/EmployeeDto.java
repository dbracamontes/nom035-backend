package com.example.nom035.dto;

import com.example.nom035.entity.Employee;

public class EmployeeDto {
    private Long id;
    private String name;
    private String email;
    private String position;
    private String department;
    private Integer seniorityYears;
    private String gender;
    private Integer age;
    private String status;
    private Long companyId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getSeniorityYears() { return seniorityYears; }
    public void setSeniorityYears(Integer seniorityYears) { this.seniorityYears = seniorityYears; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public static EmployeeDto fromEntity(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setPosition(employee.getPosition());
        dto.setDepartment(employee.getDepartment());
        dto.setSeniorityYears(employee.getSeniorityYears());
        dto.setGender(employee.getGender() != null ? employee.getGender().name() : null);
        dto.setAge(employee.getAge());
        dto.setStatus(employee.getStatus() != null ? employee.getStatus().name() : null);
        dto.setCompanyId(employee.getCompany() != null ? employee.getCompany().getId() : null);
        return dto;
    }
}