# Vì sao dùng local transaction và Saga?

<!-- [TODO-4.2] Giải thích transaction boundary, 2PC và giới hạn Saga của mã nguồn. -->

`MSWatch` sở hữu DBWatch; `MSBrand` sở hữu DBBrand. Khi tạo watch, hệ thống phải INSERT `Watches` trong DBWatch và gán `BrandWatches` trong DBBrand. Lời gọi Feign là HTTP, không tham gia transaction JDBC/JPA đang chạy trong MSWatch.

## Transaction boundary trong code

| Thành phần | Transaction | Tác dụng |
|---|---|---|
| `WatchSagaOrchestrator` | Không có `@Transactional` | Tạo kế hoạch CREATE/UPDATE/DELETE và snapshot |
| `SagaExecutor` | Không có `@Transactional` | Chạy từng action, lưu stack đã hoàn tất, xử lý pivot/lỗi |
| `WatchLocalService` | Mỗi hàm ghi là một local transaction | Insert, update/restore, delete/deleteIfExists trong DBWatch |
| `BrandService.assignWatch/unassignWatch` | Mỗi hàm là một local transaction | Upsert/gỡ liên kết trong DBBrand; gọi lặp lại an toàn |
| `SagaTracker` | Mọi hàm ghi dùng `REQUIRES_NEW` | Commit trạng thái và log độc lập, giữ bằng chứng khi nghiệp vụ lỗi |
| `SagaCompensator` | Điều phối các local transaction riêng | Bù trừ LIFO, tối đa 3 lần mỗi bước; thất bại thì dừng |

Ví dụ CREATE với brand 999: local insert trong DBWatch đã commit; MSBrand trả 404; `SagaCompensator` gọi `WatchLocalService.deleteIfExists` bằng một transaction mới. Watch đã thực sự tồn tại rồi bị xóa; WatchID có thể nhảy cóc vì IDENTITY không được trả lại.

Nếu bọc orchestrator bằng `@Transactional`, transaction DBWatch sẽ bị kéo dài qua lời gọi mạng. Local write có thể nhập vào transaction ngoài, bị rollback khi lỗi, trong khi DBBrand đã commit độc lập. Cách đó không tạo atomicity trên hai database. Nhật ký `SagaTracker` vẫn có thể tồn tại do `REQUIRES_NEW`, nhưng việc bù trừ và transaction ngoài sẽ xung đột với mục tiêu mỗi bước commit độc lập.

## Vì sao không chọn 2PC cho bài này?

2PC cần coordinator và mọi tài nguyên cùng hỗ trợ transaction phân tán: prepare trước, rồi commit/rollback theo quyết định chung. REST/Feign hiện tại không có cơ chế enlist XA như vậy. Triển khai thêm coordinator, participant protocol và quản lý khóa sẽ tăng phụ thuộc giữa service, đồng thời giữ tài nguyên trong lúc chờ mạng hoặc phục hồi coordinator.

Saga cho phép mỗi service commit ngắn trên database của mình. Khi bước sau lỗi, ứng dụng gửi thao tác nghiệp vụ đảo ngược. Đổi lại, hệ thống chỉ hướng tới nhất quán cuối cùng khi các bước hoặc compensation hoàn tất; không có isolation xuyên service. Trong khi Saga đang chạy, một request đọc có thể thấy watch đã ghi nhưng link brand chưa cập nhật.

## Sáu trạng thái

```text
PENDING → EXECUTING → COMPLETED
              └→ FAILED → COMPENSATING → COMPENSATED
                               └→ compensation lỗi: giữ COMPENSATING
```

| Trạng thái | Ý nghĩa |
|---|---|
| `PENDING` | Đã lưu yêu cầu Saga và payload, chưa chạy chuỗi bước |
| `EXECUTING` | Đang thực hiện các local transaction |
| `COMPLETED` | Chuỗi thao tác đã hoàn tất theo chiều tiến |
| `FAILED` | Đã ghi nhận lỗi nghiệp vụ/kỹ thuật |
| `COMPENSATING` | Đang bù trừ, hoặc đã dừng sau khi retry hết |
| `COMPENSATED` | Mọi compensation được đăng ký đã trả về thành công |

`DELETE_WATCH` là pivot cuối Saga DELETE. Sau khi xóa cứng commit, không gán lại brand hoặc tạo lại watch với ID khác. Fault injection sau pivot được ghi `SKIPPED` và Saga đi tới `COMPLETED`.

## Giới hạn cần giải thích khi vấn đáp

Snapshot `WatchData` được ghi vào `SagaStates.Payload`: CREATE có `after`, UPDATE có `before` và `after`, DELETE có `before`. Snapshot hỗ trợ điều tra và khôi phục; chưa có scheduler đọc payload để resume sau process crash. Stack thực thi vẫn nằm trong JVM, nên persisted state/log không tự biến engine thành durable workflow.

Đặc biệt, nếu MSBrand commit nhưng response bị mất/read-timeout, action phía MSWatch ném lỗi trước khi compensation của bước đó được push. Mã hiện tại chưa xử lý đầy đủ trường hợp không biết remote đã commit hay chưa. `COMPENSATED` chỉ nói rằng stack đã đăng ký được xử lý, không chứng minh mọi remote effect đã được đảo ngược. Cần đối soát dữ liệu; một hướng cải tiến là đăng ký thao tác bù idempotent trước remote call, kết hợp durable command/inbox/outbox và recovery. Các cơ chế đó chưa được triển khai trong bài.

Quy trình phục hồi thủ công được mô tả tại [README](../README.md#giới-hạn-và-khôi-phục-thủ-công).
