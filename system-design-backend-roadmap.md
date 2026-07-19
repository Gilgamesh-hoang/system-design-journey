# Lộ trình System Design cho Backend Java (Junior → Middle) — bám sát roadmap, hiểu sâu để phỏng vấn

> **Đối tượng:** Backend Java/Spring Boot, ~1 năm kinh nghiệm junior.
> **Mục tiêu:** Hiểu **sâu** và **làm được** (không học cho biết) → đủ sức trả lời phỏng vấn System Design và làm task khó ở mức middle.
> **Cường độ:** ~6–8 giờ/tuần, timeline ~9–12 tháng.
> **Môi trường thực hành:** Local + Docker Compose trước, cloud để sau khi nền đã vững.
> **Nguyên tắc:** Bám sát System Design roadmap bạn gửi, nhưng **lọc theo mức độ cốt lõi & phổ biến 2025+**. Mỗi mục ghi rõ: học gì (cơ bản → nâng cao), lưu ý/trade-off, câu hỏi phỏng vấn hay gặp, và phần thực hành (kata riêng hoặc nâng cấp flagship project).

---

## 0. Cách đọc lộ trình này

Mỗi module có 5 phần cố định:

- **Học gì** — kiến thức cần nắm, đi từ cơ bản đến nâng cao.
- **Lưu ý / trade-off** — chỗ dễ hiểu sai, và các đánh đổi mà middle engineer phải giải thích được.
- **Câu hỏi phỏng vấn hay gặp** — để bạn học có đích, tự kiểm tra "mình có trả lời được không".
- **Thực hành** — kata nhỏ (bài tập tách biệt) và/hoặc nâng cấp **flagship project**.
- **Xong module khi** — tiêu chí tự đánh giá đã "làm được", không phải "đọc xong".

**Ba kiểu thực hành** dùng xuyên suốt (đã bàn ở phần trước):
1. **Kata** — bài nhỏ tập trung đúng 1 cơ chế (vd: tự viết rate limiter), làm 1–2 buổi, xong có thể vứt.
2. **Case study trên giấy** — vẽ diagram + viết doc trade-off cho các hệ thống lớn, KHÔNG code full. Đây là dạng sát phỏng vấn nhất.
3. **Flagship project** — 1 project chính xây mới, tiến hoá dần qua từng module (chi tiết ở mục 1).

---

## 1. Flagship Project: **RideNow — Hệ thống gọi xe real-time (kiểu Grab/Uber)**

### Vì sao chọn project này cho bạn

Bạn đã làm nhiều về **thương mại điện tử, bán hàng, quản lý task/project (kiểu Jira), quản lý nhân sự / leave request** — tất cả đều là "business CRUD app". RideNow được chọn có chủ đích vì nó **hoàn toàn khác domain** và ép bạn chạm vào những thứ mà app quản lý thông thường không có:

- **Real-time & geospatial** (định vị, tìm tài xế gần nhất) — domain hoàn toàn mới với bạn, tạo sự hứng thú.
- **Matching bất đồng bộ** (ghép tài xế ↔ khách) — bài toán phân tán thực sự, không phải CRUD.
- **"Design Uber / ride-hailing" là một trong những câu hỏi phỏng vấn System Design kinh điển bậc cao** — làm project này = luyện thẳng vào đề phỏng vấn.
- Có thể tiến hoá dần để **chạm gần như toàn bộ roadmap**: caching, sharding, message queue, resilience, observability, CQRS...
- Có phần "nhìn thấy được" (bản đồ, xe di chuyển) → làm xong mỗi giai đoạn thấy thành quả ngay, đỡ nản.

> Nếu sau này thấy hợp hơn, có thể "đổi áo" cùng bộ khung này thành **giao đồ ăn / giao bưu kiện real-time** — kiến trúc gần như y hệt (matching shipper thay vì tài xế). Nhưng khuyên giữ nguyên ride-hailing để bám sát đề phỏng vấn.
>
> *Lựa chọn nhẹ hơn nếu bạn muốn thiên về game/real-time thuần:* một **nền tảng quiz/trắc nghiệm nhiều người chơi real-time (kiểu Kahoot)** — vui, mạnh về WebSocket + leaderboard, nhưng phủ ít khái niệm DB/sharding hơn. Mình vẫn khuyên RideNow vì độ phủ rộng hơn hẳn.

### Bản đồ tiến hoá của RideNow (mỗi module bồi thêm 1 lớp)

