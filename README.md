# HireConnect

> A production-grade, microservices-based job portal connecting job seekers with recruiters — featuring real-time interview notifications, AI-powered job matching, subscription management, and a built-in digital wallet.

[![Live Frontend](https://img.shields.io/badge/Frontend-Live-brightgreen)](https://hire-connect-frontend-five.vercel.app)
[![API Gateway](https://img.shields.io/badge/API%20Gateway-Live-blue)](https://hireconnect-api-gateway-qtuk.onrender.com)
[![Swagger UI](https://img.shields.io/badge/Swagger-UI-orange)](https://hireconnect-api-gateway-qtuk.onrender.com/swagger-ui.html)

---

## Table of Contents

- [Live URLs](#live-urls)
- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Production Infrastructure](#production-infrastructure)
- [Getting Started (Local)](#getting-started-local)
- [API Documentation](#api-documentation)
- [Branch Structure](#branch-structure)
- [Default Local Config](#default-local-config)
- [License](#license)

---

## Live URLs

| Service | URL |
|---|---|
| Frontend | https://hire-connect-frontend-five.vercel.app |
| API Gateway | https://hireconnect-api-gateway-qtuk.onrender.com |
| Eureka Server | https://hireconnect-eureka-server.onrender.com |
| User Service | https://hireconnect-backend-4fcw.onrender.com |
| Swagger UI | https://hireconnect-api-gateway-qtuk.onrender.com/swagger-ui.html |

> ⚠️ **Cold Start Warning:** All backend services are hosted on Render's free tier. After a period of inactivity, expect **50+ seconds** on the first request.

---

## Architecture Overview

HireConnect uses a **microservices architecture** orchestrated via Spring Cloud, with a centralized API Gateway and service discovery through Netflix Eureka.

```
Client (Angular)
      │
      ▼
 API Gateway  ──► JWT Auth
      │
      ├──► User Service              (auth, OAuth2, admin)
      ├──► Job Service               (postings, search, applications)
      ├──► Interview & Notification  (scheduling, email, RabbitMQ)
      └──► Subscription & Analytics  (plans, payments, wallet)
                                        │
                                   Eureka Server (service registry)
```

### Services

| # | Service | Port | Responsibility |
|---|---|---|---|
| 1 | **Eureka Server** | 8761 | Service registry and discovery |
| 2 | **API Gateway** | 8080 | Single entry point; routing + JWT enforcement |
| 3 | **User Service** | 8081 | Accounts (Job Seekers, Recruiters, Admins), JWT auth, GitHub OAuth2 |
| 4 | **Job Service** | 8082 | Job postings, search, applications, recruiter team management |
| 5 | **Interview & Notification Service** | 8083 | Interview scheduling, email (SMTP), real-time events via RabbitMQ |
| 6 | **Subscription & Analytics Service** | 8084 | Subscriptions, Razorpay payments, digital wallet, platform analytics |
| 7 | **Frontend** | 4200 | Angular 17 SPA with light/dark theme and real-time dashboard |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Frontend** | Angular 17, TypeScript, HTML/CSS, Angular Material |
| **Backend** | Java 21, Spring Boot, Spring Cloud (Eureka, Gateway) |
| **Database** | MySQL 8.0 (Clever Cloud) |
| **Caching** | Redis (Upstash) |
| **Messaging** | RabbitMQ (CloudAMQP) |
| **Search** | Elasticsearch *(configured; disabled in production)* |
| **Security** | JWT, OAuth2 (GitHub) |
| **Payments** | Razorpay |
| **Containerization** | Docker |
| **Frontend Hosting** | Vercel |
| **Backend Hosting** | Render |

---

## Production Infrastructure

| Component | Provider | Host |
|---|---|---|
| MySQL | Clever Cloud | `bnvjay5ywgftaej1744f-mysql.services.clever-cloud.com` |
| Redis | Upstash | `rare-bird-75638.upstash.io` |
| RabbitMQ | CloudAMQP | `collie.lmq.cloudamqp.com` |
| Frontend | Vercel | `https://hire-connect-frontend-five.vercel.app` |
| Eureka Server | Render | `https://hireconnect-eureka-server.onrender.com` |
| User Service | Render | `https://hireconnect-backend-4fcw.onrender.com` |
| API Gateway | Render | `https://hireconnect-api-gateway-qtuk.onrender.com` |

---

## Getting Started (Local)

### Prerequisites

Ensure the following are installed before proceeding:

- **JDK 21+**
- **Node.js & npm**
- **Angular CLI** — `npm install -g @angular/cli`
- **Maven**
- **Docker Desktop**

---

### Step 1 — Configure Environment Variables

Each service reads configuration from environment variables. Create a `.env` file in each service directory (refer to `.env.example` if available) or export them in your shell session.

> 🔒 **Never commit `.env` files to version control.**

| Variable | Description |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | MySQL connection details |
| `DB_USER`, `DB_PASSWORD` | MySQL credentials |
| `RABBITMQ_HOST`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `RABBITMQ_VHOST` | RabbitMQ connection |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` | Redis connection |
| `EUREKA_URL` | Eureka server base URL |
| `JWT_SECRET` | Shared JWT signing secret *(min. 32 characters)* |
| `MAIL_USER`, `MAIL_PASS` | Gmail SMTP credentials *(interview-notification-service)* |
| `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET` | Razorpay credentials *(subscription-analytics-service)* |
| `FRONTEND_URL` | Frontend origin URL for CORS configuration |
| `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET` | GitHub OAuth App credentials *(user-service)* |

---

### Step 2 — Start Infrastructure Services

Use the provided `docker-compose.yml` to spin up local dependencies:

```bash
cd /path/to/HireConnect
docker-compose up -d
```

This starts the following services locally:

| Service | Port |
|---|---|
| MySQL | 3306 |
| RabbitMQ | 5672 (AMQP), 15672 (Management UI) |
| Redis | 6379 |
| Elasticsearch | 9200 |

---

### Step 3 — Run Backend Microservices

Start services in the following order. Eureka and the Gateway must be running before the other services register themselves.

```bash
# 1. Service Registry (must be first)
cd eureka-server && mvn spring-boot:run

# 2. API Gateway
cd api-gateway && mvn spring-boot:run

# 3. Core Services (order independent once Eureka & Gateway are up)
cd user-service                    && mvn spring-boot:run
cd job-service                     && mvn spring-boot:run
cd interview-notification-service  && mvn spring-boot:run
cd subscription-analytics-service  && mvn spring-boot:run
```

---

### Step 4 — Start the Frontend

```bash
cd hireconnect-web
npm install
ng serve
```

Open your browser at **http://localhost:4200**.

---

## API Documentation

Interactive Swagger UI is served via the API Gateway and aggregates documentation across all microservices.

| Environment | URL |
|---|---|
| Local | http://localhost:8080/swagger-ui.html |
| Production | https://hireconnect-api-gateway-qtuk.onrender.com/swagger-ui.html |

---

## Branch Structure

| Branch | Purpose |
|---|---|
| `main` | Stable, production-ready code |
| `dev` | Active integration branch |
| `feature/eureka-server` | Eureka Server service |
| `feature/api-gateway` | API Gateway service |
| `feature/user-service` | User Service |
| `feature/job-service` | Job Service |
| `feature/interview-notification-service` | Interview & Notification Service |
| `feature/subscription-analytics-service` | Subscription & Analytics Service |

---

## Default Local Config

| Setting | Value |
|---|---|
| MySQL databases | `hireconnect_users`, `hireconnect_jobs`, `hireconnect_interviews`, `hireconnect_subscriptions` |
| RabbitMQ credentials | `guest` / `guest` |
| Eureka Dashboard | http://localhost:8761 |

---

## License

*License details to be added.*
