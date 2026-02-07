package com.example.nom035.service;

import com.example.nom035.entity.DocumentChunk;
import com.example.nom035.entity.DocumentJob;
import com.example.nom035.entity.DocumentOcrPage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class DocumentChunkingService {

    private static final Pattern HEADING_PATTERN = Pattern.compile("(?m)^(CAP[IÍ]TULO|ART[IÍ]CULO|CL[AÁ]USULA)");
    private static final int MAX_PAGES_PER_CHUNK = 3;

    public List<DocumentChunk> buildChunks(DocumentJob job, List<DocumentOcrPage> pages) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (pages.isEmpty()) {
            return chunks;
        }

        StringBuilder buffer = new StringBuilder();
        int currentStart = pages.get(0).getPageNumber();
        int currentCount = 0;
        int index = 0;

        for (int i = 0; i < pages.size(); i++) {
            DocumentOcrPage page = pages.get(i);
            String text = page.getText() != null ? page.getText() : "";
            boolean heading = HEADING_PATTERN.matcher(text).find();

            boolean mustBreak = (heading && currentCount > 0) || currentCount >= MAX_PAGES_PER_CHUNK;
            if (mustBreak) {
                appendChunk(chunks, job, index++, currentStart, page.getPageNumber() - 1, buffer.toString());
                buffer = new StringBuilder();
                currentStart = page.getPageNumber();
                currentCount = 0;
            }

            buffer.append("[Página ").append(page.getPageNumber()).append("]\n");
            buffer.append(text).append("\n\n");
            currentCount++;

            boolean reachedLimit = currentCount >= MAX_PAGES_PER_CHUNK;
            boolean lastPage = i == pages.size() - 1;
            if (reachedLimit || lastPage) {
                appendChunk(chunks, job, index++, currentStart, page.getPageNumber(), buffer.toString());
                buffer = new StringBuilder();
                if (!lastPage) {
                    currentStart = page.getPageNumber() + 1;
                    currentCount = 0;
                }
            }
        }
        return chunks;
    }

    private void appendChunk(List<DocumentChunk> chunks, DocumentJob job, int index, int start, int end, String text) {
        DocumentChunk chunk = new DocumentChunk();
        chunk.setJob(job);
        chunk.setChunkIndex(index);
        chunk.setPageStart(start);
        chunk.setPageEnd(end);
        chunk.setRawText(text);
        chunks.add(chunk);
    }
}
