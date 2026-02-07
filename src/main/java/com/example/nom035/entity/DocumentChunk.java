package com.example.nom035.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "doc_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private DocumentJob job;

    @Column(nullable = false)
    private int chunkIndex;

    @Column(nullable = false)
    private int pageStart;

    @Column(nullable = false)
    private int pageEnd;

    @Lob
    @Column(name = "raw_text", columnDefinition = "LONGTEXT")
    private String rawText;

    @Lob
    @Column(name = "interpreted_text", columnDefinition = "LONGTEXT")
    private String interpretedText;

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

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public int getPageStart() {
        return pageStart;
    }

    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    public int getPageEnd() {
        return pageEnd;
    }

    public void setPageEnd(int pageEnd) {
        this.pageEnd = pageEnd;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getInterpretedText() {
        return interpretedText;
    }

    public void setInterpretedText(String interpretedText) {
        this.interpretedText = interpretedText;
    }
}
