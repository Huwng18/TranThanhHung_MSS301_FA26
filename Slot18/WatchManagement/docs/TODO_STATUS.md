# Theo dõi TODO và commit

<!-- [TODO-0.3] Index assignment work against the preserved Git history. -->

Repository: `Slot18/WatchManagement`, nhánh `main`. Các commit cũ được giữ nguyên. Mọi commit thường tạo trong phiên triển khai tiếp chỉ tham chiếu **một TODO**; một TODO có thể có commit triển khai và commit kiểm thử riêng. Các nhánh tính năng được merge `--no-ff`.

Đã kiểm chứng: 4 project build; 54 test Java PASS; 124 request / 341 assertion Newman PASS; outage, compensation dừng sau 3 lần retry, SQL đối chiếu A–G và reset Docker đều PASS. Bằng chứng trong [README](../README.md), `docs/evidence/` và `reports/`.

TODO-9.2 có 5 ảnh Newman thực tế. Ảnh giao diện Postman/Debugger cụ thể có thể chụp riêng nếu giảng viên yêu cầu. Các giới hạn về read timeout và recovery đã ghi trong README; không coi đây là workflow engine production.

| TODO | Yêu cầu | Commit tham chiếu (mới nhất trước) |
|---|---|---|
| TODO-0.1 | `git init` tại thư mục `WatchManagement/`, cài commit template | `7cac237` |
| TODO-0.2 | Tạo `.gitignore` (bỏ `target/`, `.idea/`, `.env`) và `.env.example` | `d1dc989`, `b8db214`, `7cac237` |
| TODO-0.3 | Commit khởi tạo trên `main`; mỗi bước sau làm trên nhánh riêng như sơ đồ | `7cac237` |
| TODO-1.1 | Tạo 4 project bằng Spring Initializr: Maven, Java 21, Boot **4.1.1**, group `com.mss301` | `25d6b97`, `d09f5f8` |
| TODO-1.2 | MSAccount, MSBrand, MSWatch: `webmvc`, `data-jpa`, `validation`, `mssql-jdbc` (runtime), Lombok; MSAccount thêm `security` | `d09f5f8` |
| TODO-1.3 | Import BOM Spring Cloud **2025.1.3**; MSWatch thêm `openfeign`; Gateway dùng `gateway-server-webflux` + `circuitbreaker-reactor-resilience4j` | `25d6b97` |
| TODO-1.4 | Khai báo version qua `<properties>`: jjwt **0.13.0** (MSAccount, Gateway), springdoc **3.0.2** (3 service MVC) | `25d6b97`, `d09f5f8` |
| TODO-1.5 | Lombok trong `maven-compiler-plugin` → `annotationProcessorPaths`; exclude Lombok khỏi fat-jar | `25d6b97`, `d09f5f8` |
| TODO-2.1 | Tạo DBAccount, DBBrand, DBWatch; bật SQL Server Authentication và login `sa` | `2c1775a` |
| TODO-2.2 | Cấu hình datasource cho 3 service: URL `${DB_HOST}`/`${DB_PORT}`, `ddl-auto: update`, `open-in-view: false`, `physical-strategy: PhysicalNamingStrategyStandardImpl` | `2c1775a` |
| TODO-3.1 | Entity `StoreAccount` (bảng `StoreAccounts`, `@Builder.Default isActive = true`), `AccountRepository.findByUsername`, seed 5 tài khoản với BCrypt | `ac0b3dd` |
| TODO-3.2 | `JwtService`: access token (claim `role`, `type=access`, 15 phút), refresh token (`type=refresh`, 7 ngày); cấu hình `jwt.*` đọc từ biến môi trường | `f918ff2` |
| TODO-3.3 | `AuthService.login` (inactive → 403, sai mật khẩu → 401) và `refresh` (chỉ nhận refresh token) | `7cc2553` |
| TODO-3.4 | `AuthController` (`/api/auth/login`, `/refresh`, `/logout`), `SecurityConfig` (permitAll + BCrypt), DTO, `GlobalExceptionHandler` | `bd8776e` |
| TODO-3.5 | Test trực tiếp cổng 8081 bằng Postman | `aad5391` |
| TODO-4.1 | Viết `docs/SAGA_DESIGN.md`: bảng bước / compensation / idempotent cho 3 Saga, state machine, pivot | `2f5d948` |
| TODO-4.2 | Giải thích được vì sao không dùng `@Transactional` / 2PC cho bài này | `3a000e2` |
| TODO-5.1 | Entity `WatchBrand` (bảng `WatchBrands`) và `BrandWatch` (bảng `BrandWatches`, `WatchID` unique) + 2 repository | `edb0ddb` |
| TODO-5.2 | `assignWatch(brandId, watchId)`: upsert, brand không tồn tại → 404, **idempotent** | `fcf0a4a` |
| TODO-5.3 | `unassignWatch(watchId)`: không có gì để gỡ → 200 `NOTHING_TO_DO` | `39dcfa4` |
| TODO-5.4 | `InternalBrandController`: `PUT /internal/brands/{b}/watches/{w}`, `DELETE /internal/brands/watches/{w}`; lỗi trả `ApiResponse` | `8d94f65` |
| TODO-5.5 | API public: `GET /api/brands` (có `watchCount`), `/api/brands/{id}`, `/api/brands/{id}/watches` | `18dab55` |
| TODO-5.6 | Seed 5 brand của đề bài + 10 dòng `BrandWatches` khớp WatchID → BrandID | `6dcd40b`, `d331172` |
| TODO-6.1 | yml: Feign timeout (connect 2s, read 5s), `brand-service.url`, `saga.fault-injection.enabled`, `saga.compensation.*` | `51b0c13` |
| TODO-6.2 | Entity `SagaState` (status kiểu enum) + `SagaLog` và 2 repository | `2d3197f` |
| TODO-6.3 | `SagaStatus` đủ 6 trạng thái của Slide 27 | `e2fdff5` |
| TODO-6.4 | `SagaTracker`: mọi thao tác ghi dùng `@Transactional(propagation = REQUIRES_NEW)` | `526067c`, `9f437e5` |
| TODO-6.5 | `WatchLocalService`: `insert`, `update`, `delete`, `deleteIfExists` (mỗi method 1 local tx) | `f70366b`, `96b1ee0` |
| TODO-6.6 | Record `WatchData` làm snapshot `before` / `after` | `c240a8b` |
| TODO-6.7 | `@EnableFeignClients` + `BrandServiceClient` gọi `/internal/brands/**` | `f892390` |
| TODO-6.8 | Record `SagaStep(action, compensation)`; `pivot()` khi không có compensation | `20890d6` |
| TODO-6.9 | `execute()`: chạy tuần tự, **push** bước đã commit vào stack; **không** `@Transactional` | `f9d1383`, `5d47ea7` |
| TODO-6.10 | `compensate()` LIFO + `compensateWithRetry()` 3 lần; lỗi hẳn → dừng ở `COMPENSATING` | `87cb733`, `4736b27` |
| TODO-6.11 | 3 Saga: `createWatch` (a), `updateWatch` (b), `deleteWatch` (c, pivot cuối) | `4830030`, `6ace9e6` |
| TODO-6.12 | `SagaFaultInjector` + header `X-Saga-Fail-After-Step`; bỏ qua lỗi giả sau pivot | `b98aee6` |
| TODO-6.13 | `WatchRequest` (name > 10 ký tự, price ≥ 100, đủ field); `WatchService` validate releaseDate, category **trước** Saga; list mới nhất trước | `a2747ce`, `68146a4`, `3b28fbc` |
| TODO-6.14 | `WatchController` (Admin only, header `X-Saga-Id`), `SagaController` (`/api/sagas`, `/api/sagas/{id}`) | `f3d8182` |
| TODO-6.15 | `SagaExecutionException` → HTTP 400/503/500 kèm `sagaId`, `sagaStatus`, `failedStep` | `7e12c2b`, `d2fa764`, `4536fcc` |
| TODO-6.16 | Entity `Watch` (BrandID chỉ là tham chiếu), `WatchCategory`; seed **đúng nguyên văn** đề bài | `ec87eb3`, `b63b4e4` |
| TODO-6.17 | Bean `feign.Logger.Level.BASIC`; dùng `JsonMapper` (Jackson 3) do Boot cấu hình | `dc2601f` |
| TODO-7.1 | Khai báo route dưới `spring.cloud.gateway.server.webflux.routes`: `/api/auth/**`, `/api/brands/**`, `/api/watches/**` | `d0544c9` |
| TODO-7.2 | Route watch-service thêm `/api/sagas/**`; **không** route `/internal/**`; CircuitBreaker + `FallbackController` | `1a93ce3` |
| TODO-7.3 | Luôn xoá `X-User-Id`, `X-User-Role` do client gửi lên | `622b808` |
| TODO-7.4 | Public chỉ gồm login, refresh, fallback và **`GET /api/watches`** | `0644869` |
| TODO-7.5 | Verify chữ ký + hạn JWT; từ chối token có `type != access`; gắn `X-User-Id`, `X-User-Role` | `b02dd2f` |
| TODO-7.6 | TimeLimiter `15s` cho accountCB/brandCB/watchCB | `ddc0885` |
| TODO-8.1 | Chạy đúng thứ tự MSAccount → MSBrand → MSWatch → APIGateway | `aa9199b` |
| TODO-8.2 | Smoke test qua Gateway: login, list watches, list brands | `28db825` |
| TODO-9.1 | Import và chạy Postman collection theo thứ tự; tất cả test **PASS** | `245d3fb` |
| TODO-9.2 | Chụp màn hình *Run results* + Console của folder 04, 05, 06, 07 | `d07d859` |
| TODO-9.3 | Kịch bản thủ công: tắt MSBrand (mục 6.3) | `ae51581` |
| TODO-9.4 | Kịch bản nâng cao: compensation thất bại → `COMPENSATING` (mục 6.4) | `defe905` |
| TODO-9.5 | Chạy query đối chiếu chéo sau mỗi kịch bản → 0 dòng | `86c26a5` |
| TODO-10.1 | Dockerfile multi-stage (maven build → temurin 21 JRE) cho 4 service; `sqlserver` có healthcheck `mssql-tools18/bin/sqlcmd -C` | `e47a20b` |
| TODO-10.2 | Service `db-init` tạo 3 database; các service `depends_on: service_completed_successfully` | `132ed6f`, `639129c` |
| TODO-10.3 | Truyền biến môi trường: `DB_*`, `JWT_*`, `BRAND_SERVICE_URL`, `SAGA_FAULT_INJECTION` (mặc định `true`) | `0f4a0bd` |
