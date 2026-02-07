package com.example.nom035.dto;

public class DocumentPreviewChunkDto {
    private int chunkIndex;
    private int pageStart;
    private int pageEnd;
    private String rawText;
    private String interpretedText;

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
