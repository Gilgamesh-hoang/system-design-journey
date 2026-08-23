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

## 2026-07-30 — M1-01 Kata: Code Idempotency Key (Spring Boot + Redis)
- Decision: Hiện thực `POST /rides` với `Idempotency-Key` bắt buộc trong `katas/m1-01-idempotency-key/idempotency`. Redis SETNX (`opsForValue().setIfAbsent`, atomic) làm "khoá nhận chỗ", lưu record `{state, bodyHash, status, body}` với TTL 24h. Phân biệt rõ 3 nhánh khi key đã tồn tại: hash khác body → `409`; state `IN_PROGRESS` → `409` (không coi là chưa có key); state `DONE` → replay lại đúng status/body cũ. Nếu logic tạo ride throw exception thì `release()` xoá key ngay để không phải chờ hết TTL mới retry được.
- Trade-off: Xác nhận lại bằng test thực chiến (không mock) — 8 thread bắn đồng thời cùng key vào Redis thật (chạy trong Docker container) vẫn chỉ tạo đúng 1 ride, chứng minh "check rồi mới set" là không đủ, phải là 1 lệnh atomic. Đánh đổi đã chọn: Redis (nhanh, có TTL sẵn) thay vì DB unique constraint (bền, cùng transaction với ride) — chấp nhận rủi ro nhỏ "kẹt IN_PROGRESS tới khi TTL hết" nếu process chết giữa chừng, đổi lại đơn giản hơn để scale.
- Note kỹ thuật bất ngờ: Spring Boot 4 đổi Jackson sang groupId `tools.jackson` (không còn `com.fasterxml.jackson.databind`), `TestRestTemplate` bị thay bằng `RestTestClient`. Testcontainers 1.21.3 không tương thích với Docker Desktop bản mới trên máy này (docker-java bắn 400) — phải tự start container Redis qua `docker` CLI thô (`ProcessBuilder`) + `@DynamicPropertySource`, `curl` thẳng vào socket vẫn chạy bình thường nên biết chắc lỗi nằm ở thư viện chứ không phải Docker.
- Next: Điền lại `NOTES.md` mục 8 (đã điền), có thể quay lại thử DB/Postgres UNIQUE constraint làm phiên bản đối chứng nếu muốn so sánh trade-off bằng tay thay vì chỉ nói suông.

## 2026-08-02 — M2-01 Kata: N+1 Query & Indexing (Spring Boot + Postgres)
- Decision: Hiện thực Kata M2-01 để hiểu sâu về 2 vấn đề lớn nhất của ORM/Database. Sử dụng lệnh `COPY` để nạp 2 triệu dòng cực nhanh thay vì Hibernate `save()`. Dùng `@EntityGraph` (tránh FetchType.EAGER global) để sửa dứt điểm N+1 query bằng LEFT JOIN. Sử dụng `EXPLAIN ANALYZE` để quan sát `Seq Scan` và `Bitmap Index Scan`.
- Trade-off: Dùng Index Scan giúp truy vấn cực nhanh nhưng nếu select ra quá nhiều dòng, việc "nhảy cóc" trên Heap Page đôi khi còn chậm hơn quét tuần tự (`Seq Scan`) toàn bộ Data Page, nên Index chỉ thực sự hiệu quả với dữ liệu có tính High Selectivity.
- Next: Bước sang Kata tiếp theo về Database Replication (M2-02) hoặc tiến lên xây dựng kiến trúc mới cho Flagship RideNow.

## 2026-08-04 — M2: Thực hành Kata M2-01
- Decision: Diệt N+1 Query bằng `@EntityGraph` và tối ưu quét bảng bằng Index trong Spring Boot + Postgres.
- Trade-off: Dùng `@EntityGraph` giúp giải quyết N+1 hiệu quả, nhưng nếu join quá nhiều bảng (Cartesian Product) có thể làm memory bloat. Index giúp Read nhanh hơn nhưng gây Write Amplification.
- Next: Học lý thuyết về Transactions & ACID.