| Giai đoạn | Nâng cấp RideNow | Khái niệm roadmap được thực hành |
|-----------|------------------|----------------------------------|
| M0–M1 | Monolith Spring Boot: đăng ký tài xế/khách, tạo yêu cầu đi xe, REST API chuẩn, idempotency cho "đặt xe" | REST, idempotency, HTTP, transaction cơ bản |
| M2 | PostgreSQL nghiêm túc: index, transaction, tối ưu query; thêm lịch sử chuyến đi | Indexing, ACID, SQL tuning, N+1 |
| M3 | Redis: lưu vị trí tài xế (GEO), cache thông tin tài xế/giá; tìm tài xế gần nhất | Cache-aside, Redis geospatial, cache pitfalls |
| M4 | Kafka: matching engine bất đồng bộ, sự kiện vòng đời chuyến đi, thông báo; outbox pattern | Message queue, event-driven, competing consumers, outbox, back pressure |
| M5 | Tách microservice (trip, driver-location, pricing, notification); nginx load balancer; API gateway | Microservices, LB, reverse proxy, service discovery, API gateway |
| M6 | Resilience: circuit breaker cho payment, rate limiting API, retry, surge pricing; saga cho thanh toán | Circuit breaker, retry, throttling, bulkhead, saga |
| M7 | Observability: Prometheus + Grafana, distributed tracing (OpenTelemetry), structured logging | Monitoring, tracing, alerting, performance antipatterns |
| M8 | CQRS + materialized view cho lịch sử/analytics chuyến đi; sharding driver-location theo vùng | CQRS, event sourcing (nhẹ), sharding, denormalization |
| M9 (tùy) | Deploy lên cloud free tier, health check, auto-restart | Cloud cơ bản, deployment, health endpoint |

**Quy tắc vàng cho flagship:** làm đúng chuẩn ngay từ đầu — có Git flow tử tế, có test, có README ghi lại **quyết định thiết kế và trade-off** (chính là "chất liệu" bạn kể khi phỏng vấn). Đừng chờ "sau này refactor".

---

## MODULE 0 — Nền tảng tư duy System Design *(≈2 tuần)*

Đây là phần **phải học kỹ nhất** vì nó là ngôn ngữ chung của mọi buổi phỏng vấn. Thiếu nền này, các module sau chỉ là học vẹt.

### Học gì
- **System Design là gì, quy trình tiếp cận một bài** (requirements → ước lượng tải/dung lượng → high-level design → deep dive → bottleneck). Học thuộc *khung 6 bước* để áp vào mọi câu hỏi phỏng vấn.
- **Các cặp đánh đổi cốt lõi:** Performance vs Scalability, Latency vs Throughput, Availability vs Consistency.
- **CAP theorem** — hiểu thật, không thuộc lòng: khi có network partition, chọn CP hay AP; ví dụ hệ thống nào chọn gì và **tại sao**.
- **Consistency patterns:** weak / eventual / strong — mỗi loại dùng ở đâu (vd: eventual cho vị trí tài xế, strong cho số dư ví).
- **Availability patterns:** failover (active-active vs active-passive), replication (master-slave vs master-master), "availability in numbers" (99.9% = ba số 9 → bao nhiêu phút downtime/năm), availability nối tiếp vs song song.
- **Ước lượng (back-of-envelope):** QPS, dung lượng lưu trữ, băng thông — kỹ năng bắt buộc trong phỏng vấn.

### Lưu ý / trade-off
- CAP thường bị hiểu sai: "C" ở đây là **linearizability**, không phải ACID consistency. Và CAP chỉ nói về **lúc có partition** — lúc bình thường bạn vẫn có cả C lẫn A.
- "Availability 5 số 9" nghe oai nhưng **cực đắt**; middle engineer phải biết khi nào *không cần* mức đó.
- Strong consistency luôn đánh đổi bằng latency/availability — đây là câu phỏng vấn rất hay hỏi.

### Câu hỏi phỏng vấn hay gặp
- "Giải thích CAP theorem. Hệ thống X của bạn chọn CP hay AP, vì sao?"
- "Eventual consistency là gì? Cho ví dụ khi nó chấp nhận được và khi nó KHÔNG chấp nhận được."
- "99.99% availability nghĩa là downtime tối đa bao nhiêu mỗi tháng?"
- "Khác nhau giữa latency và throughput? Tối ưu cái này có hại cái kia không?"

### Thực hành
- **Kata:** viết một trang tính (hoặc file md) tự ước lượng tải cho RideNow: giả sử 100k chuyến/ngày → QPS trung bình & đỉnh, dung lượng lưu 1 năm lịch sử chuyến. Làm quen với con số.
- **Flagship:** viết **1-page design doc** cho RideNow phiên bản đầu: yêu cầu chức năng/phi chức năng, high-level diagram, chọn C hay A cho từng thành phần (vị trí tài xế vs ví tiền) và giải thích.

### Xong module khi
Bạn có thể cầm một đề bất kỳ ("design Twitter") và tự đi qua 6 bước, ước lượng được QPS/storage, và nói được từng phần chọn consistency/availability ra sao.

