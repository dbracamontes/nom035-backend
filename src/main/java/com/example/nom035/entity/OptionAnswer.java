package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "option_answer") // Mantengo el nombre de tabla actual
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionAnswer {
    public Long getId() { return id; }
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnore
    private Question question;

    private Integer value;
    private String text;

    private Integer sortOrder;

    @OneToMany(mappedBy = "optionAnswer")
    @JsonIgnore
    private List<Response> responses;
}