## 2026-08-04 — M2: Isolation Levels & Lost Updates
- Decision: Hiểu rõ 3 mức cô lập (Read Committed, Snapshot Isolation, Serializability). Quyết định xử lý thao tác chuyển tiền (ví dụ: A trừ, B cộng) bằng `Atomic Update` (hoặc `SELECT FOR UPDATE`) ở mức `Read Committed` thay vì bật `Serializability`.
- Trade-off: Dùng Row-level lock (`Atomic Update`) giúp giải quyết hoàn hảo bài toán *Lost Update* trên các dòng rời rạc mà vẫn giữ được tốc độ cực cao, tránh thắt cổ chai so với 2-Phase Locking của Serializability. Chỉ dùng Serializability khi gặp *Write Skew* (đọc A làm tiền đề để ghi B).
- Next: Thực hành giải quyết các bài toán Concurrency / hoặc đi tiếp lý thuyết Replication & Partitioning.

## 2026-08-08 — M3: Partitioning & Sharding (Range vs Hash)
- Decision: Dùng Range Partitioning cho các bài toán lấy dữ liệu theo dải thời gian liên tục nhưng cẩn thận Hot Spot. Dùng Hash Partitioning để dàn đều tải nhưng chấp nhận mất khả năng Range Scan.
- Trade-off: (1) Tránh Hot Spot bằng Salting ở tầng App giúp ghi nhanh nhưng đọc chậm. (2) Tránh chia dư cho số Node (`hash % N`) vì khi scale mạng sẽ sập; thay vào đó định nghĩa số lượng Phân vùng cố định (Fixed Partitions) để di chuyển dữ liệu ở mức tối thiểu.
- Next: Thực hành cấu hình Rebalancing hoặc đi tiếp lý thuyết Chặng 4 (Hệ thống phân tán & CAP).

## 2026-08-09 � M4: Unreliable Networks, Clocks & Process Pauses
- Decision: Hi?u b?n ch?t c�c r?i ro h? th?ng ph�n t�n. M?ng truy?n tin kh�ng c� d? tr? gi?i h?n (Unbounded delays) d?n d?n Timeout kh�ng lu?ng tru?c (k�ch ho?t False Positive). �?ng h? v?t l� l?ch nh?p l�m sai l?ch gi?i thu?t LWW (Last-Write-Wins) g�y m?t d? li?u. Ti?n tr�nh b? d�ng bang (GC Pause) cu?p CPU l�m d? li?u b? ghi d� khi t?nh d?y.
- Trade-off: (1) Thay v� d�ng Timeout c?ng, d�ng Phi Accrual Failure Detector h?c theo bi?n d?ng c?a m?ng. (2) Thay v� tin d?ng h? NTP, d�ng Google TrueTime (Kho?ng th?i gian) ho?c Logical Clocks. (3) D�ng Fencing Tokens (tang d?n don di?u) ch?n Zombie Process ghi file b?y b? thay v� ch? d?a v�o Lease/Lock th�ng thu?ng.
- Next: �i ti?p l� thuy?t CAP Theorem ho?c th?c h�nh x? l� l?i m?ng/kh�a.

## 2026-08-09 � M4: Th?c h�nh Kata M4-02 (Fencing Tokens)
- Decision: D�ng bi?n AtomicLong sinh Token don di?u trong Lock Service v� bi?n luu Token cao nh?t t?i Storage Service d? block c�c Zombie Client.
- Trade-off: Storage b?t bu?c ph?i "th�ng minh" v� tham gia v�o qu� tr�nh check Token (Active Validation) thay v� ch? nh?m m?t ghi data. �i?u n�y d�i h?i Storage Service (v� d? HDFS, S3, ho?c DB) ph?i h? tr? truy?n v� ki?m tra fencing token, ho?c ph?i nh�ng token v�o t�n file. N?u d�ng DB, c� th? d�ng c�u l?nh UPDATE ... WHERE token < :newToken.
- Next: Ti?p t?c l� thuy?t CAP Theorem.

