package com.example.nom035.dto;

import com.example.nom035.entity.Employee;

public class EmployeeDto {
    private Long id;
    private String name;
    private String email;
    private String position;
    private String department;
    private String dateOfBirth;
    private String maritalStatus;
    private Integer seniorityYears;
    private String gender;
    private String education;
    private String companyCategory;
    private Integer age;
    private String status;
    private Long companyId;
    private String companyName;
    private String curp;

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
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public Integer getSeniorityYears() { return seniorityYears; }
    public void setSeniorityYears(Integer seniorityYears) { this.seniorityYears = seniorityYears; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getCompanyCategory() { return companyCategory; }
    public void setCompanyCategory(String companyCategory) { this.companyCategory = companyCategory; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCurp() { return curp; }
    public void setCurp(String curp) { this.curp = curp; }

    public static EmployeeDto fromEntity(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setEmail(employee.getEmail());
        dto.setPosition(employee.getPosition());
        dto.setDepartment(employee.getDepartment());
        dto.setDateOfBirth(employee.getDateOfBirth() != null ? employee.getDateOfBirth().toString() : null);
        dto.setMaritalStatus(employee.getMaritalStatus());
        dto.setSeniorityYears(employee.getSeniorityYears());
        dto.setGender(employee.getGender() != null ? employee.getGender().name() : null);
        dto.setEducation(employee.getEducation());
        dto.setCompanyCategory(employee.getCompanyCategory());
        dto.setAge(employee.getAge());
        dto.setStatus(employee.getStatus() != null ? employee.getStatus().name() : null);
        dto.setCompanyId(employee.getCompany() != null ? employee.getCompany().getId() : null);
        dto.setCompanyName(employee.getCompany() != null ? employee.getCompany().getName() : null);
        dto.setCurp(employee.getCurp());
        return dto;
    }
}