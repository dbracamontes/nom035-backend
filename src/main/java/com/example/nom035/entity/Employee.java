package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "company_id", nullable = false)
	    private Company company;

	    @Column(nullable = false)
	    private String name;

	    @Column(unique = true)
	    private String email;

	    private String position;

	    private String department;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "marital_status", length = 50)
    private String maritalStatus;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 200)
    private String education;

    @Column(name = "company_category", length = 100)
    private String companyCategory;

    private Integer seniorityYears;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @Column(length = 18)
    private String curp;

    @OneToMany(mappedBy = "employee")
    private List<SurveyApplication> surveyApplications;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<EmployeeDocs> documents = new java.util.ArrayList<>();

    public enum Gender {
        M, F, Otro
    }
    
    public enum EmployeeStatus {
        activo, inactivo
    }

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
	public LocalDate getDateOfBirth() { return dateOfBirth; }
	public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
	public String getMaritalStatus() { return maritalStatus; }
	public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
	public Gender getGender() { return gender; }
	public void setGender(Gender gender) { this.gender = gender; }
	public String getEducation() { return education; }
	public void setEducation(String education) { this.education = education; }
	public String getCompanyCategory() { return companyCategory; }
	public void setCompanyCategory(String companyCategory) { this.companyCategory = companyCategory; }
	public Integer getSeniorityYears() { return seniorityYears; }
	public void setSeniorityYears(Integer seniorityYears) { this.seniorityYears = seniorityYears; }
	public Integer getAge() { return age; }
	public void setAge(Integer age) { this.age = age; }
	public EmployeeStatus getStatus() { return status; }
	public void setStatus(EmployeeStatus status) { this.status = status; }
	public Company getCompany() { return company; }
	public void setCompany(Company company) { this.company = company; }
	public String getCurp() { return curp; }
	public void setCurp(String curp) { this.curp = curp; }
	public java.util.List<EmployeeDocs> getDocuments() { return documents; }
	public void setDocuments(java.util.List<EmployeeDocs> documents) { this.documents = documents; }
}
