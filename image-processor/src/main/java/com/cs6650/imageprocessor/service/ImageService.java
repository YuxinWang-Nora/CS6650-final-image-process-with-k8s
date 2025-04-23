package com.cs6650.imageprocessor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

  private final S3Client s3Client;

  @Value("${aws.s3.bucket}")
  private String bucketName;

  @Value("${image.resize.width}")
  private int resizeWidth;

  @Value("${image.resize.height}")
  private int resizeHeight;

  /**
   * Process an image by resizing it and storing it in S3
   *
   * @param file The image file to process
   * @return URL of the processed image
   */
  public Map<String, String>  processImage(MultipartFile file) throws IOException {
    log.info("Processing image: {}", file.getOriginalFilename());

    // Read the image
    BufferedImage originalImage = ImageIO.read(file.getInputStream());

    // Resize the image
    BufferedImage resizedImage = resizeImage(originalImage);

    // Convert to byte array
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    String formatName = getFormatName(file.getOriginalFilename());
    ImageIO.write(resizedImage, formatName, os);
    byte[] resizedImageBytes = os.toByteArray();

    // Generate unique key for S3
    String key = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

    // Upload to S3
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(file.getContentType())
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(resizedImageBytes));

    log.info("Image processed and uploaded to S3: {}", key);

//    // Return the URL (would be different in production vs LocalStack)
//    return String.format("http://localhost:4566/%s/%s", bucketName, key);
    // Return both the URL and the key
    Map<String, String> result = new HashMap<>();
    result.put("url", String.format("http://localhost:4566/%s/%s", bucketName, key));
    result.put("key", key);

    return result;
  }

  /**
   * Retrieve an image from S3 by its key
   *
   * @param key The image key in S3
   * @return The image bytes
   */
  public byte[] getImage(String key) throws IOException {
    log.info("Retrieving image with key: {}", key);

    try {
      // Create a request to get the object
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      // Get the object from S3
      ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

      // Return the bytes
      return objectBytes.asByteArray();
    } catch (S3Exception e) {
      log.error("Error retrieving image from S3: {}", e.getMessage(), e);
      throw new IOException("Error retrieving image: " + e.getMessage(), e);
    }
  }

  /**
   * Resize an image while maintaining aspect ratio
   */
  private BufferedImage resizeImage(BufferedImage originalImage) {
    // Calculate resize dimensions while maintaining aspect ratio
    double originalAspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
    double targetAspectRatio = (double) resizeWidth / resizeHeight;

    int width, height;

    if (originalAspectRatio > targetAspectRatio) {
      // Width constrained
      width = resizeWidth;
      height = (int) (width / originalAspectRatio);
    } else {
      // Height constrained
      height = resizeHeight;
      width = (int) (height * originalAspectRatio);
    }

    return Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, width, height);
  }

  /**
   * Extract format name from filename for ImageIO
   */
  private String getFormatName(String filename) {
    String extension = filename.substring(filename.lastIndexOf('.') + 1);
    return extension.toLowerCase();
  }
}