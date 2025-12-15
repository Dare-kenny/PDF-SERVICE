package com.example.ImageTools_MicroService.services;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
public class ImageToolServices {

    private static final Set<String> SUPPORT_FORMATS = Set.of("png","jpg","jpeg");

    private BufferedImage validateAndReadImage(MultipartFile file) throws  IOException{
        if (file == null || file.isEmpty()){
            throw new IllegalArgumentException("No image file uploaded");
        }

        try (InputStream is = file.getInputStream()) {

            BufferedImage image = ImageIO.read(is);

            if (image == null){
                throw new IllegalArgumentException("Uploaded file is not a valid image");
            }

            return image;
        }
    }

    private String normalizeFormat(String format){
        if (format == null || format.isBlank()){
            throw new IllegalArgumentException("Target format is required");
        }

        String f = format.toLowerCase().trim();

        if (f.startsWith(".")){
            f = f.substring(1);
        }

        if (f.contains("/")){
            f = f.substring(f.indexOf("/") + 1);
        }

        if (!SUPPORT_FORMATS.contains(f)){
            throw new IllegalArgumentException("Unsupported format "+f+" , Only PNG and JPG are allowed.");
        }
        return f.equals("jpeg") ? "jpg" : f;
    }


    public byte[] resizeImage(MultipartFile file, int width, int height) throws IOException {

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be greater than zero");
        }

        BufferedImage image = validateAndReadImage(file);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Thumbnails.of(image)
                    .size(width, height)
                    .outputFormat("png")
                    .toOutputStream(baos);

            return baos.toByteArray();
        }
    }

    public byte[] compressImage(MultipartFile file, int quality) throws IOException {

        if (quality < 1 || quality > 100) {
            throw new IllegalArgumentException("Quality must be between 1 and 100");
        }

        BufferedImage image = validateAndReadImage(file);
        double q = quality / 100.0;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Thumbnails.of(image)
                    .scale(1.0)
                    .outputFormat("jpg")
                    .outputQuality(q)
                    .toOutputStream(baos);

            return baos.toByteArray();
        }
    }

    public byte[] convertImage(MultipartFile file, String targetFormat) throws IOException {

        BufferedImage image = validateAndReadImage(file);
        String format = normalizeFormat(targetFormat);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Thumbnails.of(image)
                    .scale(1.0)
                    .outputFormat(format)
                    .toOutputStream(baos);

            return baos.toByteArray();
        }
    }





}