📚 *Tài nguyên:* "System Design Primer" (GitHub, đây chính là nguồn của roadmap bạn gửi) phần Introduction; kênh YouTube ByteByteGo cho CAP & ước lượng.

---

## MODULE 1 — Communication & API Design *(≈2 tuần)*

### Học gì
- **HTTP sâu hơn:** status code đúng ngữ nghĩa, method (GET/POST/PUT/PATCH/DELETE), **idempotency** của từng method, header quan trọng (Cache-Control, ETag), HTTP/1.1 vs HTTP/2 (multiplexing).
- **TCP vs UDP** (chỉ cần mức khái niệm: khi nào real-time chấp nhận mất gói → UDP).
- **REST chuẩn:** thiết kế resource, versioning, phân trang, lọc, chuẩn lỗi; **OpenAPI/Swagger spec** đầy đủ.
- **Idempotency thực chiến** — RẤT quan trọng 2025: idempotency key cho request "đặt xe"/"thanh toán" để tránh double-charge khi client retry.
- **gRPC** (biết): khi nào dùng cho giao tiếp nội bộ microservice (nhị phân, nhanh, có schema); **GraphQL** (biết): giải quyết over/under-fetching, khi nào KHÔNG nên dùng.

### Lưu ý / trade-off
- PUT idempotent, POST thì không → đó là lý do cần **idempotency key**. Đây là mỏ vàng câu hỏi phỏng vấn.
- REST vs gRPC vs GraphQL: đừng học để "chọn cái xịn nhất" mà để **biết đánh đổi** (REST đơn giản/phổ biến; gRPC nhanh nhưng khó debug, cần proto; GraphQL linh hoạt nhưng phức tạp caching & N+1).

### Câu hỏi phỏng vấn hay gặp
- "Làm sao đảm bảo API đặt xe không tạo 2 chuyến khi mạng chập chờn và client bấm lại?" (→ idempotency key)
- "Khi nào chọn gRPC thay vì REST?"
- "GraphQL giải quyết vấn đề gì, và tạo ra vấn đề gì mới?"

### Thực hành
- **Kata:** implement **idempotency key** cho một endpoint POST (lưu key + kết quả vào Redis/DB, request trùng key trả lại kết quả cũ).
- **Flagship:** dựng REST API chuẩn cho RideNow (đăng ký, đặt xe), có Swagger spec, có idempotency cho "đặt xe". Viết cả API bằng chuẩn lỗi thống nhất.

### Xong module khi
API của bạn có Swagger, xử lý idempotency đúng, và bạn giải thích được vì sao từng endpoint dùng method & status code đó.

---

## MODULE 2 — Databases (phần NẶNG & QUAN TRỌNG NHẤT) *(≈4–5 tuần)*

> Gần như mọi câu hỏi System Design đều quy về database. Đây là module đầu tư nhiều thời gian nhất.

### Học gì (cơ bản → nâng cao)
**Cơ bản (nền phải chắc):**
- **Transaction & ACID**, isolation levels (read committed, repeatable read, serializable) và các hiện tượng (dirty/non-repeatable/phantom read).
- **Index**: B-Tree hoạt động thế nào, composite index, covering index, khi nào index *không* được dùng; đọc `EXPLAIN`/`EXPLAIN ANALYZE`.
- **N+1 problem** trong JPA/Hibernate (bạn đang dùng) — cách phát hiện & xử lý (fetch join, `@EntityGraph`, batch size).
- **SQL tuning**: viết query hiệu quả, tránh SELECT *, pagination bằng keyset thay vì OFFSET lớn.

**Nâng cao (chất liệu phỏng vấn):**
- **Replication**: master-slave (read replica), master-master; độ trễ replication & đọc dữ liệu cũ (read-your-write problem).
- **Sharding**: theo range / hash / directory; **consistent hashing**; hot partition; khó khăn khi join/transaction xuyên shard.
- **Federation** (chia DB theo chức năng), **Denormalization** (đánh đổi đọc nhanh ↔ ghi phức tạp/ dữ liệu trùng).
- **SQL vs NoSQL** và 4 họ NoSQL: key-value (Redis/DynamoDB), document (MongoDB), wide-column (Cassandra), graph (Neo4j) — mỗi loại hợp bài toán nào.
- **CAP áp vào chọn DB** thực tế.

### Lưu ý / trade-off
- **Đừng shard sớm.** Middle engineer biết sharding là "giải pháp cuối"; trước đó là index, read replica, caching, vertical scaling. Câu hỏi phỏng vấn cài bẫy: "khi nào bạn shard?" — trả lời "khi đã hết các cách rẻ hơn".
- Chọn **shard key** sai → hot partition & không thể re-balance. Đây là điểm ăn/trượt trong phỏng vấn.
- NoSQL không "hiện đại hơn" SQL — nó chỉ đánh đổi khác. Biết **khi nào SQL vẫn là lựa chọn đúng** thể hiện độ chín.

