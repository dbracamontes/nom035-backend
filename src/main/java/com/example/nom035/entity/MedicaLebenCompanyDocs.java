package com.example.nom035.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "medica_leben_company_docs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicaLebenCompanyDocs {
    public enum DocumentStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DocumentStatus status = DocumentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "acta_constitutiva_status", nullable = false, length = 16)
    private DocumentStatus actaConstitutivaStatus = DocumentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "asamblea_status", nullable = false, length = 16)
    private DocumentStatus asambleaStatus = DocumentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "constancia_situacion_fiscal_status", nullable = false, length = 16)
    private DocumentStatus constanciaSituacionFiscalStatus = DocumentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "poder_notarial_status", nullable = false, length = 16)
    private DocumentStatus poderNotarialStatus = DocumentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "identificacion_representante_status", nullable = false, length = 16)
    private DocumentStatus identificacionRepresentanteStatus = DocumentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "comprobante_domicilio_status", nullable = false, length = 16)
    private DocumentStatus comprobanteDomicilioStatus = DocumentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cuenta_bancaria_status", nullable = false, length = 16)
    private DocumentStatus estadoCuentaBancariaStatus = DocumentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "comprobante_ema_eba_status", nullable = false, length = 16)
    private DocumentStatus comprobanteEmaEbaStatus = DocumentStatus.PENDING;

    @Column(name = "acta_constitutiva")
    private String actaConstitutiva;

    @Column(name = "asamblea")
    private String asamblea;

    @Column(name = "constancia_situacion_fiscal")
    private String constanciaSituacionFiscal;

    @Column(name = "poder_notarial")
    private String poderNotarial;

    @Column(name = "identificacion_representante")
    private String identificacionRepresentante;

    @Column(name = "comprobante_domicilio")
    private String comprobanteDomicilio;

    @Column(name = "estado_cuenta_bancaria")
    private String estadoCuentaBancaria;

    @Column(name = "comprobante_ema_eba")
    private String comprobanteEmaEba;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "companyDocs", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicaLebenCompanyWorkPhoto> workPhotos;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
