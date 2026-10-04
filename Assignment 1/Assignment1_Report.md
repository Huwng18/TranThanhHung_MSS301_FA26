# BÁO CÁO ASSIGNMENT 1

**Mục lục** (Vui lòng chọn Update Table of Contents trong Word)

## F0 – Hạ tầng & khởi tạo project

### TODO 0.1: Tạo thư mục gốc fu-cinema/, docker-compose.yml chạy 3 database
- **Commit Message:** `chore: setup docker-compose with sqlserver, mongo and mysql`
- **Kết quả:** Đã tạo file `docker-compose.yml` định nghĩa các container `cinema-sqlserver`, `cinema-sqlserver-init`, `cinema-mongo`, `cinema-mysql`.

[Chèn ảnh chụp màn hình docker-compose up thành công tại đây]

### TODO 0.2: Script tạo database
- **Commit Message:** `chore: add init scripts for sqlserver and mysql`
- **Kết quả:** Đã tạo `sqlserver/init.sql` và `mysql/init.sql`.

[Chèn ảnh chụp màn hình database được tạo trong DBeaver/DataGrip tại đây]

### TODO 0.3: Generate 3 project service
- **Commit Message:** `init: generate customer-service, movie-service, booking-service`
- **Kết quả:** Khởi tạo project thành công.

[Chèn ảnh cấu trúc thư mục project tại đây]

### TODO 0.4: Generate project api-gateway
- **Commit Message:** `init: generate api-gateway`
- **Kết quả:** Khởi tạo project thành công.

[Chèn ảnh cấu trúc api-gateway tại đây]

## F1 – Authentication (customer-service + Gateway)
- **Commit Message:** `feat: implement authentication with jwt and bcrypt`

[Chèn ảnh kết quả test Postman Login thành công / thất bại tại đây]

## F2 – Customer: Register & Profile (customer-service)
- **Commit Message:** `feat: implement customer registration and profile management`

[Chèn ảnh kết quả Register và View Profile từ Postman tại đây]

## F3 – Admin: Manage customers (customer-service)
- **Commit Message:** `feat: add admin APIs for managing customers`

[Chèn ảnh kết quả Get List Customers từ Postman tại đây]

## F4 – Admin: Manage genres & cinema rooms (movie-service)
- **Commit Message:** `feat: add genre and cinema room management`

[Chèn ảnh MongoDB DataSeeder / Kết quả API tại đây]

## F5 – Admin: Manage movies (movie-service)
- **Commit Message:** `feat: implement movie management with mongodb`

[Chèn ảnh kết quả test API Movies tại đây]

## F6 – Admin: Manage showtimes (movie-service)
- **Commit Message:** `feat: implement showtime scheduling and conflict validation`

[Chèn ảnh kết quả test tạo Showtime tại đây]

## F7 – Customer: Create booking (booking-service + OpenFeign)
- **Commit Message:** `feat: implement ticket booking with feign client`

[Chèn ảnh kết quả test Create Booking tại đây]

## F8 – Customer: Booking history & cancel (booking-service)
- **Commit Message:** `feat: add booking history and cancellation`

[Chèn ảnh kết quả test My Bookings tại đây]

## F9 – Admin: Report statistic (booking-service)
- **Commit Message:** `feat: implement revenue report by period`

[Chèn ảnh kết quả Report API tại đây]

## F10 – API Gateway: Routing & Security
- **Commit Message:** `feat: configure gateway routes and security filters`

[Chèn ảnh cấu hình Gateway tại đây]

## F11 – Kiểm thử bằng Postman
- **Commit Message:** `test: add postman collection with test scripts`

[Chèn ảnh kết quả Collection Runner pass tất cả các test tại đây]

