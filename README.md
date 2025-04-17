# CS6650 Final Project

## Group Members

- **Lan Wang** 
- **Jingwen Huang**
- **Yuxin Wang**

## Project Proposal: Scalable Image Processing System with Kubernetes

## 1. Problem Statement

Modern applications across industries (social media, e-commerce, content management systems) require processing thousands of images in real-time for various purposes such as resizing, format conversion, and applying visual effects. Traditional sequential processing approaches create significant bottlenecks under heavy load, leading to:

- Increased response times and degraded user experience
- Inefficient resource utilization
- Difficulty handling sudden traffic spikes
- Single points of failure

There is a critical need for a **high-throughput, fault-tolerant distributed system** that can efficiently process images at scale while adapting to variable workloads.

## 2. Proposed Solution

We propose building a **Kubernetes-Powered Image Processing Platform** that leverages containerization and orchestration to achieve elastic scalability.

### Architecture:

- **REST API** for image upload and processing requests
- **Java-based microservice** to handle image processing (simple resizing functionality)
- **Kubernetes (Minikube)**  to run multiple replicas (pods) of the service
- **Horizontal Pod Autoscaler (HPA)** to adjust pod count based on CPU usage
- **LocalStack S3** for storing processed images

### Workflow:

1. Client uploads images through REST API endpoints
2. Worker pods process images (resize to standard dimensions)
3. Processed images are stored in LocalStack S3 bucket
4. REST API returns response with processed image URLs or status

### Technology Stack:

- **Backend**: Java with Spring Boot (REST controllers)
- **Containerization**: Docker
- **Orchestration**: Kubernetes (Minikube)
- **Storage**: LocalStack S3
- **Testing**: JMeter for load testing

### Advanced Features (Priority-Based Implementation):

1. **Multiple Processing Operations**:
  - Image resizing with aspect ratio preservation
  - Format conversion (JPEG, PNG, WebP)
  - Basic filters and watermarking

2. **Observability and Monitoring**:
  - Prometheus and Grafana integration
  - Custom metrics for throughput and latency

3. **Extended Deployment Options** (time permitting):
  - Deployment to AWS EKS or GKE for cloud-scale testing
  - CI/CD pipeline for automated deployment

## 3. Evaluation Methodology

We will evaluate our system's performance through practical tests:

### Core Evaluation:

- **Baseline vs. Kubernetes Comparison:**  
  Compare performance of single-instance vs. Kubernetes-orchestrated deployment

- **Throughput Measurement:**  
  Count of images processed per minute under increasing load

- **Latency Measurement:**  
  Average time from submission to completion

- **Fault Tolerance:**  
  Test system recovery when worker pods are deliberately terminated

### Optional Evaluation:

- **Comparison of different Kubernetes configurations:**  
  Test performance with different settings:
  - Varying number of replicas
  - Different resource limits
  - With and without autoscaling

- **Scalability Test:**  
  Observe how system handles sudden spikes in incoming tasks

### Testing Approach:

1. Use JMeter to simulate varying client loads
2. Measure key metrics at different concurrency levels
3. Document findings in graphical format