### Câu hỏi phỏng vấn hay gặp
- "Query này chậm, bạn debug thế nào?" (→ EXPLAIN, index, N+1)
- "Phân biệt sharding, replication, partitioning, federation."
- "Consistent hashing giải quyết vấn đề gì so với hash % N?"
- "Khi nào dùng NoSQL thay vì SQL? Cho ví dụ chọn Cassandra vs MongoDB."
- "Read replica gây ra vấn đề gì về consistency?"

### Thực hành
- **Kata 1:** dựng **MySQL/Postgres master-slave replication bằng Docker Compose**, ghi vào master, đọc từ slave, rồi cố tình tạo độ trễ và quan sát đọc dữ liệu cũ.
- **Kata 2:** tự viết **consistent hashing** (vòng hash + virtual nodes) bằng Java, test phân bố key khi thêm/bớt node.
- **Kata 3:** tạo bảng vài triệu dòng, cố ý gây N+1 & full scan, rồi dùng `EXPLAIN` + index để tối ưu, đo thời gian trước/sau.
- **Flagship:** chuyển RideNow sang Postgres nghiêm túc — thiết kế schema có index hợp lý, transaction cho tạo chuyến, thêm bảng lịch sử chuyến; đo & sửa vài query chậm.

### Xong module khi
Bạn đọc được EXPLAIN, sửa được N+1 trong Hibernate, dựng được replication, tự cài consistent hashing, và giải thích được lý do chọn shard key.

📚 *Tài nguyên:* "Designing Data-Intensive Applications" (Martin Kleppmann) — chương 5–7 là kinh thánh phần này (đọc dần, không cần hết ngay); "Use The Index, Luke" cho indexing.

---

## MODULE 3 — Caching *(≈2 tuần)*

### Học gì
- **Chiến lược caching:** cache-aside (phổ biến nhất), read-through, write-through, write-behind (write-back), refresh-ahead — hiểu **khi nào dùng cái nào** và rủi ro mỗi loại.
- **Các loại cache** (client, CDN, web server, application, database) — nhưng **tập trung application cache & Redis**.
- **Vấn đề cache thực chiến:** cache invalidation, TTL, stale data, **cache stampede / thundering herd**, cache penetration, hot key.
- **Redis nâng cao:** cấu trúc dữ liệu (string, hash, sorted set, set), Redis làm cache vs làm store, eviction policy (LRU/LFU).

### Lưu ý / trade-off
- "There are only two hard things: cache invalidation and naming things" — invalidation là chỗ sinh bug tinh vi. Middle phải nói được **chiến lược invalidation** chứ không chỉ "nhét Redis vào".
- Write-through an toàn nhưng chậm ghi; write-behind nhanh nhưng có nguy cơ mất dữ liệu khi cache chết → biết đánh đổi.
- **CDN caching**: chỉ cần biết khái niệm (dùng cho static asset, cache tĩnh ở edge). *Không đào sâu* — ít liên quan công việc backend hằng ngày của bạn (bạn cũng đã nói không cần). Biết "khi nào cần CDN" là đủ cho phỏng vấn.

### Câu hỏi phỏng vấn hay gặp
- "Cache-aside vs write-through khác nhau ra sao? Bạn chọn cái nào cho hệ thống của bạn?"
- "Cache stampede là gì, chống thế nào?" (→ lock, TTL jitter, request coalescing)
- "Làm sao invalidate cache khi dữ liệu gốc thay đổi?"

### Thực hành
- **Kata 1:** tự viết **LRU cache** bằng Java (HashMap + doubly linked list) — bài phỏng vấn coding rất hay ra.
- **Kata 2:** mô phỏng **cache stampede**: nhiều thread cùng miss 1 key hết hạn, rồi sửa bằng lock/coalescing, đo số lần gọi DB trước/sau.
- **Flagship:** thêm Redis vào RideNow — cache-aside cho thông tin tài xế & bảng giá; dùng **Redis GEO** (`GEOADD`/`GEOSEARCH`) lưu vị trí tài xế và tìm tài xế gần nhất. Đặt TTL + chiến lược invalidation hợp lý.

### Xong module khi
Bạn tự cài được LRU, xử lý được stampede, và giải thích được chiến lược cache + invalidation cho RideNow.

---

## MODULE 4 — Asynchronism & Message Queue *(≈3 tuần)*

