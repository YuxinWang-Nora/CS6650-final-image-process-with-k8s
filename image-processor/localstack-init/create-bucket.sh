#!/bin/bash

echo "Creating S3 bucket for image storage..."
aws --endpoint-url=http://localhost:4566 s3 mb s3://images-bucket || echo "Bucket may already exist"

echo "Setting bucket policy to public-read for ease of testing..."
aws --endpoint-url=http://localhost:4566 s3api put-bucket-policy \
  --bucket images-bucket \
  --policy '{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "PublicReadGetObject",
        "Effect": "Allow",
        "Principal": "*",
        "Action": "s3:GetObject",
        "Resource": "arn:aws:s3:::images-bucket/*"
      }
    ]
  }' || echo "Setting policy may have failed - will continue anyway"

echo "S3 bucket setup complete!"