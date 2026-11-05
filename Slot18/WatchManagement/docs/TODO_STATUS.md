# Theo dõi TODO và commit

<!-- [TODO-0.3] Index assignment work against the preserved Git history. -->

Repository: `TranThanhHung_MSS301_FA26`, nhánh `main`; mã nguồn ở `Slot18/WatchManagement`. Tác giả commit của Slot18 được thống nhất thành `Huwng18 <hungthanh18122005@gmail.com>`. Lịch sử giữ riêng từng thay đổi TODO; một TODO có thể có commit triển khai và commit kiểm thử riêng. Các nhánh tính năng được merge `--no-ff`.

Theo yêu cầu hiện tại của bạn, **TODO-9 chỉ bàn giao một file [WatchManagement_Saga.postman_collection.json](../WatchManagement_Saga.postman_collection.json)** có đầy đủ request, biến và script kiểm thử để import vào Postman. Các yêu cầu báo cáo/ảnh/script riêng thuộc TODO-9.2–9.5 trong tài liệu gốc được thay bằng phạm vi này. Hướng dẫn chạy collection nằm trong [README](../README.md).

Các giới hạn về read timeout và recovery của Saga được ghi trong README.

| TODO | Yêu cầu | Commit tham chiếu (mới nhất trước) |
|---|---|---|
| TODO-0.1 | `git init` tại thư mục `WatchManagement/`, cài commit template | `6dae174` |
| TODO-0.2 | Tạo `.gitignore` (bỏ `target/`, `.idea/`, `.env`) và `.env.example` | `d0324a1`, `8b5bb3e`, `6dae174` |
| TODO-0.3 | Commit khởi tạo trên `main`; mỗi bước sau làm trên nhánh riêng như sơ đồ | `6dae174` |
| TODO-1.1 | Tạo 4 project bằng Spring Initializr: Maven, Java 21, Boot **4.1.1**, group `com.mss301` | `22036d2`, `466dd6b` |
| TODO-1.2 | MSAccount, MSBrand, MSWatch: `webmvc`, `data-jpa`, `validation`, `mssql-jdbc` (runtime), Lombok; MSAccount thêm `security` | `466dd6b` |
| TODO-1.3 | Import BOM Spring Cloud **2025.1.3**; MSWatch thêm `openfeign`; Gateway dùng `gateway-server-webflux` + `circuitbreaker-reactor-resilience4j` | `22036d2` |
| TODO-1.4 | Khai báo version qua `<properties>`: jjwt **0.13.0** (MSAccount, Gateway), springdoc **3.0.2** (3 service MVC) | `22036d2`, `466dd6b` |
| TODO-1.5 | Lombok trong `maven-compiler-plugin` → `annotationProcessorPaths`; exclude Lombok khỏi fat-jar | `22036d2`, `466dd6b` |
| TODO-2.1 | Tạo DBAccount, DBBrand, DBWatch; bật SQL Server Authentication và login `sa` | `4a2ba57` |
| TODO-2.2 | Cấu hình datasource cho 3 service: URL `${DB_HOST}`/`${DB_PORT}`, `ddl-auto: update`, `open-in-view: false`, `physical-strategy: PhysicalNamingStrategyStandardImpl` | `4a2ba57` |
| TODO-3.1 | Entity `StoreAccount` (bảng `StoreAccounts`, `@Builder.Default isActive = true`), `AccountRepository.findByUsername`, seed 5 tài khoản với BCrypt | `f85de6f` |
| TODO-3.2 | `JwtService`: access token (claim `role`, `type=access`, 15 phút), refresh token (`type=refresh`, 7 ngày); cấu hình `jwt.*` đọc từ biến môi trường | `a87467f` |
| TODO-3.3 | `AuthService.login` (inactive → 403, sai mật khẩu → 401) và `refresh` (chỉ nhận refresh token) | `1f59448` |
| TODO-3.4 | `AuthController` (`/api/auth/login`, `/refresh`, `/logout`), `SecurityConfig` (permitAll + BCrypt), DTO, `GlobalExceptionHandler` | `74be3b8` |
| TODO-3.5 | Test trực tiếp cổng 8081 bằng Postman | `c3fc7dd` |
| TODO-4.1 | Viết `docs/SAGA_DESIGN.md`: bảng bước / compensation / idempotent cho 3 Saga, state machine, pivot | `b4fa789` |
| TODO-4.2 | Giải thích được vì sao không dùng `@Transactional` / 2PC cho bài này | `dcab156` |
| TODO-5.1 | Entity `WatchBrand` (bảng `WatchBrands`) và `BrandWatch` (bảng `BrandWatches`, `WatchID` unique) + 2 repository | `6c889af` |
| TODO-5.2 | `assignWatch(brandId, watchId)`: upsert, brand không tồn tại → 404, **idempotent** | `195c17e` |
| TODO-5.3 | `unassignWatch(watchId)`: không có gì để gỡ → 200 `NOTHING_TO_DO` | `e96f2b2` |
| TODO-5.4 | `InternalBrandController`: `PUT /internal/brands/{b}/watches/{w}`, `DELETE /internal/brands/watches/{w}`; lỗi trả `ApiResponse` | `ebc300b` |
| TODO-5.5 | API public: `GET /api/brands` (có `watchCount`), `/api/brands/{id}`, `/api/brands/{id}/watches` | `75ca022` |
| TODO-5.6 | Seed 5 brand của đề bài + 10 dòng `BrandWatches` khớp WatchID → BrandID | `d443501`, `430b7f3` |
| TODO-6.1 | yml: Feign timeout (connect 2s, read 5s), `brand-service.url`, `saga.fault-injection.enabled`, `saga.compensation.*` | `f43163f` |
| TODO-6.2 | Entity `SagaState` (status kiểu enum) + `SagaLog` và 2 repository | `17f928a` |
| TODO-6.3 | `SagaStatus` đủ 6 trạng thái của Slide 27 | `8c3b35c` |
| TODO-6.4 | `SagaTracker`: mọi thao tác ghi dùng `@Transactional(propagation = REQUIRES_NEW)` | `1fd845f`, `1bb932d` |
| TODO-6.5 | `WatchLocalService`: `insert`, `update`, `delete`, `deleteIfExists` (mỗi method 1 local tx) | `db23664`, `0a8f976` |
| TODO-6.6 | Record `WatchData` làm snapshot `before` / `after` | `0d83c77` |
| TODO-6.7 | `@EnableFeignClients` + `BrandServiceClient` gọi `/internal/brands/**` | `868cf66` |
| TODO-6.8 | Record `SagaStep(action, compensation)`; `pivot()` khi không có compensation | `2d77449` |
| TODO-6.9 | `execute()`: chạy tuần tự, **push** bước đã commit vào stack; **không** `@Transactional` | `98377b7`, `0fb4a3d` |
| TODO-6.10 | `compensate()` LIFO + `compensateWithRetry()` 3 lần; lỗi hẳn → dừng ở `COMPENSATING` | `67e5924`, `3d15d0a` |
| TODO-6.11 | 3 Saga: `createWatch` (a), `updateWatch` (b), `deleteWatch` (c, pivot cuối) | `34b248c`, `2761f43` |
| TODO-6.12 | `SagaFaultInjector` + header `X-Saga-Fail-After-Step`; bỏ qua lỗi giả sau pivot | `0582598` |
| TODO-6.13 | `WatchRequest` (name > 10 ký tự, price ≥ 100, đủ field); `WatchService` validate releaseDate, category **trước** Saga; list mới nhất trước | `ff584ef`, `40e64cd`, `66ab63c` |
| TODO-6.14 | `WatchController` (Admin only, header `X-Saga-Id`), `SagaController` (`/api/sagas`, `/api/sagas/{id}`) | `7cc97d5` |
| TODO-6.15 | `SagaExecutionException` → HTTP 400/503/500 kèm `sagaId`, `sagaStatus`, `failedStep` | `6c290c9`, `2240084`, `e5d457b` |
| TODO-6.16 | Entity `Watch` (BrandID chỉ là tham chiếu), `WatchCategory`; seed **đúng nguyên văn** đề bài | `0bea6d3`, `4cb3816` |
| TODO-6.17 | Bean `feign.Logger.Level.BASIC`; dùng `JsonMapper` (Jackson 3) do Boot cấu hình | `6c0682d` |
| TODO-7.1 | Khai báo route dưới `spring.cloud.gateway.server.webflux.routes`: `/api/auth/**`, `/api/brands/**`, `/api/watches/**` | `1c17aef` |
| TODO-7.2 | Route watch-service thêm `/api/sagas/**`; **không** route `/internal/**`; CircuitBreaker + `FallbackController` | `a5c8f2e` |
| TODO-7.3 | Luôn xoá `X-User-Id`, `X-User-Role` do client gửi lên | `bdaa44d` |
| TODO-7.4 | Public chỉ gồm login, refresh, fallback và **`GET /api/watches`** | `ddbe38e` |
| TODO-7.5 | Verify chữ ký + hạn JWT; từ chối token có `type != access`; gắn `X-User-Id`, `X-User-Role` | `5da6bc1` |
| TODO-7.6 | TimeLimiter `15s` cho accountCB/brandCB/watchCB | `2da706c` |
| TODO-8.1 | Chạy đúng thứ tự MSAccount → MSBrand → MSWatch → APIGateway | `09e8b45` |
| TODO-8.2 | Smoke test qua Gateway: login, list watches, list brands | `978df09` |
| TODO-9.1 | Import và chạy Postman collection theo thứ tự; tất cả test **PASS** | `1519e4c` |
| TODO-9.2 | Báo cáo/ảnh riêng được bỏ theo yêu cầu hiện tại; xem phạm vi TODO-9.1 | — |
| TODO-9.3 | Script lỗi hạ tầng riêng được bỏ theo yêu cầu hiện tại; xem phạm vi TODO-9.1 | — |
| TODO-9.4 | Script compensation failure riêng được bỏ theo yêu cầu hiện tại; xem phạm vi TODO-9.1 | — |
| TODO-9.5 | Báo cáo/script đối chiếu riêng được bỏ theo yêu cầu hiện tại; xem phạm vi TODO-9.1 | — |
| TODO-10.1 | Dockerfile multi-stage (maven build → temurin 21 JRE) cho 4 service; `sqlserver` có healthcheck `mssql-tools18/bin/sqlcmd -C` | `6e2bc09` |
| TODO-10.2 | Service `db-init` tạo 3 database; các service `depends_on: service_completed_successfully` | `14b80f7`, `a542e44` |
| TODO-10.3 | Truyền biến môi trường: `DB_*`, `JWT_*`, `BRAND_SERVICE_URL`, `SAGA_FAULT_INJECTION` (mặc định `true`) | `1a9f136` |
