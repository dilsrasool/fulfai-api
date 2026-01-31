package com.fulfai.sellingpartner.product;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ApplicationScoped
public class ProductImageUploadService {

    @ConfigProperty(name = "company.assets.bucket.name")
    String bucketName;

    @ConfigProperty(name = "app.cdn.base-url")
    String cdnBaseUrl;

    @ConfigProperty(name = "app.image.max-size", defaultValue = "5242880")
    long maxFileSize;

    @ConfigProperty(
            name = "app.image.allowed-types",
            defaultValue = "image/jpeg,image/png,image/webp"
    )
    String allowedTypes;

    @ConfigProperty(name = "app.upload.url-expiry-minutes", defaultValue = "5")
    int urlExpiryMinutes;

    @Inject
    S3Presigner s3Presigner;

    @Inject
    S3Client s3Client;

    @Inject
    ProductService productService;

    /* =========================
       DTOs
    ========================= */

    public static class UploadRequest {
        public String fileName;
        public String contentType;
        public long fileSize;
        public String productId;
    }

    public static class PresignedUploadResponse {
        public String uploadUrl;
        public String finalImageUrl;
        public String originalKey;
        public String thumbnailUrl;
        public String uploadId;
        public long expiresAt;

        public PresignedUploadResponse(
                String uploadUrl,
                String finalImageUrl,
                String originalKey,
                String thumbnailUrl,
                String uploadId,
                long expiresAt
        ) {
            this.uploadUrl = uploadUrl;
            this.finalImageUrl = finalImageUrl;
            this.originalKey = originalKey;
            this.thumbnailUrl = thumbnailUrl;
            this.uploadId = uploadId;
            this.expiresAt = expiresAt;
        }
    }

    /* =========================
       PRESIGNED UPLOAD
    ========================= */

    public PresignedUploadResponse generatePresignedUploadUrl(
            String companyId,
            String branchId,
            UploadRequest request
    ) {

        validateUploadRequest(request);

        String uploadId = UUID.randomUUID().toString();
        String sanitizedFileName = sanitizeFileName(request.fileName);

        String originalKey = String.format(
                "products/%s/%s/%s/uploads/%s/%s",
                companyId,
                branchId,
                request.productId,
                uploadId,
                sanitizedFileName
        );

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(originalKey)
                .contentType(request.contentType)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .metadata(Map.of(
                        "companyId", companyId,
                        "branchId", branchId,
                        "productId", request.productId,
                        "uploadId", uploadId
                ))
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(urlExpiryMinutes))
                        .putObjectRequest(putRequest)
                        .build();

        PresignedPutObjectRequest presigned =
                s3Presigner.presignPutObject(presignRequest);

        // mark uploading
        productService.markImageUploadStarted(
                companyId,
                branchId,
                request.productId,
                uploadId
        );

        String finalImageUrl = generateFinalUrl(originalKey);
        String thumbnailUrl = generateThumbnailUrl(originalKey);

        Log.infof(
                "Presigned upload created company=%s branch=%s product=%s uploadId=%s",
                companyId,
                branchId,
                request.productId,
                uploadId
        );

        return new PresignedUploadResponse(
                presigned.url().toString(),
                finalImageUrl,
                originalKey,
                thumbnailUrl,
                uploadId,
                presigned.expiration().toEpochMilli()
        );
    }

    /* =========================
       FINALIZE
    ========================= */

    public String confirmUpload(
        String companyId,
        String branchId,
        String productId,
        String uploadId,
        String originalKey
) {
    s3Client.headObject(b -> b.bucket(bucketName).key(originalKey));

    String finalImageUrl = generateFinalUrl(originalKey);
    String thumbnailUrl = generateThumbnailUrl(originalKey);

    productService.markImageUploadCompleted(
            companyId,
            branchId,
            productId,
            finalImageUrl,
            thumbnailUrl
    );

    return finalImageUrl; // ✅ CRITICAL
}


    /* =========================
       VALIDATION
    ========================= */

    private void validateUploadRequest(UploadRequest request) {

        if (request.fileSize <= 0 || request.fileSize > maxFileSize) {
            throw new IllegalArgumentException("Invalid file size");
        }

        boolean allowed = false;
        for (String type : allowedTypes.split(",")) {
            if (type.trim().equals(request.contentType)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new IllegalArgumentException("Content type not allowed");
        }

        String extension = getFileExtension(request.fileName).toLowerCase();
        if (!isValidExtensionForContentType(extension, request.contentType)) {
            throw new IllegalArgumentException("Extension mismatch");
        }
    }

    private boolean isValidExtensionForContentType(String extension, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg".equals(extension) || "jpeg".equals(extension);
            case "image/png" -> "png".equals(extension);
            case "image/webp" -> "webp".equals(extension);
            default -> false;
        };
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(idx + 1) : "";
    }

    private String generateFinalUrl(String key) {
        return cdnBaseUrl + "/" + key;
    }

    private String generateThumbnailUrl(String originalKey) {
        String thumbKey = originalKey.replace("/uploads/", "/thumbnails/");
        thumbKey = thumbKey.substring(0, thumbKey.lastIndexOf('.')) + "_thumb.jpg";
        return cdnBaseUrl + "/" + thumbKey;
    }
}
