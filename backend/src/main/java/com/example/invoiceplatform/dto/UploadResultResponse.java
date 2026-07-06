package com.example.invoiceplatform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UploadResultResponse(
        String fileName,
        String s3Key,
        String bucket,
        long sizeBytes,
        Instant uploadedAt,
        // Warehouse sync (null when produced by the raw S3 store alone)
        Integer rowsLoadedToWarehouse,
        Boolean warehouseRefreshTriggered
) {
}