### Học gì
- **Vì sao async:** tách coupling, chịu tải đỉnh (queue-based load leveling), xử lý việc nặng ngoài luồng request.
- **Message queue vs task queue vs pub/sub**; **Kafka** (bạn đã đụng) đào sâu: partition, consumer group, offset, ordering trong partition, at-least-once vs at-most-once vs exactly-once.
- **Competing consumers**, **back pressure**, **dead-letter queue (DLQ)**, retry & idempotent consumer.
- **Outbox pattern** (2025 rất phổ biến): đảm bảo ghi DB và publish event nhất quán (tránh mất/nhân đôi event).
- **Async request-reply**, **priority queue** (biết).

### Lưu ý / trade-off
- "Exactly-once" gần như là **ảo tưởng** ở tầng messaging — thực tế là "at-least-once + idempotent consumer". Nói được điều này = ghi điểm lớn.
- Kafka vs RabbitMQ: Kafka mạnh cho streaming/throughput cao & replay; RabbitMQ mạnh cho routing linh hoạt & message riêng lẻ. Biết đánh đổi, đừng chỉ "vì Kafka phổ biến".
- Ordering chỉ đảm bảo **trong 1 partition** → chọn key partition quan trọng.

### Câu hỏi phỏng vấn hay gặp
- "Làm sao đảm bảo không xử lý trùng message?" (→ idempotent consumer + dedup key)
- "Outbox pattern giải quyết vấn đề gì?" (→ dual write problem)
- "Kafka đảm bảo thứ tự message thế nào?"
- "Khi consumer chậm hơn producer thì sao?" (→ back pressure, lag, scale consumer)

### Thực hành
- **Kata:** producer/consumer Kafka đơn giản, cố ý gửi trùng, rồi làm **idempotent consumer** bằng dedup key trong Redis; thêm DLQ cho message lỗi.
- **Flagship:** biến **matching tài xế** thành bất đồng bộ qua Kafka — request đặt xe đẩy vào topic, matching engine (consumer) tìm tài xế & phát event `TRIP_MATCHED`; các event vòng đời chuyến (`REQUESTED`→`MATCHED`→`STARTED`→`COMPLETED`) chạy qua queue; áp **outbox pattern** cho việc tạo chuyến + publish event.

### Xong module khi
Bạn giải thích được at-least-once + idempotency, cài được outbox & DLQ, và RideNow xử lý matching qua Kafka ổn định khi có retry.

---

## MODULE 5 — Scalability & Kiến trúc dịch vụ *(≈3 tuần)*

### Học gì
- **Vertical vs horizontal scaling**; **stateless service** (điều kiện để scale ngang).
- **Load balancer**: LB vs reverse proxy, thuật toán (round-robin, least-connections, hash), **L4 vs L7**, health check; sticky session và vì sao nên tránh.
- **Reverse proxy / API Gateway**: gateway routing, aggregation, offloading (auth, rate limit, TLS ở gateway); **BFF (Backend for Frontend)**.
- **Microservices vs Monolith**: khi nào tách, chi phí của phân tán (network, data consistency, vận hành); **service discovery**; giới thiệu **service mesh** (chỉ biết khái niệm).
- **Containerization**: Docker thành thạo hơn; **Kubernetes cơ bản** (pod, deployment, service, scaling) — mức khái niệm, vì thiên về DevOps.

### Lưu ý / trade-off
- **Đừng tách microservice sớm.** "Monolith-first" là quan điểm chủ đạo 2025 cho team nhỏ; tách khi có lý do rõ ràng (scale/tổ chức). Câu này phỏng vấn rất thích hỏi và cài bẫy "microservice cho ngầu".
- Microservice **không** giảm độ phức tạp — nó **dời** độ phức tạp từ code sang vận hành & mạng.
- Service stateful thì không scale ngang được — biết đẩy state ra ngoài (DB/Redis).

### Câu hỏi phỏng vấn hay gặp
- "Monolith vs microservices — bạn chọn gì cho startup 5 người? Vì sao?"
- "L4 và L7 load balancing khác nhau chỗ nào?"
- "API Gateway làm những việc gì?"
- "Làm sao để service scale ngang được?" (→ stateless)

### Thực hành
- **Kata:** chạy 3 instance của 1 service Spring Boot sau **nginx** làm load balancer, bật health check, kill 1 instance và quan sát nginx định tuyến lại.
- **Flagship:** tách RideNow thành vài service (trip, driver-location, pricing, notification), đặt sau **API Gateway** (Spring Cloud Gateway) xử lý auth + rate limit; đảm bảo các service **stateless**.

### Xong module khi
Bạn dựng được LB + nhiều instance, tách được service với gateway, và tranh luận được monolith vs microservice bằng trade-off cụ thể.

---

## MODULE 6 — Resilience & Reliability *(≈3 tuần)*

> Phần này phân biệt rõ "code chạy được ở local" với "hệ thống sống sót ở production".

