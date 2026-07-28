# Learning Log

Reverse-chronological log of theory-session takeaways (from Cowork/Chat). Written after each theory session — conclusions only, not the full conversation. `CLAUDE.md` points here so Claude Code has this context without needing the chat history.

## Format per entry

```
## YYYY-MM-DD — M<n>: <topic>
- Decision: <what was decided/understood>
- Trade-off: <the trade-off now understood>
- Next: <what to do next — kata, flagship step, or reading>
```

---

## 2026-07-25 — M0: 6-Step System Design Framework
- Decision: Áp dụng chuẩn khung 6 bước cho mọi bài System Design: (1) Requirements, (2) Estimation, (3) API, (4) DB Schema, (5) High-level Design, (6) Deep Dive & Bottleneck.
- Trade-off: Chốt cứng API/DB (bước 3, 4) trước khi vẽ High-level (bước 5) sẽ tốn thời gian đầu nhưng giảm rủi ro phải thiết kế lại kiến trúc ở giai đoạn sau.
- Next: Thực hành framework 6 bước này vào một bài toán cụ thể trong `design-docs/`.

## 2026-07-25 — M0: QPS & Roadmap Check
- Decision: Hiểu rõ khái niệm QPS (Read/Write) để đánh giá giới hạn công nghệ; Xác nhận roadmap tập trung sâu vào Sharding, Partitioning và Replication.
- Trade-off: Giữa hệ thống Read-heavy (ưu tiên Caching, Read Replicas) và Write-heavy (ưu tiên Message Queue, NoSQL/Cassandra).
- Next: Thực hành ước lượng tải (QPS, Storage) cho project RideNow (Kata 1).

## 2026-07-25 — M0: Khai phá CAP Theorem
- Decision: Hiểu rõ chữ P (Partition) là bắt buộc phải có vì mạng luôn có thể đứt. Chỉ được chọn CP (Nhất quán) hoặc AP (Sẵn sàng) lúc xảy ra sự cố mạng.
- Trade-off: Chọn CP (từ chối request để giữ đúng data - hợp với Ví tiền/Thanh toán) hay chọn AP (trả về data cũ/lệch để giữ hệ thống sống - hợp với GPS/Feed).
- Next: Vận dụng CP/AP vào thiết kế các hệ thống con trong dự án RideNow.

## 2026-07-25 — M0: Consistency Patterns
- Decision: Áp dụng Strong Consistency cho luồng nạp/rút tiền ví điện tử; Áp dụng Eventual Consistency cho luồng tracking vị trí GPS tài xế.
- Trade-off: Strong Consistency đảm bảo dữ liệu không bị sai lệch nhưng làm hệ thống chậm và dễ nghẽn (do phải lock). Eventual Consistency giúp hệ thống cực nhanh và scale tốt nhưng phải chấp nhận user thỉnh thoảng nhìn thấy dữ liệu trễ vài giây.
- Next: Tiếp tục luyện tập các khái niệm còn lại trong Module 0.

## 2026-07-25 — M0: Availability Patterns
- Decision: Hiểu rõ 4 mẫu thiết kế tăng tính sẵn sàng. (1) Cấp Server: Failover (Active-Passive rẻ nhưng phí, Active-Active khó nhưng tối ưu). (2) Cấp DB: Master-Slave phổ biến nhất. (3) Định lượng: Dùng hệ "số 9" (99.99% ~ 52 phút downtime/năm). (4) Cấu trúc: Luôn dùng kiến trúc song song (Load Balancer) thay vì nối tiếp để tăng SLA cấp số nhân.
- Trade-off: Master-Master có thể tăng Availability ghi nhưng đối diện rủi ro Conflict (đụng độ) cực lớn; Cố gắng chạy theo "5 số 9" tốn chi phí theo cấp số mũ mà không phải business nào cũng cần.
- Next: Hoàn thành nốt các kiến thức lý thuyết nền tảng (M0) và bước vào thực hành (Kata 1 / Flagship).

## 2026-07-25 — M0: Back-of-the-envelope Estimation
- Decision: Dùng quy tắc "làm tròn" (1 ngày ~ 100k giây) để tính nhẩm nhanh QPS, Storage, Bandwidth mà không cần máy tính. Peak QPS luôn lấy bằng 2-5 lần Average QPS.
- Trade-off: Tính nhẩm không cần chính xác tuyệt đối từng byte, quan trọng là ra được Order of Magnitude (hàng nghìn, hàng triệu hay hàng tỷ) để chốt kiến trúc: dùng 1 server hay sharding, dùng SQL hay Object Storage (như hình ảnh 1MB thì không lưu DB).
- Next: Thực hành tính tải (Kata) cho RideNow.

## 2026-07-25 — M0: Thực hành Kata 1 (Ước lượng tải RideNow)
- Decision: Tách hệ thống làm 2 luồng: Khách hàng (đặt xe) và Tài xế (GPS). Áp dụng tính nhẩm với 100k chuyến/ngày và 10k tài xế. 
- Trade-off: Nhận ra luồng Đặt xe RẤT NHẸ (chỉ ~25 QPS), trong khi luồng bắn GPS lại CỰC NẶNG (~5000 QPS) và tốn dung lượng khổng lồ (~7.3 TB/năm). Do đó, thiết kế hệ thống phải dồn toàn bộ nguồn lực Scale và NoSQL cho phần GPS, còn phần tính tiền, chuyến đi có thể cứ dùng DB SQL thông thường.
- Next: Chuẩn bị làm Flagship (1-page design doc).

