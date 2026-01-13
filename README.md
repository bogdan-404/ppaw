# PPAW Monetization App - Text Summarizer & Rewriter

Complete monetization web application with Spring Boot backend, React frontend, and PostgreSQL database.

## Tech Stack

- **Backend**: Spring Boot 3.2.0, Java 21, Maven
- **Frontend**: React 18, Bootstrap 5
- **Database**: PostgreSQL 16
- **Migrations**: Flyway
- **Containerization**: Docker & Docker Compose

## Architecture

- **Presentation Layer**: 
  - REST controllers for API (backend-api service on port 8080)
  - MVC controllers with Thymeleaf for admin (backend-admin service on port 8081)
- **Service Layer**: Business logic with caching and logging (shared by both services)
- **Data Access Layer**: Spring Data JPA repositories and entities (shared by both services)

### Docker Services
- **postgres**: PostgreSQL database
- **backend-api**: Spring Boot service serving REST API only (port 8080)
- **backend-admin**: Spring Boot service serving MVC admin panel only (port 8081)
- **frontend**: React user application (port 3000)

## Quick Start

### Prerequisites
- Docker and Docker Compose installed

### Run the Application

1. **Start all services**:
   ```bash
   docker compose up --build
   ```

2. **Access the application**:
   - **User Web App**: http://localhost:3000
   - **Backend API**: http://localhost:8080
   - **Admin Panel**: http://localhost:8081/admin/login

## Access Links

### Frontend (React web app)
- **URL**: http://localhost:3000
- User-facing app with Plans, Main (Summarize/Rewrite), and History pages

### Backend REST API
- **Base URL**: http://localhost:8080/api
- **Endpoints**:
  - `GET /api/plans` — Get all active plans
  - `POST /api/auth/register` — Register new user
  - `POST /api/auth/login` — Login
  - `GET /api/subscription` — Get current subscription (requires X-USER-ID header)
  - `POST /api/subscription/pay` — Subscribe to plan (requires X-USER-ID header)
  - `POST /api/subscription/cancel` — Cancel subscription (requires X-USER-ID header)
  - `POST /api/text/summarize` — Summarize text (requires X-USER-ID header)
  - `POST /api/text/rewrite` — Rewrite text (requires X-USER-ID header)
  - `GET /api/history` — Get user history (requires X-USER-ID header)
  - `DELETE /api/history/{id}` — Delete saved work (requires X-USER-ID header)

### Admin MVC panel
- **URL**: http://localhost:8081/admin/login
- **Login credentials**:
  - Email: `admin@local`
  - Password: `admin`
- **Features**:
  - Dashboard: http://localhost:8081/admin/dashboard
  - Plans Management: http://localhost:8081/admin/plans
  - Plan Limits: http://localhost:8081/admin/plans/{planId}/limits
  - Users Management: http://localhost:8081/admin/users

**Note**: The admin panel runs on port 8081 as a separate service from the REST API (port 8080). Both services share the same database and service layer code, but run in separate Docker containers.

## Features

### User Features
- Register/Login
- View subscription plans (Free/Usual/Premium)
- Subscribe to plans via "Achita" button
- Cancel subscription
- Summarize and rewrite text with different styles
- View history of saved works
- Delete saved works (logical delete)

### Admin Features
- Manage Plans (CRUD)
- Manage Plan Limits (CRUD)
- Manage Users (view/edit role, email, password)

### Plan Limits
- **Free**: 3 requests/day, max 2000 chars, style: simple
- **Usual**: 200 requests/month, max 20000 chars, 5 styles
- **Premium**: Unlimited requests, batch enabled, export enabled, tone rules enabled

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Plans & Subscription
- `GET /api/plans` - Get all active plans
- `GET /api/subscription` - Get current subscription (requires X-USER-ID header)
- `POST /api/subscription/pay` - Subscribe to plan (requires X-USER-ID header)
- `POST /api/subscription/cancel` - Cancel subscription (requires X-USER-ID header)

### Text Processing
- `POST /api/text/summarize` - Summarize text (requires X-USER-ID header)
- `POST /api/text/rewrite` - Rewrite text (requires X-USER-ID header)

### History
- `GET /api/history` - Get saved works (requires X-USER-ID header)
- `DELETE /api/history/{id}` - Delete saved work (requires X-USER-ID header)


## Configuration

- Caching enabled for plans (evicted on admin updates)
- Logging configured for services
- CORS enabled for frontend origin
- Logical delete for saved_works
- JPA open-in-view disabled