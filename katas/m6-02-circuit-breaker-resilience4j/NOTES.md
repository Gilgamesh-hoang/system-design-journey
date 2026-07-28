# M6-02: Circuit Breaker + Retry + Fallback (Resilience4j)

> **Module:** M6 — Resilience & Reliability
> **Concept:** Chống lỗi lan truyền khi service phụ thuộc chết; retry/fallback đúng cách.
> **Time-box:** ~1–2 buổi.
> **Prereq:** Spring Boot; hiểu timeout, dependency call.

---

## 1. Vấn đề — vì sao có kata này
Service A gọi service B. B chậm/chết → A **chờ timeout hàng loạt** → thread pool A cạn → **A sập theo** (cascading failure). **Circuit breaker** phát hiện B hỏng và "mở mạch" (fail fast) để A sống sót. Nhưng retry sai (không backoff, retry trong breaker mở) lại **tự gây DDoS** — phải cấu hình đúng.

## 2. Mục tiêu (sau kata này tôi làm được gì)
Dùng thành thạo **Resilience4j**: circuit breaker (closed → open → half-open) + retry (exponential backoff + jitter) + fallback; mô phỏng B chết và **quan sát breaker mở**, giải thích thứ tự các decorator.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] 1 "downstream" giả có thể bật/tắt lỗi (slow / 500) theo ý.
- [ ] Bọc lời gọi bằng **CircuitBreaker** + **Retry** + **Fallback** của Resilience4j.
- [ ] Khi tỉ lệ lỗi vượt ngưỡng → breaker **OPEN** → request fail nhanh + trả fallback (không gọi downstream).
- [ ] Sau `waitDuration` → **HALF_OPEN** thử vài request → thành công thì **CLOSED** lại.

**Ràng buộc / phi chức năng:**
- [ ] Retry có **exponential backoff + jitter**, số lần giới hạn (tránh retry storm).
- [ ] Có **timeout** cho lời gọi (TimeLimiter) — không chờ vô hạn.
- [ ] Thứ tự decorator đúng: Retry **bọc ngoài** CircuitBreaker (không retry khi breaker đang mở một cách vô nghĩa) — thử và giải thích.

**Stack đề xuất:** Spring Boot + `resilience4j-spring-boot`. Bonus: expose state qua Actuator.

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Log/metric cho thấy chuyển trạng thái **CLOSED → OPEN → HALF_OPEN → CLOSED**.
- [ ] Khi downstream chết: request trả **fallback nhanh** (không kẹt timeout), latency thấp hẳn so với không có breaker.
- [ ] Đo được retry **không** tạo bão request khi có backoff (so với retry ngay lập tức).
- [ ] Giải thích được khác biệt khi đặt Retry trong vs ngoài CircuitBreaker.

## 5. Gợi ý cách làm (không phải lời giải)
1. Cấu hình `failureRateThreshold`, `slidingWindowSize`, `waitDurationInOpenState`, `permittedNumberOfCallsInHalfOpenState`.
2. Toggle lỗi downstream giữa chừng và bắn tải để ép breaker mở.
3. Bật event listener của Resilience4j để log mỗi lần đổi trạng thái.

## 6. Bẫy & Trade-off phải giải thích được
- **Retry storm**: retry không backoff/jitter = tự DDoS chính mình. Backoff + jitter là bắt buộc.
- **Retry bên trong breaker đang OPEN = vô nghĩa** → thứ tự decorator quan trọng.
- **Bulkhead**: cô lập thread pool cho từng dependency để 1 cái chết không nuốt hết tài nguyên (nhắc kèm).
- Chỉ retry với lỗi **transient/idempotent** — retry lệnh trừ tiền không idempotent = double charge (nối lại M1).

## 7. Câu hỏi phỏng vấn liên quan
- "Service B bạn gọi bị chậm/chết, làm sao A không sập theo?" (→ timeout + breaker + fallback + bulkhead)
- "Circuit breaker có mấy trạng thái, chuyển đổi thế nào?"
- "Exponential backoff + jitter để làm gì? Retry storm là gì?"
- "Khi nào KHÔNG nên retry?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả (chuyển trạng thái, latency có/không breaker):**
- **Bài học / điều bất ngờ:**
