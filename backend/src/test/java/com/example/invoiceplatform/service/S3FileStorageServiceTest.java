package com.example.invoiceplatform.service;

import com.example.invoiceplatform.config.AwsS3Properties;
import com.example.invoiceplatform.exception.FileStorageException;
import com.example.invoiceplatform.exception.InvalidFileException;
import com.example.invoiceplatform.dto.UploadResultResponse;
import com.example.invoiceplatform.util.FileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    private S3FileStorageService service;

    @BeforeEach
    void setUp() {
        AwsS3Properties properties = new AwsS3Properties("test-bucket", "ap-southeast-1", "raw/invoices/");
        service = new S3FileStorageService(s3Client, properties, new FileValidator());
    }

    @Test
    void uploadsCsvAndReturnsResult_whenFileIsValid() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoices_ocr.csv", "text/csv", "File Name,Json Data\n".getBytes());

        UploadResultResponse result = service.store(file);

        assertThat(result.fileName()).isEqualTo("invoices_ocr.csv");
        assertThat(result.bucket()).isEqualTo("test-bucket");
        assertThat(result.s3Key()).startsWith("raw/invoices/").endsWith("invoices_ocr.csv");
        assertThat(result.sizeBytes()).isEqualTo(file.getSize());
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void rejectsEmptyFile_withoutCallingS3() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("empty");
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void rejectsNonCsvFile_withoutCallingS3() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoices.pdf", "application/pdf", "not-a-csv".getBytes());

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("CSV");
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void wrapsSdkFailure_asFileStorageException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoices.csv", "text/csv", "a,b\n".getBytes());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("connection refused"));

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("S3");
    }
}
