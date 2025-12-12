package com.example.GateWay_Service.controllers;

import com.example.GateWay_Service.services.ImageGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageToolGatewayServiceController {

    private final ImageGatewayService imageGatewayService;

    @PostMapping("/resize")
    public ResponseEntity<byte[]> resizeImage(@RequestParam("file") MultipartFile file, @RequestParam("width") int width, @RequestParam("height") int height) throws Exception{

        return imageGatewayService.resizeImage(file, width, height);
    }

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convertImage(@RequestParam("file") MultipartFile file, @RequestParam("format") String format) throws Exception{

        return imageGatewayService.convertImage(file,format);
    }

    @PostMapping("/compress")
    public ResponseEntity<byte[]> compressImage(@RequestParam("file") MultipartFile file , @RequestParam("quality") int quality) throws Exception{
        return imageGatewayService.compressImage(file, quality);
    }
}
