package com.example.invoiceplatform.util;

import com.example.invoiceplatform.exception.InvalidFileException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
public class FileValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("csv");

    public void validateCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file must not be empty");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new InvalidFileException("Uploaded file must have a file name");
        }
        if (!ALLOWED_EXTENSIONS.contains(extensionOf(fileName))) {
            throw new InvalidFileException("Only CSV files are supported, got: " + fileName);
        }
    }

    private String extensionOf(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
