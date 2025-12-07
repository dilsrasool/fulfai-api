package com.fulfai.common.s3;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.quarkus.logging.Log;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class S3Utils {

    private static final Duration DEFAULT_GET_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration DEFAULT_PUT_EXPIRATION = Duration.ofMinutes(30);

    /**
     * Generate a presigned GET URL for downloading an object
     */
    public static String createPresignedGetUrl(S3Presigner presigner, String bucketName, String keyName) {
        return createPresignedGetUrl(presigner, bucketName, keyName, DEFAULT_GET_EXPIRATION);
    }

    /**
     * Generate a presigned GET URL with custom expiration
     */
    public static String createPresignedGetUrl(S3Presigner presigner, String bucketName, String keyName, Duration expiration) {
        Log.debugf("S3_PRESIGN_GET: bucket=%s, key=%s, expiration=%s", bucketName, keyName, expiration);

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        Log.debugf("S3_PRESIGN_GET_URL: %s", presignedRequest.url().toString());

        return presignedRequest.url().toExternalForm();
    }

    /**
     * Generate a presigned PUT URL for uploading an object
     */
    public static String createPresignedPutUrl(S3Presigner presigner, String bucketName, String keyName) {
        return createPresignedPutUrl(presigner, bucketName, keyName, DEFAULT_PUT_EXPIRATION);
    }

    /**
     * Generate a presigned PUT URL with custom expiration
     */
    public static String createPresignedPutUrl(S3Presigner presigner, String bucketName, String keyName, Duration expiration) {
        Log.debugf("S3_PRESIGN_PUT: bucket=%s, key=%s, expiration=%s", bucketName, keyName, expiration);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        Log.debugf("S3_PRESIGN_PUT_URL: %s", presignedRequest.url().toString());

        return presignedRequest.url().toExternalForm();
    }

    /**
     * Check if an S3 object exists
     *
     * @throws NoSuchKeyException if the file does not exist
     */
    public static void checkFileExists(S3Client s3Client, String bucketName, String keyName) throws NoSuchKeyException {
        Log.debugf("S3_CHECK_EXISTS: bucket=%s, key=%s", bucketName, keyName);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.headObject(headObjectRequest);
            Log.debugf("S3_CHECK_EXISTS: File exists - %s", keyName);

        } catch (NoSuchKeyException e) {
            Log.debugf("S3_CHECK_EXISTS: File not found - bucket=%s, key=%s", bucketName, keyName);
            throw e;
        }
    }

    /**
     * Check if an S3 object exists (returns boolean instead of throwing)
     */
    public static boolean fileExists(S3Client s3Client, String bucketName, String keyName) {
        try {
            checkFileExists(s3Client, bucketName, keyName);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * List objects in a bucket with given prefix
     */
    public static List<S3ObjectInfo> listObjects(S3Client s3Client, String bucketName, String prefix) {
        return listObjects(s3Client, bucketName, prefix, 100);
    }

    /**
     * List objects in a bucket with given prefix and max keys
     */
    public static List<S3ObjectInfo> listObjects(S3Client s3Client, String bucketName, String prefix, int maxKeys) {
        Log.debugf("S3_LIST: bucket=%s, prefix=%s, maxKeys=%d", bucketName, prefix, maxKeys);

        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .maxKeys(maxKeys)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            List<S3Object> objects = listResponse.contents();

            List<S3ObjectInfo> results = objects.stream()
                    .map(obj -> new S3ObjectInfo(obj.key(), obj.lastModified(), obj.size()))
                    .toList();

            Log.debugf("S3_LIST_RESULT: Found %d objects in prefix=%s", results.size(), prefix);
            return results;

        } catch (Exception e) {
            Log.errorf(e, "S3_LIST_ERROR: bucket=%s, prefix=%s", bucketName, prefix);
            return List.of();
        }
    }

    /**
     * Find the most recent file with given extension in prefix
     */
    public static Optional<S3ObjectInfo> findMostRecentFile(S3Client s3Client, String bucketName, String prefix, String extension) {
        List<S3ObjectInfo> files = listObjects(s3Client, bucketName, prefix).stream()
                .filter(obj -> obj.key().endsWith(extension))
                .toList();

        if (files.isEmpty()) {
            Log.debugf("S3_FIND_RECENT: No %s files found in prefix=%s", extension, prefix);
            return Optional.empty();
        }

        Optional<S3ObjectInfo> mostRecent = files.stream()
                .max((f1, f2) -> f1.lastModified().compareTo(f2.lastModified()));

        mostRecent.ifPresent(file ->
                Log.debugf("S3_FIND_RECENT: Most recent %s file: %s, lastModified=%s", extension, file.key(), file.lastModified()));

        return mostRecent;
    }

    /**
     * S3 Object information wrapper
     */
    public static class S3ObjectInfo {
        private final String key;
        private final Instant lastModified;
        private final long size;

        public S3ObjectInfo(String key, Instant lastModified, long size) {
            this.key = key;
            this.lastModified = lastModified;
            this.size = size;
        }

        public String key() {
            return key;
        }

        public Instant lastModified() {
            return lastModified;
        }

        public long size() {
            return size;
        }
    }
}