### Học gì
- **Timeout & retry** (retry với exponential backoff + jitter; **retry storm** và cách tránh).
- **Circuit breaker** (đóng/mở/half-open) — chống lỗi lan truyền khi 1 service phụ thuộc chết.
- **Rate limiting / throttling**: token bucket, leaky bucket, sliding window; rate limit phân tán bằng Redis.
- **Bulkhead** (cô lập tài nguyên), **graceful degradation**, **fallback**.
- **Health endpoint monitoring**, **leader election** (biết, vd chọn node chạy job định kỳ).
- **Distributed transaction**: vì sao 2PC khó, và **Saga pattern** (choreography vs orchestration), **compensating transaction**.
- **Thư viện:** Resilience4j (chuẩn cho Spring hiện nay).

### Lưu ý / trade-off
- Retry **không có backoff/jitter** = tự gây DDoS chính mình (retry storm). Đây là bug thật rất hay gặp.
- Circuit breaker & retry phải đi cùng nhau đúng cách (retry *bên trong* breaker mở = vô nghĩa).
- Saga đánh đổi: được availability & không khoá phân tán, nhưng **chỉ eventual consistency** và phải viết logic bù trừ (compensation) — phức tạp. Biết khi nào chấp nhận.

### Câu hỏi phỏng vấn hay gặp
- "Service B bạn gọi bị chậm/chết, làm sao service A không sập theo?" (→ timeout + circuit breaker + fallback + bulkhead)
- "Thiết kế rate limiter cho API." (câu **cực kỳ hay ra** — chuẩn bị kỹ)
- "Xử lý transaction xuyên nhiều service thế nào?" (→ Saga, tại sao không 2PC)
- "Exponential backoff với jitter để làm gì?"

### Thực hành
- **Kata 1:** tự viết **rate limiter token bucket & sliding window** bằng Java + Redis (phân tán) — luyện thẳng đề phỏng vấn.
- **Kata 2:** dùng **Resilience4j** bọc một lời gọi service hay lỗi: circuit breaker + retry + fallback; mô phỏng service chết và quan sát breaker mở.
- **Flagship:** thêm rate limiting ở gateway RideNow; circuit breaker cho service pricing/payment; surge pricing; áp **Saga** cho luồng thanh toán chuyến (trừ ví → nếu fail thì hoàn tác).

### Xong module khi
Bạn tự cài được rate limiter phân tán, dùng thành thạo Resilience4j, và thiết kế được luồng Saga cho thanh toán RideNow.

---

## MODULE 7 — Observability & Performance Antipatterns *(≈2 tuần)*

> Observability là kỹ năng **đang rất "hot" 2025** và là thứ AI khó thay bạn: đọc tín hiệu hệ thống để tìm nguyên nhân gốc.

### Học gì
- **3 trụ cột observability:** metrics, logs, traces.
- **Metrics + Prometheus + Grafana**: expose metrics từ Spring Boot (Micrometer/Actuator), vẽ dashboard, đặt **alert**.
- **Structured logging** (JSON), correlation/trace id để nối log across service.
- **Distributed tracing** với **OpenTelemetry** — theo dấu 1 request đi qua nhiều service.
- **Các loại monitoring** (health, availability, performance, security, usage).
- **Performance antipatterns** (từ roadmap): busy database, chatty I/O, extraneous fetching, N+1, no caching, synchronous I/O, noisy neighbor, retry storm — nhận diện & cách sửa.

### Lưu ý / trade-off
- Log nhiều ≠ observability. Middle biết log **có cấu trúc + trace id**, và biết metric nào đáng cảnh báo (tránh alert fatigue).
- "Chatty I/O" và "extraneous fetching" chính là biến thể của N+1 bạn đã gặp ở Module 2 — nối lại kiến thức.

### Câu hỏi phỏng vấn hay gặp
- "Production chậm/hay lỗi, bạn điều tra từ đâu?" (→ metrics → traces → logs)
- "Phân biệt metrics, logs, traces."
- "Distributed tracing giúp gì trong microservices?"

### Thực hành
- **Kata:** thêm Actuator + Micrometer vào 1 service, scrape bằng Prometheus, dựng dashboard Grafana (latency p95/p99, error rate, QPS), đặt 1 alert.
- **Flagship:** gắn observability cho RideNow — dashboard cho tỉ lệ match thành công, latency matching, lag Kafka; thêm trace id xuyên các service bằng OpenTelemetry; cố ý tạo 1 antipattern (vd chatty I/O) rồi phát hiện qua trace và sửa.

### Xong module khi
Bạn có dashboard + tracing chạy thật trên RideNow, và trình bày được quy trình debug production theo 3 trụ cột.

---

## MODULE 8 — Advanced Patterns & Ghép nối *(≈3 tuần)*

