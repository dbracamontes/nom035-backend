package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionAnswer {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private String text;

    private Integer value;

    private Integer sortOrder;

    @OneToMany(mappedBy = "optionAnswer")
    private List<Response> responses;
}