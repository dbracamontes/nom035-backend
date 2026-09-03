package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "medica_leben_company_work_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicaLebenCompanyWorkPhoto {
    public enum PhotoStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_docs_id", nullable = false)
    private MedicaLebenCompanyDocs companyDocs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PhotoStatus status = PhotoStatus.PENDING;

    @Column(nullable = false)
    private String url;

    private String description;

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
