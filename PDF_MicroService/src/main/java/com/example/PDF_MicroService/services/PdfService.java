package com.example.PDF_MicroService.services;

import org.apache.pdfbox.io.MemoryUsageSetting;
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
    public byte[] mergePdfs(List<MultipartFile> files){ // Multipartfile represents a file uploaded through a HTTP request

        PDFMergerUtility merger = new PDFMergerUtility(); // to take multiple PDF inputs and merge them into a single pdf

        try{ // to write the merged PDF into this in-memory stream

            for(MultipartFile file : files){ // for each file uploaded
                merger.addSource(new ByteArrayInputStream(file.getBytes())); // add these read PDF bytes into the final merged documents
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            merger.setDestinationStream(outputStream); // write the final merged PDF to the in-memory stream
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly()); // merge using default merging settings

            return outputStream.toByteArray(); // return a byte containing full merged PDF

        } catch (Exception e) {
            throw new RuntimeException("Failed to merge PDFs",e);
        }

    }


    public byte[] splitPDF(MultipartFile file,int startPage, int endPage){

        //validate values entered
        if (startPage <= 0 || endPage < startPage){
            throw new IllegalArgumentException("Invalid page range");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        try(
            PDDocument original = PDDocument.load(new ByteArrayInputStream(fileBytes)); // get the original Document being uploaded
            PDDocument splitDoc = new PDDocument(); // new document to hold the selected page

        ) {
            int totalPages = original.getNumberOfPages(); // get number of pages of original document for validation purposes

            if(startPage > totalPages){ // validate input value
                throw new IllegalArgumentException("Page range exceeds original document length");
            }

            int clampedEnd = Math.min(endPage, totalPages);

            for (int i = startPage - 1; i <= clampedEnd - 1; i++) {
                splitDoc.importPage(original.getPage(i));
            }

            // Output stream intentionally outside try-with-resources — see mergePdfs note.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            splitDoc.save(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
