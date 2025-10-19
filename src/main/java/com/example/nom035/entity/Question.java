package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "question")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "survey_id")
    private Long surveyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false, insertable = false, updatable = false)
    @JsonIgnore
    private Survey survey;

    @Column(nullable = false)
    private String text;

    private String category;

    @Column(name = "response_type")
    private String type;

    @Column(name = "guide_type")
    private String guideType;

    private Integer sortOrder;

    private String riskFactor;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OptionAnswer> options;
}