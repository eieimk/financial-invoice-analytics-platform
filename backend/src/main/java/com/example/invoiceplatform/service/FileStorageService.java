package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.UploadResultResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage abstraction: controllers depend on this interface, not on S3 directly,
 * so the storage backend can change (e.g. local disk in tests) without API impact.
 */
public interface FileStorageService {

    UploadResultResponse store(MultipartFile file);
}
