# CS6650 Final Project

## Group Members

- **Member 1:** [Name] - [Email]
- **Member 2:** [Name] - [Email]
- **Member 3:** [Name] - [Email]

## Project Proposal: Scalable Image Processing Service using Kubernetes
## 1. Problem Statement
In real-world applications like social media platforms, e-commerce websites, or digital media companies, massive numbers of images must be processed (e.g., resized, watermarked, or converted) before storage or display. Processing images sequentially on a single server can quickly become a performance bottleneck, especially under heavy load.
There is a need for a **scalable and fault-tolerant distributed system** that can process images efficiently under varying workloads.

## 2. Proposed Solution
We propose to build a **Scalable Image Processing Service** deployed on **Kubernetes (Minikube)**.

### Architecture:
- A **Java-based microservice** will accept image processing tasks (for example, resize images).
- Kubernetes (Minikube) will run **multiple replicas** (pods) of the image processing service.
- Kubernetes **Horizontal Pod Autoscaler** will automatically adjust the number of pods based on CPU usage.
- A S3 bucket in local stack will be used to store the processed images.
- Client will upload images to do the load testing.

### Workflow:
1. client uploads images.
2. Worker pods (Java microservices) consume tasks and process images.
3. Processed images are stored in local stack S3 bucket.

**Technology Stack:**
- **Backend:** Java (Spring Boot or simple Java HTTP server)
- **Containerization:** Docker
- **Orchestration:** Kubernetes (Minikube)
- **Autoscaling:** Kubernetes Horizontal Pod Autoscaler

**Bonus Enhancements (optional depending on time):**
- Implement different processing modes (resize, rotate, watermark, filter).
- Provide a simple API endpoint for uploading tasks instead of CLI client.
- Python or Node.js client for uploading images。
- Deploy to AWS Kubernetes Service (EKS) or Google Kubernetes Engine (GKE) for production. (if time permits)

## 3. Proposed Evaluation
We will evaluate the system’s performance and scalability under different workloads by:
- **Comparison of performance with and without kubernetes:**  
  Measure the performance of the image processing service with and without Kubernetes to demonstrate the benefits of using Kubernetes for scaling.
- **Comparison of performance with different kubernetes configurations:**  
  Measure the performance of the image processing service with different Kubernetes configurations (different number of replicas, different resource limits, autoscaling) to demonstrate the impact of these configurations on performance.
- The measurement will be done using the following metrics:
  - **Throughput Measurement:**  
    Measure the number of images processed per minute as load increases.
  - **Latency Measurement:**  
    Measure the average time taken from task submission to image processing completion.
  - **Fault Tolerance Check:**  
    Kill random worker pods during processing and observe that Kubernetes reschedules and continues processing without system failure.
  - **Scalability Test(optional depending on time):**  
    Use load testing tools (e.g., a custom script or `hey` load generator) to simulate a sudden spike in incoming tasks and observe how the system automatically scales up pods.