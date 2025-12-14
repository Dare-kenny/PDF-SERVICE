package com.example.GateWay_Service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImageGatewayService {

    private final @Qualifier("imageWebClient") WebClient imageWebClient;

    private void validateFile(MultipartFile file){
         if (file == null || file.isEmpty()){
             throw new IllegalArgumentException("Please upload an image file");
         }
    }


    private MultipartBodyBuilder buildMultiPartWithFile(MultipartFile file) throws Exception{

        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        ByteArrayResource resource = new ByteArrayResource(file.getBytes()){

            @Override
            public String getFilename(){
                return file.getOriginalFilename();
            }
        };

        builder.part("file",resource)
                .filename(file.getOriginalFilename())
                .contentType(MediaType.parseMediaType(
                        file.getContentType() != null? file.getContentType(): MediaType.APPLICATION_OCTET_STREAM_VALUE
                ));

        return builder;
    }


    public ResponseEntity<byte[]> resizeImage(MultipartFile file, int width, int height) throws Exception{

        validateFile(file);

        MultipartBodyBuilder bodyBuilder = buildMultiPartWithFile(file);
        bodyBuilder.part("width",String.valueOf(width));
        bodyBuilder.part("height",String.valueOf(height));


        return imageWebClient.post()
                .uri("/api/image/resize")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .toEntity(byte[].class)
                .block();

    }


    public ResponseEntity<byte[]> convertImage(MultipartFile file, String format) throws Exception{

        validateFile(file);

        MultipartBodyBuilder bodyBuilder = buildMultiPartWithFile(file);
        bodyBuilder.part("format",format);

        return imageWebClient.post()
                .uri("/api/image/convert")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .toEntity(byte[].class)
                .block();
    }


    public ResponseEntity<byte[]> compressImage(MultipartFile file, int quality) throws Exception{

        validateFile(file);

        MultipartBodyBuilder bodyBuilder = buildMultiPartWithFile(file);

        bodyBuilder.part("quality",String.valueOf(quality));

        return imageWebClient.post()
                .uri("/api/image/compress")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .toEntity(byte[].class)
                .block();

    }

}