> Chọn lọc các Cloud Design Pattern **đáng học nhất**; phần còn lại chỉ đọc overview (xem mục "Bỏ qua có chủ đích").

### Học gì
- **CQRS** — tách đường ghi và đường đọc; khi nào đáng dùng (đọc/ghi lệch tải, view phức tạp) và khi nào là over-engineering.
- **Event Sourcing** (biết + demo nhẹ) — lưu chuỗi event thay vì state; ưu/nhược; thường đi cùng CQRS.
- **Materialized View / Index Table** — dựng sẵn view cho truy vấn đọc nhanh.
- **Strangler Fig** — chiến lược migrate dần monolith → microservice (rất thực tế khi đi làm).
- **Sidecar, Ambassador, Anti-Corruption Layer, Gateway aggregation** (biết khái niệm & khi nào dùng).
- **Ghép nối:** tổng duyệt lại toàn hệ thống RideNow như một bài System Design hoàn chỉnh.

### Lưu ý / trade-off
- CQRS & Event Sourcing **rất dễ bị lạm dụng**. Điểm chín của middle là biết nói "chỗ này KHÔNG cần CQRS". Phỏng vấn đánh giá cao sự tiết chế này.
- Strangler Fig là câu trả lời "người lớn" cho "làm sao bỏ monolith mà không viết lại từ đầu".

### Câu hỏi phỏng vấn hay gặp
- "CQRS là gì? Khi nào bạn KHÔNG dùng nó?"
- "Event sourcing đánh đổi gì?"
- "Migrate monolith sang microservices mà không downtime — chiến lược?" (→ Strangler Fig)

### Thực hành
- **Kata:** dựng CQRS nhỏ — 1 command service ghi event, 1 read model (materialized view) cập nhật từ event, phục vụ truy vấn đọc.
- **Flagship:** áp **CQRS + materialized view** cho **lịch sử & thống kê chuyến đi** của RideNow (đường ghi qua Kafka event, đường đọc là view tối ưu); shard `driver-location` theo vùng địa lý.

### Xong module khi
Bạn cài được CQRS/read-model, giải thích được khi nào **không** nên dùng, và trình bày trọn vẹn kiến trúc RideNow như một bài phỏng vấn.

---

## MODULE 9 (tùy chọn) — Cloud & Deployment cơ bản *(≈2 tuần)*

Chỉ làm sau khi nền local đã vững. Không cần sâu ở giai đoạn này.

### Học gì
- Khái niệm cloud lõi (chọn AWS hoặc GCP): compute (EC2/ECS hoặc Cloud Run), managed DB (RDS), object storage (S3/GCS), managed cache, load balancer managed.
- Deploy RideNow (hoặc 1 service) lên **free tier**; health check; container registry; biến môi trường/secret cơ bản.
- Khái niệm CI/CD tối thiểu (GitHub Actions build + test + deploy).

### Thực hành
- **Flagship:** đóng gói 1–2 service RideNow bằng Docker, deploy lên free tier, cấu hình health endpoint để tự restart khi chết.

---

## 2. Bỏ qua có chủ đích (và vì sao) — để không phí thời gian

Roadmap gốc rất rộng; đây là những mục **không cần đào sâu** cho mục tiêu backend middle 2025+ của bạn. Chỉ cần **biết tên + 1 câu nó là gì** để không bị "đứng hình" khi phỏng vấn nhắc tới:

- **CDN chi tiết (push vs pull CDN, CDN caching)** — bạn đã nói không cần; thiên về frontend/static. Biết "CDN để phục vụ nội dung tĩnh gần người dùng" là đủ.
- **Nhiều Cloud Design Pattern ngách:** Sequential Convoy, Scheduling Agent Supervisor, Claim Check, Valet Key, Compute Resource Consolidation, Geodes, Deployment Stamps, Gatekeeper, Static Content Hosting... — chỉ đọc lướt 1 lần cho biết, **không thực hành**.
- **SOAP** — công nghệ cũ, gần như không gặp ở dự án mới.
- **Federated Identity/SAML sâu** — biết khái niệm SSO/OAuth2/OIDC là đủ (OAuth2 bạn đã có nền).
- **Service Mesh (Istio) sâu, Kubernetes vận hành sâu** — thiên DevOps/platform; middle backend chỉ cần mức khái niệm + dùng được, chưa cần tự vận hành cluster.
- **UDP/mạng tầng thấp sâu** — biết khác biệt TCP/UDP là đủ.

> Nguyên tắc: những thứ trên **không phải vô dụng**, chỉ là **ROI thấp** cho giai đoạn của bạn. Khi công việc thực tế cần, học sau vẫn kịp.

---

## 3. Luyện phỏng vấn System Design (làm song song từ Module 4 trở đi)

