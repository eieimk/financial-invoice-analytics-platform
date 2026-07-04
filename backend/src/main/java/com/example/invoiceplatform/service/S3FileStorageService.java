package com.example.invoiceplatform.service;

import com.example.invoiceplatform.config.AwsS3Properties;
import com.example.invoiceplatform.dto.UploadResultResponse;
import com.example.invoiceplatform.exception.FileStorageException;
import com.example.invoiceplatform.util.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final AwsS3Properties properties;
    private final FileValidator fileValidator;

    @Override
    public UploadResultResponse store(MultipartFile file) {
        fileValidator.validateCsv(file);

        String key = buildObjectKey(file.getOriginalFilename());
        log.info("Uploading '{}' ({} bytes) to s3://{}/{}",
                file.getOriginalFilename(), file.getSize(), properties.bucket(), key);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .contentType("text/csv")
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | software.amazon.awssdk.core.exception.SdkException ex) {
            log.error("Upload to S3 failed for key {}", key, ex);
            throw new FileStorageException("Failed to upload file to S3", ex);
        }

        return UploadResultResponse.builder()
                .fileName(file.getOriginalFilename())
                .s3Key(key)
                .bucket(properties.bucket())
                .sizeBytes(file.getSize())
                .uploadedAt(Instant.now())
                .build();
    }

    /**
     * Keys are prefixed and salted with a UUID so re-uploads of the same file
     * never overwrite earlier batches (raw landing zone stays append-only).
     */
    private String buildObjectKey(String originalFilename) {
        String prefix = properties.uploadPrefix() == null ? "" : properties.uploadPrefix();
        return "%s%s-%s".formatted(prefix, UUID.randomUUID(), originalFilename);
    }
}
