# FUCinema API Services

This is the backend system for FUCinema, comprising 4 microservices built with Spring Boot, Spring Cloud Gateway, and various databases.

## Services Overview
1. **customer-service** (Port 8081): Manages customers and authentication (SQL Server).
2. **movie-service** (Port 8082): Manages genres, rooms, movies, and showtimes (MongoDB).
3. **booking-service** (Port 8083): Manages bookings and reports (MySQL).
4. **api-gateway** (Port 9000): Centralized routing and JWT-based role authorization.

## Prerequisites
- **Java 21**
- **Maven** (Apache Maven 3.9+)
- **Docker & Docker Compose**

## How to Run

1. **Start the databases**:
   Make sure Docker Desktop is running, then execute:
   ```bash
   docker compose up -d
   ```
   This will start SQL Server, MongoDB, and MySQL.

2. **Start the microservices**:
   Run the following commands in 4 separate terminal windows, or use your IDE's Services tool:
   ```bash
   cd customer-service && mvn spring-boot:run
   cd movie-service && mvn spring-boot:run
   cd booking-service && mvn spring-boot:run
   cd api-gateway && mvn spring-boot:run
   ```
   *(A `run-all.bat` script is also available for Windows users to launch all 4 services quickly).*

## Default Test Accounts
- **Admin**: `admin@fucinema.com` / `@@abc123@@`
- **Customer**: You can register a new customer via `POST /api/auth/register`, or use the Postman Collection which automatically creates test accounts.

## API Testing (Postman)
To test the entire flow (F1-F10):
1. Import `FUCinema_Local.postman_environment.json` and select it as the active environment.
2. Import `FUCinema.postman_collection.json`.
3. Open the Collection, select the **Run** tab, and click **Run FUCinema API Test**. All tests will execute sequentially and display passing green results.
