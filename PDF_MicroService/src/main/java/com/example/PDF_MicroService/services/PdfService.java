package com.example.PDF_MicroService.services;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    // PDF files are binary data which is represented as byte[]
    public byte[] mergePdfs(List<MultipartFile> files) {

        try {
            PDFMergerUtility merger = new PDFMergerUtility();

            for (MultipartFile file : files) {
                merger.addSource(new ByteArrayInputStream(file.getBytes()));
            }

            // BUG FIX: outputStream must be set BEFORE mergeDocuments() is called.
            // Also removed deprecated MemoryUsageSetting — pass null to use default in-memory mode.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            merger.setDestinationStream(outputStream);
            merger.mergeDocuments(null);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to merge PDFs", e);
        }
    }


    public byte[] splitPDF(MultipartFile file, int startPage, int endPage) {

        if (startPage <= 0 || endPage < startPage) {
            throw new IllegalArgumentException("Invalid page range");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        // BUG FIX: splitDoc must be saved to outputStream BEFORE the try-with-resources
        // closes it. We capture the bytes inside the block, then return them after.
        try (
            PDDocument original = PDDocument.load(new ByteArrayInputStream(fileBytes));
            PDDocument splitDoc = new PDDocument()
        ) {
            int totalPages = original.getNumberOfPages();

            if (startPage > totalPages) {
                throw new IllegalArgumentException("Page range exceeds original document length");
            }

            int clampedEnd = Math.min(endPage, totalPages);

            for (int i = startPage - 1; i <= clampedEnd - 1; i++) {
                splitDoc.importPage(original.getPage(i));
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            splitDoc.save(outputStream);
            // Return inside the try block so the document is still open when saved
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to split PDF", e);
        }
    }
}
