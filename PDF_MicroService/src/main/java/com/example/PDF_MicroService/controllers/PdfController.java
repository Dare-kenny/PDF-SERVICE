package com.example.PDF_MicroService.controllers;

import com.example.PDF_MicroService.services.DocxService;
import com.example.PDF_MicroService.services.PdfService;
import com.example.PDF_MicroService.util.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pdf")
public class PdfController {

    private final PdfService pdfService;
    private final DocxService docxService;


    //Utilities

    private ResponseEntity<byte[]> buildResponse(byte[] body, DocumentType type, String filePrefix) {
        MediaType mediaType = switch (type) {
            case PDF -> MediaType.APPLICATION_PDF;
            case DOCX -> MediaType.parseMediaType(type.getMimeType());
        };

        String filename = filePrefix + type.getExtension();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }


    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs(@RequestParam("files") List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        DocumentType type = DocumentType.detect(files.get(0));


        byte[] resultBytes = switch (type) {
            case PDF -> pdfService.mergePdfs(files);
            case DOCX -> docxService.mergeDocx(files);
        };

        return buildResponse(resultBytes, type, "merged");
    }

    @PostMapping("/split")
    public ResponseEntity<byte[]> splitPdf(@RequestParam("file") MultipartFile file, @RequestParam("fromPage") int startPage, @RequestParam("toPage") int endPage) {

        DocumentType type = DocumentType.detect(file);

        byte[] resultBytes = switch (type) {
            case PDF -> pdfService.splitPDF(file, startPage, endPage);
            case DOCX -> docxService.splitDocx(file, startPage, endPage);
        };

        return buildResponse(resultBytes, type, "split");
    }

}

