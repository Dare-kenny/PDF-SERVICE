package com.example.PDF_MicroService.util;

import org.springframework.web.multipart.MultipartFile;

public enum DocumentType {

    PDF("application/pdf", ".pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");

    private final String mimeType;
    private final String extension;

    DocumentType(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * Detects the DocumentType from a MultipartFile by inspecting
     * its content type and original filename extension.
     *
     * @param file the uploaded file
     * @return the matching DocumentType
     * @throws IllegalArgumentException if the file type is not supported
     */
    public static DocumentType detect(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase()
                : "";

        if (PDF.mimeType.equalsIgnoreCase(contentType) || filename.endsWith(".pdf")) {
            return PDF;
        }
        if (DOCX.mimeType.equalsIgnoreCase(contentType) || filename.endsWith(".docx")) {
            return DOCX;
        }

        throw new IllegalArgumentException(
                "Unsupported file type: '" + contentType + "'. Only PDF and DOCX files are accepted."
        );
    }
}
