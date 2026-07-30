# M1-01: Idempotency Key cho endpoint POST

> **Module:** M1 — Communication & API Design
> **Concept:** Chống tạo trùng resource khi client retry một POST không idempotent.
> **Time-box:** ~1–2 buổi (code có thể vứt, giữ lại NOTES).
> **Prereq:** Hiểu idempotency của HTTP method (GET/PUT idempotent, POST thì không).

---

## 1. Vấn đề — vì sao có kata này
POST không idempotent: mạng 3G chập chờn, client bấm "Đặt xe", request đi tới server và xử lý xong nhưng response rớt giữa đường → client tưởng lỗi → **retry** → tạo **2 chuyến xe / trừ tiền 2 lần**. Đây là bug tiền bạc kinh điển, và là "mỏ vàng" câu hỏi phỏng vấn M1.

## 2. Mục tiêu (sau kata này tôi làm được gì)
Thiết kế được cơ chế **idempotency key**: cùng một key gửi lại nhiều lần chỉ tạo resource **đúng 1 lần**, các lần sau trả lại **kết quả cũ** — và giải thích được vì sao method này cần key còn PUT thì không.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] Endpoint `POST /rides` nhận header `Idempotency-Key: <uuid>` do client sinh.
- [ ] Lần đầu với key mới → xử lý bình thường, tạo chuyến, **lưu (key → response + status code)**.
- [ ] Request lại **cùng key** → **không** tạo chuyến mới, trả lại đúng response + status code đã lưu.
- [ ] Thiếu header key → trả `400 Bad Request` (bắt buộc key cho endpoint tiền bạc).

**Ràng buộc / phi chức năng:**
- [ ] An toàn khi **2 request cùng key tới gần như đồng thời** (race) — không được tạo 2 chuyến. Dùng unique constraint trên cột key **hoặc** SETNX (Redis) làm lock nhận-chỗ.
- [ ] Key có **TTL** (vd 24h) — không giữ mãi.
- [ ] Phân biệt "cùng key, cùng body" (trả lại kết quả cũ) vs "cùng key, **khác** body" → trả `422`/`409` (client đang lạm dụng key).

**Stack đề xuất:** Spring Boot + Redis (`SETNX` + TTL) hoặc bảng Postgres có `UNIQUE(idempotency_key)`.

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Test: gọi `POST /rides` **10 lần cùng 1 key** → DB chỉ có **1** chuyến, cả 10 response giống hệt nhau.
- [ ] Test: 2 thread gọi song song cùng key → vẫn chỉ 1 chuyến (không có 2).
- [ ] Test: cùng key nhưng body khác → bị từ chối, không ghi đè kết quả cũ.
- [ ] Lưu lại: log/screenshot chứng minh chỉ 1 row được tạo sau nhiều lần retry.

## 5. Gợi ý cách làm (không phải lời giải)
1. Client sinh UUID **trước khi gửi** (không phải server sinh — nếu server sinh thì retry sẽ ra key khác).
2. Vào request: thử "nhận chỗ" key (INSERT unique / `SET key NX`). Thành công → là request đầu, xử lý & lưu kết quả. Thất bại → đã có → đọc & trả kết quả cũ.
3. Chú ý trạng thái **"đang xử lý"**: request đầu chưa xong mà request 2 đã tới → request 2 nên chờ/trả `409 Retry` chứ không được coi là "chưa có key".

## 6. Bẫy & Trade-off phải giải thích được
- **Ai sinh key?** Client, không phải server — nếu không retry là vô nghĩa.
- **Lưu kết quả ở đâu:** Redis (nhanh, TTL sẵn, mất mát chấp nhận được) vs DB (bền, cùng transaction với việc tạo chuyến → nhất quán hơn). Nêu được đánh đổi.
- **Cửa sổ race:** chỉ "check rồi mới insert" là **chưa đủ** — phải để DB/Redis đảm bảo atomic (unique constraint / SETNX), nếu không 2 request lọt qua check cùng lúc.
- Idempotency ≠ deduplication ở tầng message; đây là ở tầng API request.

