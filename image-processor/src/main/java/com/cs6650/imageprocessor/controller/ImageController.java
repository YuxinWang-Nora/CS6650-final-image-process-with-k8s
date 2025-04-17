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

      // Process the image
      String imageUrl = imageService.processImage(file);

      return ResponseEntity.ok(
          ImageResponse.builder()
              .success(true)
              .message("Image processed successfully")
              .imageUrl(imageUrl)
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
}