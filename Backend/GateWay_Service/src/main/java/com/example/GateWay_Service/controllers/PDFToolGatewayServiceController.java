package com.example.GateWay_Service.controllers;

import com.example.GateWay_Service.services.PdfGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173")
public class PDFToolGatewayServiceController {

    private final PdfGatewayService pdfGatewayService;

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs(@RequestParam("files")List<MultipartFile> files){

        return pdfGatewayService.mergedPdfs(files);
    }


    @PostMapping("/split")
    public ResponseEntity<byte[]> splitPdf(@RequestParam("file") MultipartFile file, @RequestParam("fromPage") int startPage, @RequestParam("toPage") int endPage){
        return pdfGatewayService.splitPdf(file, startPage, endPage);
    }

}
