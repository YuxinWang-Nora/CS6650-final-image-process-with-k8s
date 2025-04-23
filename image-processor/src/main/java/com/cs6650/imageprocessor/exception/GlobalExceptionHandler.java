package com.cs6650.imageprocessor.exception;

import com.cs6650.imageprocessor.model.ImageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ImageResponse> handleMaxSizeException(MaxUploadSizeExceededException exc) {
    log.error("File size exceeds limit", exc);
    return ResponseEntity
        .status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(ImageResponse.builder()
            .success(false)
            .message("File size exceeds the maximum limit (10MB)")
            .timestamp(LocalDateTime.now())
            .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ImageResponse> handleGenericException(Exception exc) {
    log.error("Unexpected error", exc);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ImageResponse.builder()
            .success(false)
            .message("An unexpected error occurred: " + exc.getMessage())
            .timestamp(LocalDateTime.now())
            .build());
  }
}