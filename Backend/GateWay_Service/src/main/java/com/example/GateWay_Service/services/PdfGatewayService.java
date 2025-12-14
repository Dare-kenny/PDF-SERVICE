package com.example.GateWay_Service.services;

import com.example.GateWay_Service.exceptionHandling.InvalidFileTypeExecption;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGatewayService {

    private final @Qualifier("pdfWebClient") WebClient pdfWebClient;

    private boolean isPdf(MultipartFile file){

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        boolean LooksLikePdfByType = contentType != null && contentType.equalsIgnoreCase(MediaType.APPLICATION_PDF_VALUE);
        boolean LooksLikePdfByName = filename != null && filename.toLowerCase().endsWith(".pdf");

        return LooksLikePdfByType || LooksLikePdfByName;
    }

    public ResponseEntity<byte[]> mergedPdfs(List<MultipartFile> files){

        if(files == null || files.isEmpty()){
            throw new InvalidFileTypeExecption("Please upload at least one PDF file.");
        }

        for (MultipartFile file: files){
            if (!isPdf(file)){
                throw new InvalidFileTypeExecption("Only PDF files are allowed for merging.");
            }
        }
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            for (MultipartFile file : files){
                builder.part("files",file.getResource())
                        .filename(file.getOriginalFilename())
                        .contentType(MediaType.APPLICATION_PDF);
            }

            return pdfWebClient.post()
                    .uri("/pdf/merge")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .toEntity(byte[].class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Error calling pdf-service merge endpoint",e);
        }
    }


    public ResponseEntity<byte[]> splitPdf(MultipartFile file, int startPage, int endPage){

        if(file == null || file.isEmpty()){
            throw new InvalidFileTypeExecption("Please upload a PDF file to split");
        }

        if (!isPdf(file)){
                throw new InvalidFileTypeExecption("Only PDF files are allowed for splitting.");
        }

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.part("file", file.getResource())
                    .filename(file.getOriginalFilename())
                    .contentType(MediaType.APPLICATION_PDF);

            builder.part("fromPage", String.valueOf(startPage));
            builder.part("toPage",String.valueOf(endPage));

            return pdfWebClient.post()
                    .uri("/pdf/split")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .toEntity(byte[].class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Error calling pdf-service split endpoint",e);
        }
    }
}


