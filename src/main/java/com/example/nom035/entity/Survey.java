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
    private GuideType guideType = GuideType.Personalizado;

    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "base_survey_id")
    private Survey baseSurvey;

    private LocalDateTime createdAt = LocalDateTime.now();
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    public GuideType getGuideType() { return guideType; }
    public void setGuideType(GuideType guideType) { this.guideType = guideType; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        public Survey getBaseSurvey() { return baseSurvey; }
        public void setBaseSurvey(Survey baseSurvey) { this.baseSurvey = baseSurvey; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    @OneToMany(mappedBy = "survey")
    @JsonManagedReference
    private List<Question> questions;

    @OneToMany(mappedBy = "survey")
    private List<CompanySurvey> companySurveys;

    public enum GuideType {
        I, II, III, Personalizado
    }
}