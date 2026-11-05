# Watch Management — MSS301 Assignment 02

Hệ thống API quản lý đồng hồ dùng Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3, SQL Server và Saga Orchestration. Mã sinh viên: **DE190481**. Mã nguồn thuộc repository cha `TranThanhHung_MSS301_FA26`, nhánh `main`, trong thư mục `Slot18/WatchManagement`. GitHub Desktop chọn repository cha để xem lịch sử và push lên GitHub.

<!-- [TODO-8.1] Build and startup instructions. -->

| Service | Cổng | Database | Vai trò |
|---|---:|---|---|
| APIGateway_DE190481 | 8080 | — | JWT, routing, CircuitBreaker |
| MSAccount_DE190481 | 8081 | DBAccount | Đăng nhập, access/refresh token |
| MSBrand_DE190481 | 8082 | DBBrand | Brands, BrandWatches; Saga participant |
| MSWatch_DE190481 | 8083 | DBWatch | Watches, categories, Saga state/log; orchestrator |

Client gọi qua `http://localhost:8080`. Các cổng 8081–8083 trong Compose chỉ bind localhost để hỗ trợ kiểm thử trực tiếp. Gateway không route `/internal/**`.

## Build và test bằng PowerShell

Chạy các lệnh sau tại thư mục chứa README. Cần JDK 21 trở lên và Maven 3.9; Maven biên dịch với `release 21`.

```powershell
mvn -f .\MSAccount_DE190481\pom.xml clean package
mvn -f .\MSBrand_DE190481\pom.xml clean package
mvn -f .\MSWatch_DE190481\pom.xml clean package
mvn -f .\APIGateway_DE190481\pom.xml clean package
```

Mỗi lệnh chạy các test của service, tạo JAR trong `target/`; báo cáo JUnit nằm tại `target/surefire-reports/`. Build Docker dùng Maven trong image Java 21 và bỏ qua test, vì vậy nên chạy các lệnh trên trước.

## Chạy bằng Docker Compose

Cần Docker Desktop đang chạy Linux containers.

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
# Sửa .env để đặt mật khẩu SQL Server và JWT secret dùng chung Account/Gateway.
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose --env-file .env ps -a
docker compose --env-file .env logs -f ms-watch
```

Nếu cổng host 1433 không dùng được, đặt `DB_PORT=11433` trong `.env`.

`sqlserver` phải healthy; `db-init` tạo DBAccount, DBBrand, DBWatch rồi thoát mã 0. Các ứng dụng chờ khởi tạo database; MSBrand chờ MSAccount; MSWatch chờ MSBrand; Gateway chờ cả ba service. Theo dõi log để xác nhận các initializer đã chạy xong trước khi chạy collection.

Các biến mẫu gồm `DB_HOST`, `DB_PORT`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `REFRESH_EXPIRATION`, `SAGA_FAULT_INJECTION`. Trong Docker, Compose đặt `DB_HOST=sqlserver` và cổng database nội bộ 1433; `DB_PORT` trong `.env` chọn cổng SQL Server trên máy host. `BRAND_SERVICE_URL`, `ACCOUNT_SERVICE_URL`, `WATCH_SERVICE_URL` được nối tới tên service trong mạng Compose. `SAGA_FAULT_INJECTION=true` bật header demo lỗi; đặt `false` để tắt.

```powershell
docker compose --env-file .env down
# Chỉ khi muốn xóa toàn bộ dữ liệu và seed lại từ đầu:
docker compose --env-file .env down -v
```

Để chạy Java trực tiếp, chỉ khởi động SQL bằng `docker compose --env-file .env up -d sqlserver db-init`; đặt các biến môi trường cần thiết trong từng terminal hoặc IntelliJ, rồi chạy JAR theo thứ tự Account → Brand → Watch → Gateway. Spring Boot không tự đọc file `.env` khi chạy JAR trực tiếp.

## Gọi API nhanh

```powershell
$baseUrl = 'http://localhost:8080'
Invoke-RestMethod "$baseUrl/api/watches"
$loginBody = @{ username = 'adminsys'; password = '@1' } | ConvertTo-Json
$login = Invoke-RestMethod "$baseUrl/api/auth/login" -Method Post `
    -ContentType 'application/json' -Body $loginBody
$headers = @{ Authorization = "Bearer $($login.data.accessToken)" }
Invoke-RestMethod "$baseUrl/api/brands" -Headers $headers
Invoke-RestMethod "$baseUrl/api/sagas?limit=20" -Headers $headers
```

Các tài khoản seed dùng mật khẩu `@1`: `adminsys` (Admin), `staff02` (Staff), `member01` (Member); `member02` và `staff01` bị khóa. Chỉ Admin được thêm/sửa/xóa watch và xem Saga. `GET /api/watches` công khai, trả `categoryName` và sắp xếp watch mới nhất trước.

Thêm/sửa watch yêu cầu đủ field, tên dài hơn 10 ký tự, giá ≥100, ngày phát hành bằng ngày hiện tại của service. Header response `X-Saga-Id` dùng để đọc `GET /api/sagas/{sagaId}`. Header request `X-Saga-Fail-After-Step: 1` hoặc `2` tạo lỗi sau bước đã commit; DELETE sau bước 2 là pivot nên lỗi giả bị bỏ qua.

