# Scalable Image Processing System with Kubernetes

This is a high-throughput, fault-tolerant distributed system for processing images at scale, built with Spring Boot and deployed on Kubernetes.

## Features

- **Multiple Image Processing Operations**:
   - Image resizing with aspect ratio preservation
   - Watermarking with customizable text and position
   - Filters (grayscale, sepia, blur, sharpen)
- **Scalable Architecture**:
   - REST API endpoints for different processing operations
   - Kubernetes orchestration with autoscaling
   - Containerized with Docker for portability
- **S3-Compatible Storage** (LocalStack for development)
- **Fault Tolerance and High Availability**

## Prerequisites

- Java 11+
- Docker & Docker Compose
- Kubernetes (Minikube for local development)
- JMeter (for load testing)

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

## Kubernetes Deployment

1. Build and tag the Docker image:
   ```bash
   docker build -t image-processor:latest .
   ```

2. Load the image into Minikube:
   ```bash
   minikube image load image-processor:latest
   ```

3. Deploy to Kubernetes:
   ```bash
   kubectl apply -f k8s/configmap.yaml
   kubectl apply -f k8s/localstack.yaml
   kubectl apply -f k8s/deployment.yaml
   kubectl apply -f k8s/service.yaml
   kubectl apply -f k8s/job.yaml
   kubectl apply -f k8s/hpa.yaml
   ```

4. Get the service URL:
   ```bash
   minikube service image-processor --url
   ```

## API Endpoints

### Health Check
```
GET /api/images/health
```

### Process Images

#### Resize Image
```
POST /api/images/upload/resize
```

Example:
```bash
curl -X POST -F "file=@/path/to/your/image.jpg" http://localhost:8080/api/images/upload/resize
```

#### Add Watermark
```
POST /api/images/upload/watermark
```

Parameters:
- `file`: Image file (required)
- `text`: Watermark text (optional, default: "© CS6650")
- `position`: Watermark position (optional, default: "bottom-right")
   - Options: "top-left", "top-right", "bottom-left", "bottom-right", "center"

Example:
```bash
curl -X POST -F "file=@/path/to/your/image.jpg" -F "text=Copyright 2025" -F "position=center" http://localhost:8080/api/images/upload/watermark
```

#### Apply Filter
```
POST /api/images/upload/filter
```

Parameters:
- `file`: Image file (required)
- `filter`: Type of filter (optional, default: "grayscale")
   - Options: "grayscale", "sepia", "blur", "sharpen"

Example:
```bash
curl -X POST -F "file=@/path/to/your/image.jpg" -F "filter=sepia" http://localhost:8080/api/images/upload/filter
```

### Retrieve Processed Image
```
GET /api/images/{imageKey}
```

Example:
```bash
curl -X GET http://localhost:8080/api/images/123e4567-e89b-12d3-a456-426614174000-image.jpg
```

## Response Format

```json
{
  "success": true,
  "message": "Image processed successfully",
  "imageUrl": "http://localhost:4566/images-bucket/123e4567-e89b-12d3-a456-426614174000-image.jpg",
  "imageKey": "123e4567-e89b-12d3-a456-426614174000-image.jpg",
  "originalName": "image.jpg",
  "timestamp": "2025-04-23T15:30:45.123"
}
```

## Performance Testing

The system can be load tested using JMeter to evaluate:

1. **Throughput**: Requests per second under varying loads
2. **Latency**: Response time distribution
3. **Scalability**: Performance with different numbers of replicas
4. **Fault Tolerance**: System behavior when pods are terminated

## Monitoring and Troubleshooting

Check pod status:
```bash
kubectl get pods
```

View logs:
```bash
kubectl logs deployment/image-processor
```

Check S3 storage:
```bash
kubectl exec -it $(kubectl get pod -l app=localstack -o name | cut -d/ -f2) -- \
  sh -c "AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test AWS_DEFAULT_REGION=us-east-1 \
  aws --endpoint-url=http://localhost:4566 s3 ls s3://images-bucket/"
```

Check Kubernetes dashboard:
```bash
minikube dashboard --url
```

## Project Structure

```
image-processor/
├── src/                            # Application source code
├── k8s/                            # Kubernetes configuration files
│   ├── configmap.yaml              # LocalStack init script
│   ├── deployment.yaml             # Main application deployment
│   ├── hpa.yaml                    # Horizontal Pod Autoscaler
│   ├── job.yaml                    # S3 bucket initialization job
│   ├── localstack.yaml             # LocalStack deployment & service
│   └── service.yaml                # Application service
├── localstack-init/                # LocalStack initialization scripts
├── Dockerfile                      # Application container definition
├── docker-compose.yml              # Local development setup
├── pom.xml                         # Maven dependencies
└── README.md                       # This file
```
