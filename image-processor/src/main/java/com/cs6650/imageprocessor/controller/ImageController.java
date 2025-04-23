package com.cs6650.imageprocessor.controller;

import com.cs6650.imageprocessor.model.ImageResponse;
import com.cs6650.imageprocessor.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

  private final ImageService imageService;

  /**
   * Health check endpoint
   */
  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Image processor service is up and running!");
  }

  /**
   * Upload and process an image
   */
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageResponse> uploadImage(@RequestParam("file") MultipartFile file) {
    try {
      if (file.isEmpty()) {
        return ResponseEntity.badRequest().body(
            ImageResponse.builder()
                .success(false)
                .message("Please select a file to upload")
                .timestamp(LocalDateTime.now())
                .build()
        );
      }

      // Check if file is an image
      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        return ResponseEntity.badRequest().body(
            ImageResponse.builder()
                .success(false)
                .message("File must be an image")
                .timestamp(LocalDateTime.now())
                .build()
        );
      }

//      // Process the image
//      String imageUrl = imageService.processImage(file);
//
//      return ResponseEntity.ok(
//          ImageResponse.builder()
//              .success(true)
//              .message("Image processed successfully")
//              .imageUrl(imageUrl)
//              .originalName(file.getOriginalFilename())
//              .timestamp(LocalDateTime.now())
//              .build()
//      );
      // Process the image
      Map<String, String> processResult = imageService.processImage(file);
      String imageUrl = processResult.get("url");
      String imageKey = processResult.get("key");

      return ResponseEntity.ok(
          ImageResponse.builder()
              .success(true)
              .message("Image processed successfully (newly updated)")
              .imageUrl(imageUrl)
              .imageKey(imageKey)  // Add this line
              .originalName(file.getOriginalFilename())
              .timestamp(LocalDateTime.now())
              .build()
      );

    } catch (IOException e) {
      log.error("Error processing image", e);
      return ResponseEntity.internalServerError().body(
          ImageResponse.builder()
              .success(false)
              .message("Error processing image: " + e.getMessage())
              .timestamp(LocalDateTime.now())
              .build()
      );
    }
  }

  /**
   * Get a processed image by its key
   */
  @GetMapping(value = "/{imageKey}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
  public ResponseEntity<byte[]> getImage(@PathVariable String imageKey) {
    try {
      byte[] imageBytes = imageService.getImage(imageKey);

      // Determine the content type based on the file extension
      String contentType = determineContentType(imageKey);

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .body(imageBytes);
    } catch (IOException e) {
      log.error("Error retrieving image", e);
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Determine the content type based on the file extension
   */
  private String determineContentType(String filename) {
    if (filename.toLowerCase().endsWith(".png")) {
      return MediaType.IMAGE_PNG_VALUE;
    } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
      return MediaType.IMAGE_JPEG_VALUE;
    } else {
      // Default to JPEG
      return MediaType.IMAGE_JPEG_VALUE;
    }
  }
}