Ngoài flagship, mỗi 2 tuần luyện **1 case study trên giấy** (chỉ diagram + trade-off, không code). Danh sách kinh điển 2025 nên luyện, xếp từ dễ → khó:

1. **URL Shortener / Pastebin** — hashing, KV store, read-heavy caching. *(khởi động)*
2. **Rate Limiter** — token bucket, phân tán bằng Redis. *(bạn đã code ở M6)*
3. **Notification Service** — fan-out, queue, retry, template.
4. **News Feed (Twitter/Instagram)** — fan-out on write vs on read, cache.
5. **Chat/WhatsApp** — WebSocket, message ordering, presence, delivery.
6. **Ride-hailing/Uber** — geospatial, matching, real-time. *(chính là flagship của bạn → lợi thế lớn)*
7. **Ticket Booking / Flash Sale** — concurrency, distributed lock, oversell.
8. **Distributed Message Queue (Kafka)** — bản chất của công cụ bạn đang dùng.

Với mỗi case, ép mình theo khung Module 0: requirements → ước lượng → high-level → deep dive 1–2 thành phần → bottleneck & scale.

---

## 4. Học & làm việc cùng AI coding agent (kỹ năng middle 2025+)

AI (Claude, Copilot, Cursor...) giờ sinh code CRUD/boilerplate rất nhanh. Điều đó **không làm System Design bớt quan trọng — mà ngược lại**:

- **Dùng AI để tăng tốc phần "viết", giữ cho mình phần "thiết kế & kiểm chứng".** Ví dụ: nhờ AI sinh boilerplate CRUD, test, docs, script Docker Compose — nhưng **bạn** quyết định kiến trúc, shard key, chiến lược cache.
- **Luôn review code AI sinh ra bằng con mắt System Design:** nó có gây N+1 không? có thiếu index không? có idempotent không? có xử lý lỗi/timeout/retry đúng không? — Đây chính là lúc kiến thức các module trên biến thành giá trị.
- **Dùng AI như bạn học:** nhờ giải thích trade-off, cho phản biện thiết kế của bạn ("phản biện giúp tôi vì sao KHÔNG nên sharding ở đây"), đóng vai người phỏng vấn System Design để bạn luyện nói.
- **Ranh giới:** AI viết hộ, nhưng **bạn chịu trách nhiệm cuối cùng** về đúng/sai, hiệu năng, bảo mật. Người không hiểu hệ thống sẽ không phát hiện được code AI "chạy đúng nhưng sai kiến trúc".

---

## 5. Checklist "đã sẵn sàng ở mức Middle"

Đánh dấu khi bạn **làm được**, không phải khi "đã đọc":

- [ ] Cầm 1 đề System Design lạ và tự đi hết khung 6 bước + ước lượng tải.
- [ ] Đọc `EXPLAIN`, sửa N+1, thiết kế index & chọn shard key có lý do.
- [ ] Tự cài: LRU cache, rate limiter phân tán, consistent hashing, idempotent consumer.
- [ ] Giải thích rành: CAP, eventual consistency, at-least-once + idempotency, cache-aside vs write-through.
- [ ] Dùng được Resilience4j (circuit breaker + retry + fallback) và thiết kế được Saga.
- [ ] Có dashboard + tracing chạy thật, và trình bày được quy trình debug production.
- [ ] Nói được **khi nào KHÔNG dùng** microservices / CQRS / sharding (dấu hiệu của độ chín).
- [ ] Trình bày trọn vẹn RideNow như một bài phỏng vấn, kèm trade-off từng quyết định.

---

## 6. Lịch tổng quan (≈9–12 tháng, 6–8h/tuần)

| Tháng | Module | Trọng tâm |
|-------|--------|-----------|
| 1 | M0 + M1 | Nền tảng tư duy + API/idempotency |
| 2–3 | M2 | Database (nặng nhất) |
| 3–4 | M3 + M4 | Caching + Messaging/Kafka |
| 5–6 | M5 | Scalability & microservices |
| 6–7 | M6 | Resilience & reliability |
| 8 | M7 | Observability |
| 9 | M8 | Advanced patterns + ghép nối |
| 10 | M9 + luyện phỏng vấn dồn | Cloud nhẹ + mock interview |
| 11–12 | Đệm | Bù phần chậm, luyện phỏng vấn, hoàn thiện RideNow |

> Timeline là hướng dẫn, không phải deadline. Module 2 (Database) đáng để chậm lại; đừng vội qua. Quan trọng là **làm được**, không phải chạy cho kịp lịch.

---

*Ghi chú nguồn: phần curate "topic cốt lõi / phổ biến 2025+" và danh sách case study phỏng vấn tham khảo từ các tổng hợp System Design interview 2025 và roadmap.sh (chi tiết ở phần trả lời kèm file). Cấu trúc bám theo System Design roadmap bạn cung cấp.*
