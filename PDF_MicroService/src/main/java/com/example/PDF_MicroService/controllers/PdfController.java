package com.example.PDF_MicroService.controllers;

import com.example.PDF_MicroService.services.PdfService;
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

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs(@RequestParam("files")List<MultipartFile> files){

        byte[] mergedBytes = pdfService.mergePdfs(files);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename:\"merged.pdf\""); // tell the HttpService that this file is to be downloaded and not operated(read or written) on

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(mergedBytes);
    }

    @PostMapping("/split")
    public ResponseEntity<byte[]> splitPdf(@RequestParam("file") MultipartFile file, @RequestParam("fromPage") int startPage, @RequestParam("toPage") int endPage){

        byte[] splitBytes = pdfService.splitPDF(file, startPage, endPage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename:\"split.pdf\"");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(splitBytes);
    }

}
