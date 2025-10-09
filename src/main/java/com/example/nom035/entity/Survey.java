package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200, nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private GuideType guideType = GuideType.Custom;

    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "base_survey_id")
    private Survey baseSurvey;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "survey")
    @JsonManagedReference
    private List<Question> questions;

    @OneToMany(mappedBy = "survey")
    private List<CompanySurvey> companySurveys;

    public enum GuideType {
        I, II, III, Custom
    }
}