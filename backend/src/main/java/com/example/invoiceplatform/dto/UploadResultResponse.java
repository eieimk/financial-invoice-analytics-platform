package com.example.invoiceplatform.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UploadResultResponse(
        String fileName,
        String s3Key,
        String bucket,
        long sizeBytes,
        Instant uploadedAt
) {
}