## 2026-07-25 — M0: Thực hành Flagship (RideNow V1 1-Page Design Doc)
- Decision: Áp dụng toàn bộ lý thuyết CAP Theorem và Caching để phác thảo kiến trúc MVP. (1) Vị trí GPS chọn AP + Redis để chịu tải 5000 QPS. (2) Thanh toán chọn CP + MySQL để đảm bảo không sai lệch. (3) Cơ chế báo kết quả chọn Short-Polling cho dễ code ở giai đoạn V1 (đánh đổi bằng tốn connection).
- Tình trạng: Đã CHÍNH THỨC HOÀN THÀNH Module 0. Sẵn sàng bước sang Module 1: Communication & API Design.
- Next: Bắt đầu Module 1 (HTTP, REST, Idempotency).

## 2026-07-26 — M1: HTTP Method & Idempotency
- Decision: Hiểu sự khác biệt khi dùng POST vs PUT/PATCH cho tracking vị trí tài xế. Dùng POST nếu muốn lưu lịch sử toàn bộ hành trình (Event logging). Dùng PUT/PATCH nếu chỉ quan tâm tới cập nhật vị trí hiện tại mới nhất (State update) vì nó Idempotent (không bị ảnh hưởng khi client retry do rớt mạng).
- Trade-off: Lưu event (POST) tốn storage và phải tự xử lý duplicate data nếu mạng chập chờn. Cập nhật state (PUT) tiết kiệm tài nguyên và client không cần retry nhưng mất dữ liệu hành trình quá khứ (trong thực tế, hệ thống lớn dùng luồng stream riêng kết hợp Kafka).
- Next: Áp dụng hiểu biết vào thực hành (Kata idempotency key).

## 2026-07-26 — M1: Caching Overhead (Cache-Control & ETag)
- Decision: Hiểu được với những dữ liệu siêu nhỏ và thay đổi liên tục (như toạ độ tài xế update mỗi 3s), không nên lạm dụng `no-cache` kết hợp ETag. Thay vào đó, dùng `no-store` để lấy data mới hoàn toàn, hoặc bỏ qua HTTP REST mà chuyển sang WebSockets/gRPC.
- Trade-off: Dùng ETag để trả về 304 Not Modified giúp tiết kiệm bandwidth, nhưng với payload siêu nhỏ (vài bytes JSON), kích thước của HTTP Headers (chứa ETag) còn lớn hơn cả data. Việc Server phải mất công compute mã Hash mỗi 3s tạo ra chi phí tính toán (CPU overhead) vô ích thay vì đơn giản là gửi luôn data mới.
- Next: Bắt tay vào làm Kata (Idempotency Key) để kết thúc Module 1.

## 2026-07-26 — M1: TCP vs UDP (Khái niệm & Ứng dụng)
- Decision: Phân biệt TCP (đảm bảo độ tin cậy, có connection) và UDP (nhanh, không cần connection, fire-and-forget). Trong System Design, TCP là mặc định cho HTTP/REST/DB, còn UDP dùng cho Streaming, Gaming hoặc bắn log/metric tần suất cao.
- Trade-off: TCP tốn overhead (3-way handshake, header lớn 20 bytes) để đổi lấy sự toàn vẹn dữ liệu (không mất gói, đúng thứ tự). UDP có header cực nhỏ (8 bytes) và không tốn thời gian thiết lập kết nối, đổi lại dữ liệu có thể đến sai thứ tự hoặc mất luôn giữa đường mà không ai quan tâm (tuyệt vời cho bắn GPS mỗi giây vì mất 1 giây thì giây sau có toạ độ mới bù vào).
- Next: Vận dụng kiến thức giao thức vào thiết kế API thực tế.

## 2026-07-26 — M1: Idempotency Thực Chiến (Idempotency Key)
- Decision: Hiểu luồng hoạt động của Idempotency Key bằng Redis. Khi Client thực hiện POST (không idempotent), bắt buộc gửi kèm header `Idempotency-Key` (UUID). Backend dùng Redis để lock key này lại (`IN_PROGRESS`), xử lý logic, rồi lưu lại kết quả (`COMPLETED`). Nếu Client gọi lại đúng key đó, Server trả luôn kết quả cũ mà không chạy lại logic kinh doanh.
- Trade-off: Giúp giải quyết triệt để lỗi double-charge (trừ tiền 2 lần, tạo 2 cuốc xe) khi mạng chập chờn buộc client phải retry. Tuy nhiên, đổi lại Backend phải setup thêm Redis, tốn thêm 1 round-trip để check Redis trước khi đụng vào DB chính, và phải xử lý các edge cases (như payload khác nhau nhưng chung key).
- Next: Code thực hành Kata (Idempotency Key bằng Java/Spring Boot + Redis).

## 2026-07-26 — M1: gRPC vs GraphQL (và lỗi N+1)
- Decision: (1) gRPC dùng cho giao tiếp Server-to-Server nhờ chuẩn Protobuf (nhị phân siêu nhẹ) và HTTP/2 (tốc độ cao). (2) GraphQL dùng cho giao tiếp Client-to-Server để giải quyết triệt để over-fetching/under-fetching của REST, giúp Frontend chủ động chọn field.
- Trade-off: gRPC không thân thiện với Browser (khó debug bằng mắt). GraphQL thì dính lỗi chí mạng N+1 Query (1 query GraphQL gọi tới N query Database nếu thiết kế resolver ngây thơ), bắt buộc phải dùng công cụ như DataLoader để gom cụm (batch) truy vấn DB. GraphQL cũng làm cho việc Cache ở mức Network (CDN) trở nên vô dụng do mọi request đều dùng phương thức POST vào chung 1 URL.
- Next: Củng cố lý thuyết API và chuẩn bị làm phần thực hành cuối module.