## 7. Câu hỏi phỏng vấn liên quan
- "Làm sao đảm bảo API đặt xe không tạo 2 chuyến khi mạng chập chờn và client bấm lại?"
- "Vì sao PUT idempotent mà POST thì không? Khi nào bắt buộc dùng idempotency key?"
- "Xử lý race 2 request cùng key tới đồng thời thế nào?"
- "Lưu idempotency record trong Redis hay DB — đánh đổi gì?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:** Spring Boot (`POST /rides`) + Redis, SETNX (`opsForValue().setIfAbsent(key, value, ttl)`) làm cơ chế "nhận chỗ" atomic. Mỗi idempotency key map tới 1 Redis key (`idem:rides:<key>`, TTL 24h) chứa JSON `{state, bodyHash, status, body}`.
  - `begin(key, bodyHash)`: SETNX record `IN_PROGRESS`. Acquire được → request đầu, cho xử lý. Acquire thất bại → đọc record cũ: hash khác → 409 (key bị tái sử dụng với body khác); hash giống mà state `DONE` → replay đúng status/body cũ; state vẫn `IN_PROGRESS` → 409 "đang xử lý, thử lại sau" (đúng gợi ý #3 trong NOTES, không coi là "chưa có key").
  - `complete(key, bodyHash, status, body)`: ghi đè record thành `DONE` sau khi tạo ride xong.
  - Nếu logic tạo ride throw exception, key bị `release()` (xoá) ngay để request retry kế tiếp không phải chờ hết TTL 24h.
  - Thiếu header `Idempotency-Key` → 400 trước khi chạm Redis.
- **Kết quả đo / quan sát:** 4 test tích hợp (`IdempotencyIT`, dùng HTTP thật qua `RestTestClient` + Redis thật trong container Docker, không mock):
  1. `sameKeyTenTimes...`: POST cùng key 10 lần → đúng **1** ride trong `RideService`, cả 10 response giống hệt nhau (status + body).
  2. `concurrentRequestsSameKey...`: 8 thread bắn đồng thời cùng key (đồng bộ qua `CountDownLatch`) → sau khi tất cả xong, `RideService` tăng đúng **+1** ride — chứng minh SETNX chặn được race, không phải chỉ "check rồi insert".
  3. `sameKeyDifferentBody...`: cùng key, body khác → request thứ 2 nhận `409 Conflict`, không ghi đè kết quả cũ.
  4. `missingIdempotencyKey...`: thiếu header → `400 Bad Request`.
  - Cả 4 test pass (`./gradlew test`, xem `build/test-results/test/*.xml`, `tests="4" failures="0"`).
- **Bài học / điều bất ngờ:**
  - Chỉ "GET rồi mới SET" không đủ atomic — 2 request lọt qua "chưa thấy key" cùng lúc vẫn tạo trùng. Phải dùng lệnh atomic 1 bước của hạ tầng (SETNX/`SET NX EX` hoặc `UNIQUE` constraint), app code không tự đảm bảo được.
  - Trạng thái "đang xử lý" (`IN_PROGRESS`) là bẫy dễ bỏ sót: nếu coi "có key nhưng chưa DONE" như "chưa có key" thì vẫn tạo trùng khi request 2 tới trong lúc request 1 chưa kịp ghi kết quả.
  - Redis SETNX không tự transaction cùng việc tạo ride — nếu process chết giữa "acquire" và "complete", key kẹt ở `IN_PROGRESS` tới khi TTL hết hoặc tới khi có `release()` khi catch exception; đây là đánh đổi so với dùng DB (ghi idempotency record cùng transaction với insert ride thì nhất quán hơn nhưng chậm hơn/phức tạp hơn để scale).
  - Bất ngờ về tooling: Spring Boot 4 đổi Jackson sang groupId `tools.jackson` (import `tools.jackson.databind.ObjectMapper` thay vì `com.fasterxml.jackson.databind`), và `TestRestTemplate` bị thay bằng `org.springframework.test.web.servlet.client.RestTestClient` (kiểu builder giống `WebTestClient`). Testcontainers 1.21.3 (docker-java) không bắt tay được với Docker Desktop version rất mới trên máy — phải tự start container Redis qua `docker` CLI (`ProcessBuilder`) + `@DynamicPropertySource` thay vì `@Testcontainers`/`@ServiceConnection`.
