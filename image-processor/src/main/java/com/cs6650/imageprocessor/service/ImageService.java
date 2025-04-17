package com.cs6650.imageprocessor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
  public String processImage(MultipartFile file) throws IOException {
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

    // Return the URL (would be different in production vs LocalStack)
    return String.format("http://localhost:4566/%s/%s", bucketName, key);
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