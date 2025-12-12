package com.example.ImageTools_MicroService.services;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageToolServices {

    private void validateFilePresence(MultipartFile file){
        if (file.isEmpty() || file == null){
            throw new IllegalArgumentException("No file was uploaded or file is empty");
        }
    }

    private void validateImageDimensions(int width, int height){

        if (width <=0 || height<=0){
            throw new IllegalArgumentException("Invalid Width and height values");
        }
    }

    private void validateImageQuality(int quality){

        if( quality < 1 || quality > 100){
            throw new IllegalArgumentException("Quality must be between 1 and 100");
        }
    }


    public byte[] resizeImage(MultipartFile file , int width, int height) throws IOException {

        validateFilePresence(file);
        validateImageDimensions(width, height);

        BufferedImage resized = Thumbnails
                .of(file.getInputStream())
                .size(width,height)
                .asBufferedImage();

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(resized, "png",baos);
            return baos.toByteArray();
        }
    }


    public byte[] compressImage(MultipartFile file, int quality ) throws IOException{

        validateFilePresence(file);
        validateImageQuality(quality);

        double q = quality / 100.0;

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Thumbnails
                    .of(file.getInputStream())
                    .scale(1.0)
                    .outputFormat("jpg")
                    .outputQuality(q)
                    .toOutputStream(baos);

            return baos.toByteArray();
        }
    }


    public byte[] convertImage(MultipartFile file, String targetFormat) throws IOException{

        validateFilePresence(file);

        if (targetFormat == null || targetFormat.isBlank()){
            throw new IllegalArgumentException("Please enter a target format.");
        }

        String format = targetFormat.toLowerCase().trim();

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Thumbnails
                    .of(file.getInputStream())
                    .scale(1.0)
                    .outputFormat(format)
                    .toOutputStream(baos);

            return baos.toByteArray();
        }
    }
}
