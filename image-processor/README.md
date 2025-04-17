# Image Processing Service

This is a scalable image processing service built with Spring Boot and designed to run on Kubernetes.

## Features

- Image upload via REST API
- Image resizing with aspect ratio preservation
- Storage in S3 (LocalStack for development)
- Containerized with Docker
- Ready for Kubernetes deployment

## Prerequisites

- Java 11+
- Docker & Docker Compose
- Maven

## Local Development

### Running with Docker Compose

1. Make the initialization script executable:
   ```bash
   chmod +x localstack-init/create-bucket.sh
   ```

2. Build and start the services:
   ```bash
   docker-compose up --build
   ```

3. The service will be available at `http://localhost:8080`

### Running the Spring Boot App Directly

1. Start LocalStack in a Docker container:
   ```bash
   docker run -d -p 4566:4566 -e "SERVICES=s3" -e "DEFAULT_REGION=us-east-1" localstack/localstack
   ```

2. Create the S3 bucket:
   ```bash
   aws --endpoint-url=http://localhost:4566 s3 mb s3://images-bucket
   ```

3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Usage

### Upload and Process an Image

```
POST /api/images/upload
```

Example using curl:
```bash
curl -X POST -F "file=@/path/to/your/image.jpg" http://localhost:8080/api/images/upload
```

Response:
```json
{
  "success": true,
  "message": "Image processed successfully",
  "imageUrl": "http://localhost:4566/images-bucket/123e4567-e89b-12d3-a456-426614174000-image.jpg",
  "originalName": "image.jpg",
  "timestamp": "2023-07-22T15:30:45.123"
}
```

### Health Check

```
GET /api/images/health
```

## Docker Build

To build the Docker image separately:

```bash
docker build -t image-processor:latest .
```

## Preparing for Kubernetes

The application is designed to be deployed to Kubernetes. See the Kubernetes deployment files in the `k8s/` directory (to be created in the next phase).

## Next Steps

- Kubernetes deployment configurations
- Horizontal Pod Autoscaler setup
- Prometheus metrics integration
- Additional image processing operations