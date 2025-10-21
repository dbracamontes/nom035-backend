package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "option_answer") // Mantengo el nombre de tabla que ya existe
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    private Integer value;
    private String label;  // Según tu guía
    private String text;   // Mantengo este también por compatibilidad

    // Getters y setters ya están incluidos con @Data
}