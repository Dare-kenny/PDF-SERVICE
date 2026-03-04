package com.example.PDF_MicroService.services;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


//Handles merge and split operations for Word (.docx) documents using Apache POI's XWPF API.

@Service
public class DocxService {

    public byte[] mergeDocx(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for merging");
        }

        try (XWPFDocument merged = new XWPFDocument()) {

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

                    // Copy all body elements from source into merged document
                    copyDocumentContent(source, merged);
                    isFirstDoc = false;
                }
            }


            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            merged.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
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

        try (XWPFDocument original = new XWPFDocument(new ByteArrayInputStream(fileBytes));
             XWPFDocument split = new XWPFDocument();
        ) {
            List<XWPFParagraph> paragraphs = original.getParagraphs();
            int totalParagraphs = paragraphs.size();

            if (startPara > totalParagraphs) {
                throw new IllegalArgumentException(
                        "startPara (" + startPara + ") exceeds total paragraph count (" + totalParagraphs + ")"
                );
            }

            // Clamp endPara to the actual document length
            int clampedEnd = Math.min(endPara, totalParagraphs);

            // Copy paragraphs in the specified range (convert from 1-based to 0-based)
            for (int i = startPara - 1; i < clampedEnd; i++) {
                copyParagraph(paragraphs.get(i), split);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            split.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to split DOCX file", e);
        }
    }

    // Private helpers

    //Copies all paragraphs and tables from a source document into a target document.
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

        // Copy paragraph-level style (alignment, spacing, etc.) via underlying CTP XML
        if (source.getCTP() != null) {
            newPara.getCTP().set(source.getCTP().copy());
        }

        // Copy each run's text and character formatting
        for (XWPFRun sourceRun : source.getRuns()) {
            XWPFRun newRun = newPara.createRun();
            newRun.setText(sourceRun.getText(0));
            newRun.setBold(sourceRun.isBold());
            newRun.setItalic(sourceRun.isItalic());
            newRun.setUnderline(sourceRun.getUnderline());
            newRun.setStrike(sourceRun.isStrike());
            newRun.setFontSize(sourceRun.getFontSize());
            if (sourceRun.getFontFamily() != null) {
                newRun.setFontFamily(sourceRun.getFontFamily());
            }
            if (sourceRun.getColor() != null) {
                newRun.setColor(sourceRun.getColor());
            }
        }
    }

    //Deep-copies a table (rows and cells) into the target document.
    private void copyTable(XWPFTable source, XWPFDocument target) {
        XWPFTable newTable = target.createTable();

        // Copy table XML directly for full fidelity
        newTable.getCTTbl().set(source.getCTTbl().copy());
    }
}
