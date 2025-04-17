package com.cs6650.imageprocessor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

  @Value("${aws.s3.endpoint}")
  private String endpoint;

  @Value("${aws.region}")
  private String region;

  @Value("${aws.accessKeyId}")
  private String accessKeyId;

  @Value("${aws.secretKey}")
  private String secretKey;

  @Bean
  public S3Client s3Client() {
    // For LocalStack, we use dummy credentials
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretKey);

    S3Configuration s3Configuration = S3Configuration.builder()
        .pathStyleAccessEnabled(true) // Required for LocalStack
        .build();

    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .serviceConfiguration(s3Configuration)
        .build();
  }
}