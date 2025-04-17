package com.cs6650.imageprocessor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
  private boolean success;
  private String message;
  private String imageUrl;
  private String originalName;
  private LocalDateTime timestamp;
}