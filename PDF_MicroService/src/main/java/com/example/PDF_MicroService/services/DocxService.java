package com.example.PDF_MicroService.services;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


// Handles merge and split operations for Word (.docx) documents using Apache POI's XWPF API.

@Service
public class DocxService {

    public byte[] mergeDocx(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for merging");
        }

        // BUG FIX: Do NOT use try-with-resources for `merged` here.
        // If merged is in a try-with-resources block, it gets closed before
        // outputStream.toByteArray() is returned, producing 0 bytes.
        // We manually close it after writing.
        XWPFDocument merged = new XWPFDocument();
        try {
            boolean isFirstDoc = true;

            for (MultipartFile file : files) {
                byte[] fileBytes = file.getBytes();

                try (XWPFDocument source = new XWPFDocument(new ByteArrayInputStream(fileBytes))) {
                    // Add a page break between documents (not before the first one)
                    if (!isFirstDoc) {
                        XWPFParagraph pageBreak = merged.createParagraph();
                        XWPFRun run = pageBreak.createRun();
                        run.addBreak(BreakType.PAGE);
                    }

                    copyDocumentContent(source, merged);
                    isFirstDoc = false;
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            merged.write(outputStream);
            // Capture bytes while document is still open, then close
            byte[] result = outputStream.toByteArray();
            merged.close();
            return result;

        } catch (IOException e) {
            try { merged.close(); } catch (IOException ignored) {}
            throw new RuntimeException("Failed to merge DOCX files", e);
        }
    }


    public byte[] splitDocx(MultipartFile file, int startPara, int endPara) {
        if (startPara <= 0 || endPara < startPara) {
            throw new IllegalArgumentException(
                    "Invalid paragraph range: startPara must be >= 1 and endPara must be >= startPara"
            );
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        // BUG FIX: Same as mergeDocx — `split` must NOT be in try-with-resources
        // if we need to call split.write(outputStream) and return the bytes.
        // We manually close after capturing the result.
        try (XWPFDocument original = new XWPFDocument(new ByteArrayInputStream(fileBytes))) {
            XWPFDocument split = new XWPFDocument();
            try {
                List<XWPFParagraph> paragraphs = original.getParagraphs();
                int totalParagraphs = paragraphs.size();

                if (startPara > totalParagraphs) {
                    throw new IllegalArgumentException(
                            "startPara (" + startPara + ") exceeds total paragraph count (" + totalParagraphs + ")"
                    );
                }

                int clampedEnd = Math.min(endPara, totalParagraphs);

                for (int i = startPara - 1; i < clampedEnd; i++) {
                    copyParagraph(paragraphs.get(i), split);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                split.write(outputStream);
                byte[] result = outputStream.toByteArray();
                split.close();
                return result;

            } catch (IOException e) {
                try { split.close(); } catch (IOException ignored) {}
                throw new RuntimeException("Failed to split DOCX file", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to split DOCX file", e);
        }
    }

    // Private helpers

    // Copies all paragraphs and tables from a source document into a target document.
    private void copyDocumentContent(XWPFDocument source, XWPFDocument target) {
        for (IBodyElement element : source.getBodyElements()) {
            if (element instanceof XWPFParagraph sourcePara) {
                copyParagraph(sourcePara, target);
            } else if (element instanceof XWPFTable sourceTable) {
                copyTable(sourceTable, target);
            }
        }
    }

    // Deep-copies a paragraph (including all runs and their formatting) into the target document.
    private void copyParagraph(XWPFParagraph source, XWPFDocument target) {
        XWPFParagraph newPara = target.createParagraph();

        // BUG FIX: Use getCTP().set() to copy the ENTIRE paragraph XML (including all runs
        // and formatting) in one shot. Do NOT also loop over runs manually — that would
        // duplicate every run (once from the XML copy, once from the explicit loop).
        if (source.getCTP() != null) {
            newPara.getCTP().set(source.getCTP().copy());
        }
        // Removed the erroneous manual run-copying loop that was here before —
        // getCTP().set() already copies all runs from the XML.
    }

    // Deep-copies a table (rows and cells) into the target document.
    private void copyTable(XWPFTable source, XWPFDocument target) {
        XWPFTable newTable = target.createTable();
        newTable.getCTTbl().set(source.getCTTbl().copy());
    }
}
