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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
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
   * Legacy method for backwards compatibility
   */
  public Map<String, String> processImage(MultipartFile file) throws IOException {
    return resizeImage(file);
  }

  /**
   * Resize an image and store it in S3
   *
   * @param file The image file to process
   * @return Map containing URL and key of the processed image
   */
  public Map<String, String> resizeImage(MultipartFile file) throws IOException {
    log.info("Resizing image: {}", file.getOriginalFilename());

    // Read the image
    BufferedImage originalImage = ImageIO.read(file.getInputStream());

    // Resize the image
    BufferedImage resizedImage = resizeImageInternal(originalImage);

    // Upload to S3 and return result
    return uploadImageToS3(resizedImage, file.getContentType(), file.getOriginalFilename());
  }

  /**
   * Add watermark to an image and store it in S3
   *
   * @param file The image file to process
   * @param watermarkText The text to use as watermark
   * @param position The position of the watermark (top-left, top-right, bottom-left, bottom-right, center)
   * @return Map containing URL and key of the processed image
   */
  public Map<String, String> watermarkImage(MultipartFile file, String watermarkText, String position) throws IOException {
    log.info("Adding watermark to image: {}", file.getOriginalFilename());

    // Read the image
    BufferedImage originalImage = ImageIO.read(file.getInputStream());

    // Add watermark
    BufferedImage watermarkedImage = addWatermark(originalImage, watermarkText, position);

    // Upload to S3 and return result
    return uploadImageToS3(watermarkedImage, file.getContentType(), file.getOriginalFilename());
  }

  /**
   * Apply filter to an image and store it in S3
   *
   * @param file The image file to process
   * @param filterType The type of filter to apply (grayscale, sepia, blur, etc.)
   * @return Map containing URL and key of the processed image
   */
  public Map<String, String> filterImage(MultipartFile file, String filterType) throws IOException {
    log.info("Applying {} filter to image: {}", filterType, file.getOriginalFilename());

    // Read the image
    BufferedImage originalImage = ImageIO.read(file.getInputStream());

    // Apply filter
    BufferedImage filteredImage = applyFilter(originalImage, filterType);

    // Upload to S3 and return result
    return uploadImageToS3(filteredImage, file.getContentType(), file.getOriginalFilename());
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
  private BufferedImage resizeImageInternal(BufferedImage originalImage) {
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
   * Add watermark to an image
   */
  private BufferedImage addWatermark(BufferedImage originalImage, String watermarkText, String position) {
    int width = originalImage.getWidth();
    int height = originalImage.getHeight();

    // Create a copy of the original image
    BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = watermarkedImage.createGraphics();

    // Draw the original image
    g2d.drawImage(originalImage, 0, 0, null);

    // Set up the watermark text properties
    g2d.setColor(new Color(255, 255, 255, 128)); // Semi-transparent white
    Font font = new Font("Arial", Font.BOLD, Math.max(width, height) / 20); // Scale font size with image
    g2d.setFont(font);
    FontMetrics fontMetrics = g2d.getFontMetrics();
    int textWidth = fontMetrics.stringWidth(watermarkText);
    int textHeight = fontMetrics.getHeight();

    // Determine watermark position
    int x, y;
    switch (position.toLowerCase()) {
      case "top-left":
        x = 10;
        y = textHeight + 10;
        break;
      case "top-right":
        x = width - textWidth - 10;
        y = textHeight + 10;
        break;
      case "bottom-left":
        x = 10;
        y = height - 10;
        break;
      case "center":
        x = (width - textWidth) / 2;
        y = (height + textHeight) / 2;
        break;
      case "bottom-right":
      default:
        x = width - textWidth - 10;
        y = height - 10;
        break;
    }

    // Add a subtle shadow for better visibility
    g2d.setColor(new Color(0, 0, 0, 128));
    g2d.drawString(watermarkText, x + 2, y + 2);

    // Draw the watermark text
    g2d.setColor(new Color(255, 255, 255, 180));
    g2d.drawString(watermarkText, x, y);

    g2d.dispose();

    return watermarkedImage;
  }

  /**
   * Apply filter to an image
   */
  private BufferedImage applyFilter(BufferedImage originalImage, String filterType) {
    switch (filterType.toLowerCase()) {
      case "grayscale":
        return applyGrayscaleFilter(originalImage);
      case "sepia":
        return applySepiaFilter(originalImage);
      case "blur":
        return applyBlurFilter(originalImage);
      case "sharpen":
        return applySharpenFilter(originalImage);
      default:
        log.warn("Unknown filter type: {}, applying grayscale as default", filterType);
        return applyGrayscaleFilter(originalImage);
    }
  }

  /**
   * Apply grayscale filter
   */
  private BufferedImage applyGrayscaleFilter(BufferedImage originalImage) {
    BufferedImage result = new BufferedImage(
        originalImage.getWidth(),
        originalImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    Graphics2D g2d = result.createGraphics();
    g2d.drawImage(originalImage, 0, 0, null);
    g2d.dispose();

    for (int y = 0; y < result.getHeight(); y++) {
      for (int x = 0; x < result.getWidth(); x++) {
        int rgb = result.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb & 0xFF);

        // Calculate the grayscale value using the luminance method
        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

        // Set the new RGB value
        int newRGB = (gray << 16) | (gray << 8) | gray;
        result.setRGB(x, y, newRGB);
      }
    }

    return result;
  }

  /**
   * Apply sepia filter
   */
  private BufferedImage applySepiaFilter(BufferedImage originalImage) {
    BufferedImage result = new BufferedImage(
        originalImage.getWidth(),
        originalImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    Graphics2D g2d = result.createGraphics();
    g2d.drawImage(originalImage, 0, 0, null);
    g2d.dispose();

    for (int y = 0; y < result.getHeight(); y++) {
      for (int x = 0; x < result.getWidth(); x++) {
        int rgb = result.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb & 0xFF);

        // Calculate the sepia tone values
        int tr = (int) (0.393 * r + 0.769 * g + 0.189 * b);
        int tg = (int) (0.349 * r + 0.686 * g + 0.168 * b);
        int tb = (int) (0.272 * r + 0.534 * g + 0.131 * b);

        // Ensure the values are within valid range
        r = Math.min(255, tr);
        g = Math.min(255, tg);
        b = Math.min(255, tb);

        // Set the new RGB value
        int newRGB = (r << 16) | (g << 8) | b;
        result.setRGB(x, y, newRGB);
      }
    }

    return result;
  }

  /**
   * Apply blur filter
   */
  private BufferedImage applyBlurFilter(BufferedImage originalImage) {
    // Using a 3x3 box blur kernel
    float[] blurKernel = {
        1/9f, 1/9f, 1/9f,
        1/9f, 1/9f, 1/9f,
        1/9f, 1/9f, 1/9f
    };

    Kernel kernel = new Kernel(3, 3, blurKernel);
    ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    return op.filter(originalImage, null);
  }

  /**
   * Apply sharpen filter
   */
  private BufferedImage applySharpenFilter(BufferedImage originalImage) {
    // Using a 3x3 sharpen kernel
    float[] sharpenKernel = {
        0, -1, 0,
        -1, 5, -1,
        0, -1, 0
    };

    Kernel kernel = new Kernel(3, 3, sharpenKernel);
    ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    return op.filter(originalImage, null);
  }

  /**
   * Upload image to S3 and return URL and key
   */
  private Map<String, String> uploadImageToS3(BufferedImage image, String contentType, String originalFilename) throws IOException {
    // Convert to byte array
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    String formatName = getFormatName(originalFilename);
    ImageIO.write(image, formatName, os);
    byte[] imageBytes = os.toByteArray();

    // Generate unique key for S3
    String key = UUID.randomUUID().toString() + "-" + originalFilename;

    // Upload to S3
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(contentType)
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

    log.info("Image processed and uploaded to S3: {}", key);

    // Return both the URL and the key
    Map<String, String> result = new HashMap<>();
    result.put("url", String.format("http://localhost:4566/%s/%s", bucketName, key));
    result.put("key", key);

    return result;
  }

  /**
   * Extract format name from filename for ImageIO
   */
  private String getFormatName(String filename) {
    String extension = filename.substring(filename.lastIndexOf('.') + 1);
    return extension.toLowerCase();
  }
}