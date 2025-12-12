package com.example.ImageTools_MicroService.controllers;

import com.example.ImageTools_MicroService.services.ImageToolServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageToolServices imageToolServices;

    @PostMapping("/resize")
    public ResponseEntity<byte[]> resizeImage(@RequestParam("file")MultipartFile file, @RequestParam("width") int width, @RequestParam("height") int height) throws Exception{

        byte[] resultInBytes = imageToolServices.resizeImage(file, width, height);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.IMAGE_PNG);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename:\"resized-image.png\"");

        return new ResponseEntity<>(resultInBytes,headers, HttpStatus.OK);
    }

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convertImage(@RequestParam("file") MultipartFile file, @RequestParam("format") String format) throws Exception{

        byte[] resultInBytes = imageToolServices.convertImage(file, format);

        MediaType mediaType = switch (format.toLowerCase()){
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"converted-image."+format.toLowerCase() + "\"");

        return new ResponseEntity<>(resultInBytes,headers,HttpStatus.OK);
    }

    @PostMapping("/compress")
    public ResponseEntity<byte[]> compressImage(@RequestParam("file") MultipartFile file, @RequestParam("quality") int quality) throws Exception{

        byte[] resultInBytes = imageToolServices.compressImage(file, quality);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"compress-image\"");

        return new ResponseEntity<>(resultInBytes,headers,HttpStatus.OK);
    }
}
