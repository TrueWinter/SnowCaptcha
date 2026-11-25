package dev.truewinter.snowcaptcha.storage;

import dev.truewinter.snowcaptcha.Util;
import dev.truewinter.snowcaptcha.config.S3Config;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class S3Storage {
    private final S3Config config;
    private final S3Client client;
    private final S3Presigner presigner;
    private final AtomicBoolean available = new AtomicBoolean(false);

    public S3Storage(S3Config config) {
        this.config = config;

        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey()));
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(config.isPathStyleAccess())
                .build();

        S3ClientBuilder clientBuilder = S3Client.builder()
                .region(Region.of(config.getRegion()))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration);

        S3Configuration presignerConfiguration = serviceConfiguration.toBuilder()
                .checksumValidationEnabled(false)
                .build();

        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .region(Region.of(config.getRegion()))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(presignerConfiguration);

        if (!Util.isBlank(config.getEndpoint())) {
            URI endpoint = URI.create(config.getEndpoint());
            clientBuilder.endpointOverride(endpoint);
            presignerBuilder.endpointOverride(endpoint);
        }

        this.client = clientBuilder.build();
        this.presigner = presignerBuilder.build();
    }

    public void headObject(String key) {
        client.headObject(HeadObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .overrideConfiguration(o -> o
                        .apiCallTimeout(Duration.ofSeconds(2))
                        .apiCallAttemptTimeout(Duration.ofSeconds(2)))
                .build());
    }

    public boolean isAvailable() {
        return available.get();
    }

    public void setAvailable(boolean available) {
        this.available.set(available);
    }

    public void putObject(String key, byte[] data, String contentType) {
        client.putObject(PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .contentType(contentType)
                .build(), RequestBody.fromBytes(data));
    }

    public void deleteObject(String key) {
        client.deleteObject(DeleteObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .build());
    }

    public String presignedGetUrl(String key, String contentType, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .responseContentType(contentType)
                .build();

        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build()).url().toString();
    }

    public String getKeyPrefix() {
        return config.getKeyPrefix();
    }

    public void close() {
        presigner.close();
        client.close();
    }
}
