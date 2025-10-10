package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    @JsonIgnore
    private Survey survey;

    @Column(nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    private ResponseType responseType;

    private Integer sortOrder;

    private String riskFactor;

    private String category; // útil para dashboards y agrupaciones

    @OneToMany(mappedBy = "question")
    private List<OptionAnswer> optionAnswers;
    
    public enum ResponseType {
        likert, multiple_choice, open
    }
}