package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanySurvey {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnore
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    private LocalDate assignedAt;

    private LocalDate dueDate;

    private String companyVersion;

    @Enumerated(EnumType.STRING)
    private SurveyStatus status;

    private BigDecimal completionRate;

    private String notes;

    @OneToMany(mappedBy = "companySurvey")
    @JsonIgnore
    private List<SurveyApplication> surveyApplications;

    public enum SurveyStatus  {
        activo, inactivo
    }
}