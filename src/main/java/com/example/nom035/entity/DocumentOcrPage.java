package com.example.nom035.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doc_ocr_page")
public class DocumentOcrPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private DocumentJob job;

    @Column(nullable = false)
    private int pageNumber;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String text;

    private Double confidence;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentJob getJob() {
        return job;
    }

    public void setJob(DocumentJob job) {
        this.job = job;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
