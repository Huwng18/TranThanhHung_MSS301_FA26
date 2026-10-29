# MSS301 - Microservices Architecture - Practical Exam (Summer 2026)

**Student Name:** Trần Thanh Hùng  
**Student ID:** DE190481  
**Folder Codebase:** `Slot16/Code_PE`

---

## 1. System Overview

The system consists of 3 microservices following Spring Boot 4.x / Spring Cloud 2025.x architecture:

| Microservice | Port | Framework / Tech Stack | Main Purpose |
|---|---|---|---|
| **`DE190481HotelGateway`** | `8080` | Spring Cloud Gateway (WebFlux), Springdoc OpenAPI 3.0.3 | API Gateway, Route routing, CORS filter, Swagger aggregation |
| **`DE190481RoomService`** | `8081` | Spring Boot WebMVC, Spring Data JPA, MS SQL Server, Validation | Manage rooms, room types, pricing, capacity, and statuses |
| **`DE190481ReservationService`** | `8082` | Spring Boot WebMVC, Spring Data JPA, OpenFeign, MS SQL Server | Manage reservations, validate rooms via Feign, compute amounts |

---

## 2. Database Configuration

- **Database Server:** MS SQL Server (`localhost:1433`)
- **Database Name:** `MSS301_2026_PE`
- **Username:** `sa`
- **Password:** `sa`
- **Scripts:**
  - Tables DDL: `create_rooms_sql.txt`, `create_reservations_sql.txt`
  - Seed Data: `seed_data_sql.txt`

---

## 3. Package Structure Conventions

Strictly aligned with the exam requirements (package root per service):
- `DE190481RoomService`: `fu.de190481.room.*`
  - `entity`, `dto`, `repository`, `service`, `service.impl`, `controller`, `config`, `common`
- `DE190481ReservationService`: `fu.de190481.reservation.*`
  - `entity`, `dto`, `client`, `repository`, `service`, `service.impl`, `controller`, `config`, `common`
- `DE190481HotelGateway`: `fu.de190481.gateway.*`
  - `config`

---

## 4. Response Status Code Mapping

All endpoints wrap payloads inside a standard `ApiResponse<T>`:
```json
{
  "status": 1,
  "message": "Success message",
  "data": { ... },
  "timestamp": "2026-07-01 10:00:00"
}
```

| `ApiResponse.status` | HTTP Status Code | Meaning |
|---|---|---|
| **1** | `200 OK` or `201 CREATED` | Operation succeeded |
| **2** | `406 NOT_ACCEPTABLE` | Input validation failed |
| **3** | `226 IM_USED` | Unique constraint violated (e.g. duplicate `roomNumber`) |
| **4** | `404 NOT_FOUND` | Entity / Resource not found |
| **5** | `400 BAD_REQUEST` | Business rule conflict (room not `AVAILABLE`, capacity exceeded) |
| **0** | `500 INTERNAL_SERVER_ERROR` | System error |

---

## 5. Endpoints Specification

### A. Room Service (`/api/rooms`)

| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `POST` | `/api/rooms` | Create new room (default `status=AVAILABLE`) | `201` / `406` / `226` / `500` |
| `PUT` | `/api/rooms/{roomId}` | Partial update room | `200` / `406` / `226` / `404` / `500` |
| `GET` | `/api/rooms/{roomId}` | Get room detail | `200` / `404` / `500` |
| `DELETE` | `/api/rooms/{roomId}` | Soft delete room (status to `MAINTENANCE`) | `200` / `404` / `500` |
| `GET` | `/api/rooms` | List rooms with pagination (`page`, `size`) and filters (`roomType`, `status`) | `200` / `406` / `500` |

### B. Reservation Service (`/api/reservations`)

| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `POST` | `/api/reservations` | Create reservation (Validates room via OpenFeign, checks capacity & availability, calculates `totalAmount = pricePerNight * nights`) | `201` / `406` / `404` / `400` / `500` |
| `PUT` | `/api/reservations/{reservationId}` | Partial update reservation (recalculates `totalAmount` if dates or room change) | `200` / `406` / `404` / `400` / `500` |
| `GET` | `/api/reservations/{reservationId}` | Get detail with nested `RoomDTO` | `200` / `404` / `500` |
| `DELETE` | `/api/reservations/{reservationId}` | Cancel reservation (status to `CANCELLED`) | `200` / `404` / `500` |
| `GET` | `/api/reservations` | List reservations with pagination (`page`, `size`) and filters (`guestName`, `status`), each item includes `room` | `200` / `406` / `500` |

---

## 6. How to Run and Test

1. **Execute SQL scripts in SQL Server:**
   - Execute `Slot16/create_rooms_sql.txt`
   - Execute `Slot16/create_reservations_sql.txt`
   - Execute `Slot16/Code_PE/seed_data_sql.txt`
2. **Start Microservices (in order):**
   - Run `DE190481RoomService` (port `8081`)
   - Run `DE190481ReservationService` (port `8082`)
   - Run `DE190481HotelGateway` (port `8080`)
3. **Access Swagger UI via Gateway:**
   - Open browser: `http://localhost:8080/swagger-ui.html`
   - Use the definition dropdown in the top right to switch between `room-service` and `reservation-service`.
