# Watch Management — MSS301 Assignment 02

Hệ thống API quản lý đồng hồ dùng Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3, SQL Server và Saga Orchestration. Mã sinh viên: **DE190481**. Mã nguồn thuộc repository cha `TranThanhHung_MSS301_FA26`, nhánh `main`, trong thư mục `Slot18/WatchManagement`. GitHub Desktop chọn repository cha để xem lịch sử và push lên GitHub.

<!-- [TODO-8.1] Startup instructions and verified local execution evidence. -->

Đã kiểm chứng ngày 05/11/2026: cả 4 project build thành công, **54 test Java PASS**; Newman chạy **124 request, 341 assertion, 0 lỗi**. Kiểm thử MSBrand ngừng hoạt động, compensation hết retry và SQL đối chiếu A–G đều PASS. Sau khi reset volume Docker, collection chạy lại vẫn PASS và giữ đủ 10 watch seed.

Bằng chứng nằm trong [docs/evidence](docs/evidence/postman-results.html), [báo cáo SQL](reports/sql-consistency.json), [lỗi mạng](reports/network-failure.json), [compensation thất bại](reports/compensation-failure.json) và [reset Docker](reports/docker-reset.json). Đây là ảnh/báo cáo Newman thực tế; ảnh giao diện Postman/Debugger có thể chụp riêng nếu giảng viên yêu cầu.

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

## Postman / Newman

Import `WatchManagement_Saga.postman_collection.json` vào Postman và chạy toàn bộ folder theo thứ tự. Collection dùng chung token, WatchID và SagaID giữa các request; chạy trên dữ liệu seed sạch.

```powershell
.\tools\Run-Postman.ps1
.\tools\Capture-Evidence.ps1
```

Cần Node.js/npm để chạy Newman. Kết quả console và file trong `reports/` là bằng chứng của lần chạy thực tế; README này không thay thế báo cáo chạy. Đề bài và checklist gốc ở [`../Assignment_Readme.md`](../Assignment_Readme.md), thiết kế ở [`docs/SAGA_DESIGN.md`](docs/SAGA_DESIGN.md), giải thích transaction ở [`docs/TRANSACTIONS.md`](docs/TRANSACTIONS.md).

Hai kịch bản lỗi hạ tầng có script Python riêng, chạy lần lượt khi toàn bộ stack Docker đã sẵn sàng. Script dùng `.env.example` giống môi trường kiểm thử; nếu đã đổi mật khẩu/secret, chỉnh tham số `COMPOSE` trong helper cho đúng env-file trước khi chạy.

```powershell
python .\tools\test_failures.py
python .\tools\test_compensation_failure.py
python .\tools\verify_consistency.py
```

`test_failures.py` tắt MSBrand, kiểm tra CREATE trả 503 và xóa local watch bằng compensation; DELETE trả 503 nhưng giữ watch. Script khởi động lại MSBrand và dọn fixture trong `finally`, xuất `reports/network-failure.json`.

`test_compensation_failure.py` thay breakpoint Debug bằng proxy HTTP tại cổng host 18082. Một Compose override tạm ngoài repository trỏ MSWatch tới proxy; proxy chuyển PUT sang MSBrand thật, đợi transaction assign commit, tắt MSBrand rồi mới trả response. Lỗi giả sau bước 2 dẫn tới ba lần UNASSIGN thất bại và trạng thái `COMPENSATING`; local watch vẫn còn. Script phục hồi cấu hình gốc và MSBrand, kiểm tra link brand còn tồn tại, gỡ link và dọn watch thủ công; xuất `reports/compensation-failure.json`. Cần cổng 18082 trống và Docker Desktop truy cập được `host.docker.internal`; tạm tránh các thao tác ghi khác trong lúc chạy. Các báo cáo không chứa token đăng nhập. Đây là bằng chứng tự động của cùng ranh giới transaction, chưa thay thế ảnh Postman/Debugger nếu giảng viên yêu cầu ảnh giao diện.

`verify_consistency.py` chạy lại A–G trên watch riêng và chạy SQL FULL OUTER JOIN hai database sau từng kịch bản; báo cáo ở `reports/sql-consistency.json`. Query dùng trong SSMS nằm ở `scripts/verify-consistency.sql`.

## Giới hạn và khôi phục thủ công

Saga dùng stack compensation trong bộ nhớ. `SagaStates.Payload` lưu JSON `before`/`after`; trạng thái và log được ghi độc lập vào SQL Server. Chưa có job tự khôi phục Saga sau JVM crash hoặc Saga dừng ở `COMPENSATING`. Những dòng `PENDING`, `EXECUTING`, `FAILED`, `COMPENSATING` còn lại sau sự cố cần được kiểm tra dựa trên log và dữ liệu thực tế.

Feign có thể read-timeout sau khi MSBrand đã commit. `SagaExecutor` chỉ push compensation sau khi action trả về thành công, vì vậy timeout mơ hồ có thể để lại link sai dù trạng thái cuối là `COMPENSATED`. Chưa triển khai đăng ký compensation trước lời gọi remote, durable command/outbox hoặc cơ chế đối soát tự động. Không được chỉ dựa vào tên trạng thái để kết luận hai database đã khớp.

Khi xử lý thủ công, dừng các thao tác ghi lên watch liên quan; đọc `SagaStates`, `SagaLogs`, payload và cả hai database để xác định bước đã thực sự commit. Khôi phục dịch vụ lỗi trước khi thực hiện tiếp:

- **CREATE cần bù trừ:** gọi `DELETE http://localhost:8082/internal/brands/watches/{watchId}`; sau khi gỡ link thành công mới xóa watch trong DBWatch.
- **UPDATE cần bù trừ:** gọi `PUT http://localhost:8082/internal/brands/{before.brandId}/watches/{watchId}`, rồi khôi phục đầy đủ cột watch trong DBWatch từ snapshot `before`.
- **DELETE chưa qua pivot:** nếu watch còn tồn tại, gán lại brand từ snapshot `before`; giữ nguyên watch. Nếu watch đã bị xóa tại pivot, hoàn tất theo chiều tiến và bảo đảm link MSBrand đã được gỡ.

Public API không có endpoint chuyên dụng để nạp snapshot lịch sử hoặc resume Saga. Các chỉnh sửa local trong quy trình này do người vận hành thực hiện bằng transaction SQL trên đúng database, ghi lại bằng chứng sửa chữa và chỉ cập nhật trạng thái Saga sau khi đối soát xong. Không chạy bù trừ DELETE sau pivot bằng cách tạo lại watch với IDENTITY mới.
