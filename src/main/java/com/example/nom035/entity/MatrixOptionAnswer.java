package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matrix_option_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrixOptionAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /** Fila de la matriz (por ejemplo, nombre del síntoma) */
    @Column(nullable = false)
    private String category;

    /** Columna de la matriz (por ejemplo, "Muchas veces", "A veces", "Nunca") */
    @Column(nullable = false)
    private String text;

    /** Ponderación asociada a la combinación questionId-category-text */
    @Column(name = "value", nullable = false)
    private Integer value;
}
