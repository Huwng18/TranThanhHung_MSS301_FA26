# Theo dõi TODO và commit

<!-- [TODO-0.3] Index assignment work against the preserved Git history. -->

Repository: `TranThanhHung_MSS301_FA26`, nhánh `main`; mã nguồn ở `Slot18/WatchManagement`. Tác giả commit của Slot18 được thống nhất thành `Huwng18 <hungthanh18122005@gmail.com>`. Lịch sử giữ riêng từng thay đổi TODO; một TODO có thể có commit triển khai và commit kiểm thử riêng. Các nhánh tính năng được merge `--no-ff`.

History cleanup removed 36 report/helper paths and pruned six report-only commits; the references below point to the filtered history.

Theo yêu cầu hiện tại của bạn, **TODO-9 chỉ bàn giao một file [WatchManagement_Saga.postman_collection.json](../WatchManagement_Saga.postman_collection.json)** có đầy đủ request, biến và script kiểm thử để import vào Postman. Các yêu cầu báo cáo/ảnh/script riêng thuộc TODO-9.2–9.5 trong tài liệu gốc được thay bằng phạm vi này. Hướng dẫn chạy collection nằm trong [README](../README.md).

Các giới hạn về read timeout và recovery của Saga được ghi trong README.

| TODO | Yêu cầu | Commit tham chiếu (mới nhất trước) |
|---|---|---|
| TODO-0.1 | `git init` tại thư mục `WatchManagement/`, cài commit template | `32a5e1c` |
| TODO-0.2 | Tạo `.gitignore` (bỏ `target/`, `.idea/`, `.env`) và `.env.example` | `f49f4b9`, `1f78ed8`, `32a5e1c` |
| TODO-0.3 | Commit khởi tạo trên `main`; mỗi bước sau làm trên nhánh riêng như sơ đồ | `32a5e1c` |
| TODO-1.1 | Tạo 4 project bằng Spring Initializr: Maven, Java 21, Boot **4.1.1**, group `com.mss301` | `2f1ec41`, `7e40f96` |
| TODO-1.2 | MSAccount, MSBrand, MSWatch: `webmvc`, `data-jpa`, `validation`, `mssql-jdbc` (runtime), Lombok; MSAccount thêm `security` | `7e40f96` |
| TODO-1.3 | Import BOM Spring Cloud **2025.1.3**; MSWatch thêm `openfeign`; Gateway dùng `gateway-server-webflux` + `circuitbreaker-reactor-resilience4j` | `2f1ec41` |
| TODO-1.4 | Khai báo version qua `<properties>`: jjwt **0.13.0** (MSAccount, Gateway), springdoc **3.0.2** (3 service MVC) | `2f1ec41`, `7e40f96` |
| TODO-1.5 | Lombok trong `maven-compiler-plugin` → `annotationProcessorPaths`; exclude Lombok khỏi fat-jar | `2f1ec41`, `7e40f96` |
| TODO-2.1 | Tạo DBAccount, DBBrand, DBWatch; bật SQL Server Authentication và login `sa` | `63c4e56` |
| TODO-2.2 | Cấu hình datasource cho 3 service: URL `${DB_HOST}`/`${DB_PORT}`, `ddl-auto: update`, `open-in-view: false`, `physical-strategy: PhysicalNamingStrategyStandardImpl` | `63c4e56` |
| TODO-3.1 | Entity `StoreAccount` (bảng `StoreAccounts`, `@Builder.Default isActive = true`), `AccountRepository.findByUsername`, seed 5 tài khoản với BCrypt | `0c7150e` |
| TODO-3.2 | `JwtService`: access token (claim `role`, `type=access`, 15 phút), refresh token (`type=refresh`, 7 ngày); cấu hình `jwt.*` đọc từ biến môi trường | `098e822` |
| TODO-3.3 | `AuthService.login` (inactive → 403, sai mật khẩu → 401) và `refresh` (chỉ nhận refresh token) | `5c710ee` |
| TODO-3.4 | `AuthController` (`/api/auth/login`, `/refresh`, `/logout`), `SecurityConfig` (permitAll + BCrypt), DTO, `GlobalExceptionHandler` | `1fc8de6` |
| TODO-3.5 | Test trực tiếp cổng 8081 bằng Postman | `02cc41d` |
| TODO-4.1 | Viết `docs/SAGA_DESIGN.md`: bảng bước / compensation / idempotent cho 3 Saga, state machine, pivot | `a95d3e8` |
| TODO-4.2 | Giải thích được vì sao không dùng `@Transactional` / 2PC cho bài này | `8e95bc2` |
| TODO-5.1 | Entity `WatchBrand` (bảng `WatchBrands`) và `BrandWatch` (bảng `BrandWatches`, `WatchID` unique) + 2 repository | `3d7cade` |
| TODO-5.2 | `assignWatch(brandId, watchId)`: upsert, brand không tồn tại → 404, **idempotent** | `8298337` |
| TODO-5.3 | `unassignWatch(watchId)`: không có gì để gỡ → 200 `NOTHING_TO_DO` | `598fae4` |
| TODO-5.4 | `InternalBrandController`: `PUT /internal/brands/{b}/watches/{w}`, `DELETE /internal/brands/watches/{w}`; lỗi trả `ApiResponse` | `503deff` |
| TODO-5.5 | API public: `GET /api/brands` (có `watchCount`), `/api/brands/{id}`, `/api/brands/{id}/watches` | `7fb4a67` |
| TODO-5.6 | Seed 5 brand của đề bài + 10 dòng `BrandWatches` khớp WatchID → BrandID | `fa5afdb`, `2e7f638` |
| TODO-6.1 | yml: Feign timeout (connect 2s, read 5s), `brand-service.url`, `saga.fault-injection.enabled`, `saga.compensation.*` | `6347c71` |
| TODO-6.2 | Entity `SagaState` (status kiểu enum) + `SagaLog` và 2 repository | `f50d9c4` |
| TODO-6.3 | `SagaStatus` đủ 6 trạng thái của Slide 27 | `2198a8e` |
| TODO-6.4 | `SagaTracker`: mọi thao tác ghi dùng `@Transactional(propagation = REQUIRES_NEW)` | `a61601e`, `de50bb8` |
| TODO-6.5 | `WatchLocalService`: `insert`, `update`, `delete`, `deleteIfExists` (mỗi method 1 local tx) | `d32f516`, `065b070` |
| TODO-6.6 | Record `WatchData` làm snapshot `before` / `after` | `afe3ecc` |
| TODO-6.7 | `@EnableFeignClients` + `BrandServiceClient` gọi `/internal/brands/**` | `00645ee` |
| TODO-6.8 | Record `SagaStep(action, compensation)`; `pivot()` khi không có compensation | `3a0e0fe` |
| TODO-6.9 | `execute()`: chạy tuần tự, **push** bước đã commit vào stack; **không** `@Transactional` | `b7de5f0`, `6c388b7` |
| TODO-6.10 | `compensate()` LIFO + `compensateWithRetry()` 3 lần; lỗi hẳn → dừng ở `COMPENSATING` | `24aaa74`, `7114a65` |
| TODO-6.11 | 3 Saga: `createWatch` (a), `updateWatch` (b), `deleteWatch` (c, pivot cuối) | `be0e783`, `45d22cf` |
| TODO-6.12 | `SagaFaultInjector` + header `X-Saga-Fail-After-Step`; bỏ qua lỗi giả sau pivot | `b1dd1a7` |
| TODO-6.13 | `WatchRequest` (name > 10 ký tự, price ≥ 100, đủ field); `WatchService` validate releaseDate, category **trước** Saga; list mới nhất trước | `fcc8f68`, `1efe826`, `b8889bb` |
| TODO-6.14 | `WatchController` (Admin only, header `X-Saga-Id`), `SagaController` (`/api/sagas`, `/api/sagas/{id}`) | `534cc6c` |
| TODO-6.15 | `SagaExecutionException` → HTTP 400/503/500 kèm `sagaId`, `sagaStatus`, `failedStep` | `0546931`, `a52b65c`, `f8a2990` |
| TODO-6.16 | Entity `Watch` (BrandID chỉ là tham chiếu), `WatchCategory`; seed **đúng nguyên văn** đề bài | `14ce3c1`, `84fee6c` |
| TODO-6.17 | Bean `feign.Logger.Level.BASIC`; dùng `JsonMapper` (Jackson 3) do Boot cấu hình | `91ffbc8` |
| TODO-7.1 | Khai báo route dưới `spring.cloud.gateway.server.webflux.routes`: `/api/auth/**`, `/api/brands/**`, `/api/watches/**` | `69b4147` |
| TODO-7.2 | Route watch-service thêm `/api/sagas/**`; **không** route `/internal/**`; CircuitBreaker + `FallbackController` | `6975628` |
| TODO-7.3 | Luôn xoá `X-User-Id`, `X-User-Role` do client gửi lên | `5bb5f24` |
| TODO-7.4 | Public chỉ gồm login, refresh, fallback và **`GET /api/watches`** | `41d5497` |
| TODO-7.5 | Verify chữ ký + hạn JWT; từ chối token có `type != access`; gắn `X-User-Id`, `X-User-Role` | `ce76653` |
| TODO-7.6 | TimeLimiter `15s` cho accountCB/brandCB/watchCB | `e419fc4` |
| TODO-8.1 | Chạy đúng thứ tự MSAccount → MSBrand → MSWatch → APIGateway | `7f52c0a` |
| TODO-8.2 | Smoke test qua Gateway: login, list watches, list brands | — (kiểm tra trực tiếp bằng collection Postman) |
| TODO-9.1 | M?t JSON t? ch?a to?n b? request, bi?n v? script ki?m th? Postman | `d62dffc` |
| TODO-9.2 | Báo cáo/ảnh riêng được bỏ theo yêu cầu hiện tại; xem phạm vi TODO-9.1 | — |
| TODO-9.3 | Script lỗi hạ tầng riêng được bỏ theo yêu cầu hiện tại; xem phạm vi TODO-9.1 | — |
| TODO-9.4 | Script compensation failure riêng được bỏ theo yêu cầu hiện tại; xem phạm vi TODO-9.1 | — |
| TODO-9.5 | Báo cáo/script đối chiếu riêng được bỏ theo yêu cầu hiện tại; xem phạm vi TODO-9.1 | — |
| TODO-10.1 | Dockerfile multi-stage (maven build → temurin 21 JRE) cho 4 service; `sqlserver` có healthcheck `mssql-tools18/bin/sqlcmd -C` | `a9ce455` |
| TODO-10.2 | Service `db-init` tạo 3 database; các service `depends_on: service_completed_successfully` | `b75edf3` |
| TODO-10.3 | Truyền biến môi trường: `DB_*`, `JWT_*`, `BRAND_SERVICE_URL`, `SAGA_FAULT_INJECTION` (mặc định `true`) | `2159f48` |
