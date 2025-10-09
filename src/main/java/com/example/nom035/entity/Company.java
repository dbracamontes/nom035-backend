package com.example.nom035.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 20)
    private String taxId;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "company")
    @JsonManagedReference
    private List<Employee> employees;

    @OneToMany(mappedBy = "company")
    @JsonManagedReference
    private List<CompanySurvey> companySurveys;
}