## 2026-08-09 � M4: L� thuy?t CAP Theorem (S? th?t v? CAP)
- Decision: D?p b? d?nh nghia cu "ch?n 2 trong 3". Hi?u d�ng d?nh l� CAP: Partition (P) l� quy lu?t v?t l� b?t bu?c ph?i x?y ra (d?t c�p, r?t m?ng, treo switch). Khi x?y ra P, h? th?ng ch? du?c ch?n C (d?ng ph?c v? d? d?m b?o nh?t qu�n) ho?c A (ti?p t?c ph?c v? nhung ch?p nh?n d? li?u sai/cu).
- Trade-off: Trong th?c t?, l� do ch�nh m� c�c h? th?ng d�nh d?i C (Linearizability) kh�ng ph?i l� v� s? P (d?t m?ng), m� l� v� t?c d? (Latency). Thu?t to�n d?m b?o t�nh C lu�n lu�n ch?m. Vi?c l?a ch?n AP (nhu Cassandra) b?n ch?t l� d�nh d?i s? nh?t qu�n tuy?t d?i d? l?y t?c d? v�i mili-gi�y.
- Next: K?t th�c Ch?ng 4. Chu?n b? b�i Test.

## 2026-08-09 � M4: Consensus, ZooKeeper & Raft
- Decision: Hi?u s�u co ch? �?ng thu?n. Thay v� Polling, d�ng Watch Mechanism c?a ZK d? nh?n Push Notification. Thay v� ZAB ph?c t?p c?a ZK, c�c h? th?ng m?i nhu etcd/Kafka uu ti�n Raft v� n� chia t�ch r� r�ng vi?c B?u Leader (Election) v� Sao ch�p d? li?u (Log Replication), s? d?ng Randomized Timeouts d? b?u nhanh.
- Trade-off: Thu?t to�n d?ng thu?n d?m b?o an to�n (Total Order Broadcast) nhung Write c?c k? d?t d? v� ph?i d?i Quorum (N/2 + 1). C�c h? th?ng n�y lu�n c?n s? Node l?. Ch? n�n d�ng ZK/etcd d? luu Metadata nh? (file c?u h�nh, service discovery, lock), tuy?t d?i kh�ng d�ng d? luu User Data l?n.
- Next: K?t th�c tr?n v?n Ch?ng 4 (Chuong 8 & 9 DDIA).

## 2026-08-09 - M3: B-Tree vs LSM-Tree Trade-offs
- Decision: Hiểu sâu sự khác biệt giữa hai cấu trúc dữ liệu lưu trữ phổ biến. B-Tree tối ưu cho Read bằng Random I/O nhưng chịu Write Amplification lớn (phải ghi lại toàn bộ Page 8KB dù chỉ sửa 1 byte). LSM-Tree tối ưu cho Write bằng Sequential I/O (MemTable -> SSTable) nhưng chịu Read Amplification.
- Trade-off: LSM-Tree đánh đổi bằng việc bắt CPU/Disk hoạt động liên tục ở background để Compaction (gom SSTable) nhằm dọn rác và giảm Read Amplification. Để khắc phục tốc độ đọc, LSM-Tree phải sử dụng Bloom Filter trên RAM để chặn việc đọc nhầm file.
- Next: Tiếp tục lý thuyết Hệ quản trị CSDL hoặc thực hành.

## 2026-08-22 - M5: Thực hành Kata Master-Slave Replication Lag
- Decision: Thiết lập cụm PostgreSQL Primary (Port 5432) và Replica (Port 5433) qua Docker Compose. Sử dụng cấu hình `recovery_min_apply_delay` để tạo độ trễ nhân tạo 5 giây (Replication Lag) để quan sát hiện tượng Data Inconsistency tạm thời.
- Trade-off: Asynchronous Replication giúp ghi cực nhanh (Primary không cần đợi Replica) và tăng tính High Availability (đọc trên nhiều Replica). Đổi lại, hệ thống phải đối mặt với Stale Read (đọc dữ liệu cũ). Để khắc phục ở tầng Application, có thể áp dụng chiến lược Read-Your-Own-Writes (đọc lại từ Primary trong vài giây đầu sau khi User vừa ghi).
- Next: Thực hành các kỹ thuật Sharding hoặc phân tích sâu hơn về HA.
