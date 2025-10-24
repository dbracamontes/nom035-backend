package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
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
	public Gender getGender() { return gender; }
	public void setGender(Gender gender) { this.gender = gender; }
	public Integer getAge() { return age; }
	public void setAge(Integer age) { this.age = age; }
	public EmployeeStatus getStatus() { return status; }
	public void setStatus(EmployeeStatus status) { this.status = status; }
	public Company getCompany() { return company; }
	public void setCompany(Company company) { this.company = company; }
	    @Column(unique = true)
	    private String email;

	    private String position;

	    private String department;

	    private Integer seniorityYears;

	    @Enumerated(EnumType.STRING)
	    private Gender gender;

	    private Integer age;

	    @Enumerated(EnumType.STRING)
	    private EmployeeStatus status;

	    @OneToMany(mappedBy = "employee")
	    private List<SurveyApplication> surveyApplications;

    public enum Gender {
        M, F, Other
    }
    
    public enum EmployeeStatus {
        activo, inactivo
    }
}