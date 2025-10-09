package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
	    @JsonBackReference
	    private Company company;

	    @Column(nullable = false)
	    private String name;

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