## Kiểm thử bằng Postman

<!-- [TODO-9.1] One self-contained collection for import and execution in Postman. -->

TODO-9 bàn giao một file [WatchManagement_Saga.postman_collection.json](WatchManagement_Saga.postman_collection.json), chứa toàn bộ request, biến, pre-request script và assertion. Bạn chỉ cần import file này vào Postman để chạy kiểm thử; collection tự đăng nhập, lấy token, tạo ngày hiện tại theo Việt Nam và truyền WatchID/SagaID giữa các request.

1. Khởi động SQL Server và cả bốn ứng dụng; đặt `SAGA_FAULT_INJECTION=true` để chạy các kịch bản bù trừ.
2. Đảm bảo database có đủ 10 watch seed cùng 5 brand ban đầu. Collection chỉ sửa watch kiểm thử do nó tạo, sau đó xóa watch này để giữ nguyên dữ liệu seed.
3. Import file JSON vào Postman. Kiểm tra các collection variable bên dưới nếu ứng dụng chạy ở địa chỉ khác.
4. Chạy toàn bộ collection theo thứ tự folder **00 → 08**. Các request dùng chung biến nên cần chạy theo đúng thứ tự, từ bước đăng nhập.

| Collection variable | Giá trị mặc định | Dùng cho |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | API qua Gateway |
| `accountUrl` | `http://localhost:8081` | Kiểm thử trực tiếp MSAccount |
| `brandUrl` | `http://localhost:8082` | Kiểm thử trực tiếp MSBrand và tính idempotent |
| `watchUrl` | `http://localhost:8083` | Kiểm thử trực tiếp MSWatch |

Các biến này đã nằm trong collection, nên không cần import thêm file environment. Kết quả assertion hiện ngay trong Postman; script cũng ghi các bước Saga ra Postman Console. Một số request cố ý gây lỗi trả 400/401/403/500 để kiểm tra validation, phân quyền và compensation; chúng đạt khi assertion xác nhận đúng kết quả mong đợi.

Collection kiểm tra đăng nhập/JWT, phân quyền, mọi field bắt buộc, CRUD, thứ tự watch mới nhất, các Saga A–G, khôi phục snapshot, pivot của DELETE và đối chiếu dữ liệu qua API của cả năm brand. Sau khi chạy thành công, có thể chạy lại collection với cùng dữ liệu seed còn nguyên.

Đề bài gốc ở [`../Assignment_Readme.md`](../Assignment_Readme.md), thiết kế ở [`docs/SAGA_DESIGN.md`](docs/SAGA_DESIGN.md), giải thích transaction ở [`docs/TRANSACTIONS.md`](docs/TRANSACTIONS.md). Phạm vi TODO-9 hiện tại theo yêu cầu của bạn là collection JSON; các báo cáo và công cụ tạo ảnh riêng không còn thuộc phần bàn giao.

## Giới hạn và khôi phục thủ công

Saga dùng stack compensation trong bộ nhớ. `SagaStates.Payload` lưu JSON `before`/`after`; trạng thái và log được ghi độc lập vào SQL Server. Chưa có job tự khôi phục Saga sau JVM crash hoặc Saga dừng ở `COMPENSATING`. Những dòng `PENDING`, `EXECUTING`, `FAILED`, `COMPENSATING` còn lại sau sự cố cần được kiểm tra dựa trên log và dữ liệu thực tế.

Feign có thể read-timeout sau khi MSBrand đã commit. `SagaExecutor` chỉ push compensation sau khi action trả về thành công, vì vậy timeout mơ hồ có thể để lại link sai dù trạng thái cuối là `COMPENSATED`. Chưa triển khai đăng ký compensation trước lời gọi remote, durable command/outbox hoặc cơ chế đối soát tự động. Không được chỉ dựa vào tên trạng thái để kết luận hai database đã khớp.

Khi xử lý thủ công, dừng các thao tác ghi lên watch liên quan; đọc `SagaStates`, `SagaLogs`, payload và cả hai database để xác định bước đã thực sự commit. Khôi phục dịch vụ lỗi trước khi thực hiện tiếp:

- **CREATE cần bù trừ:** gọi `DELETE http://localhost:8082/internal/brands/watches/{watchId}`; sau khi gỡ link thành công mới xóa watch trong DBWatch.
- **UPDATE cần bù trừ:** gọi `PUT http://localhost:8082/internal/brands/{before.brandId}/watches/{watchId}`, rồi khôi phục đầy đủ cột watch trong DBWatch từ snapshot `before`.
- **DELETE chưa qua pivot:** nếu watch còn tồn tại, gán lại brand từ snapshot `before`; giữ nguyên watch. Nếu watch đã bị xóa tại pivot, hoàn tất theo chiều tiến và bảo đảm link MSBrand đã được gỡ.

Public API không có endpoint chuyên dụng để nạp snapshot lịch sử hoặc resume Saga. Các chỉnh sửa local trong quy trình này do người vận hành thực hiện bằng transaction SQL trên đúng database, ghi lại bằng chứng sửa chữa và chỉ cập nhật trạng thái Saga sau khi đối soát xong. Không chạy bù trừ DELETE sau pivot bằng cách tạo lại watch với IDENTITY mới.
