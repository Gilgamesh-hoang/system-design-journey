# M6-01: Rate Limiter — Token Bucket & Sliding Window (Redis, phân tán)

> **Module:** M6 — Resilience & Reliability
> **Concept:** Giới hạn tần suất request; phân tán qua Redis để đúng trên nhiều instance.
> **Time-box:** ~2 buổi.
> **Prereq:** Redis cơ bản; hiểu vì sao cần throttling.

---

## 1. Vấn đề — vì sao có kata này
"**Thiết kế rate limiter**" là câu phỏng vấn **cực kỳ hay ra**. Rate limit bảo vệ hệ thống khỏi abuse/quá tải. Điểm khó: khi có **nhiều instance** sau LB, mỗi instance đếm riêng thì tổng vượt hạn mức → phải đếm **tập trung ở Redis** và thao tác phải **atomic** (không race).

## 2. Mục tiêu (sau kata này tôi làm được gì)
Tự cài **token bucket** và **sliding window** trên Redis (atomic bằng Lua/INCR+EXPIRE), so sánh 2 thuật toán, và giải thích vì sao fixed-window có lỗi "burst ở ranh giới cửa sổ".

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] Giới hạn theo key (vd `userId`/`ip`): cho phép **R request / T giây**.
- [ ] Cài **token bucket** (refill theo thời gian, cho phép burst tới capacity).
- [ ] Cài **sliding window** (log hoặc counter) để so sánh.
- [ ] Trả `429 Too Many Requests` + header `Retry-After` / `X-RateLimit-Remaining` khi vượt.

**Ràng buộc / phi chức năng:**
- [ ] Đúng khi chạy **nhiều instance** → đếm tập trung ở Redis, thao tác **atomic** (Lua script hoặc `INCR`+`EXPIRE` cẩn thận) để không race.
- [ ] Không rò rỉ key: window/bucket có TTL tự dọn.

**Stack đề xuất:** Spring Boot + Redis (Lua script). Bonus: dùng làm filter ở gateway.

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Test: bắn R+X request trong T giây → đúng R cái `200`, X cái `429`.
- [ ] Test chạy **2 instance** cùng lúc → tổng vẫn bị chặn ở R (không phải 2R).
- [ ] Chứng minh lỗi **fixed-window boundary burst** (2R request quanh ranh giới) và sliding window sửa được.
- [ ] Giải thích token bucket cho phép burst còn leaky bucket làm mượt (smoothing) khác nhau ra sao.

## 5. Gợi ý cách làm (không phải lời giải)
1. Token bucket trong Redis: lưu `tokens` + `lastRefill`, tính refill theo `now`. **Gộp read-modify-write vào 1 Lua script** để atomic.
2. Sliding window counter: 2 cửa sổ kề nhau + nội suy trọng số theo thời gian đã trôi.
3. Bắn tải bằng `hey`/script đa luồng để thấy 429 xuất hiện đúng ngưỡng.

## 6. Bẫy & Trade-off phải giải thích được
- **Fixed window** đơn giản nhưng cho burst gấp đôi ở ranh giới; **sliding window log** chính xác nhưng tốn bộ nhớ; **sliding window counter** cân bằng.
- **Token bucket** (cho burst) vs **leaky bucket** (đầu ra đều, smoothing) — dùng cảnh nào.
- Vì sao phải atomic: 2 request đọc cùng `tokens` rồi cùng trừ → lọt hạn mức (race) → cần Lua.
- Rate limit ở đâu: gateway (chặn sớm, đỡ tải service) vs từng service.

## 7. Câu hỏi phỏng vấn liên quan
- "Thiết kế rate limiter cho API." (chuẩn bị kỹ — hay ra)
- "Token bucket vs leaky bucket vs sliding window — chọn cái nào, vì sao?"
- "Rate limit phân tán trên nhiều instance làm thế nào cho đúng?"
- "Fixed window có lỗi gì ở ranh giới cửa sổ?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả (số 200/429, hành vi multi-instance):**
- **Bài học / điều bất ngờ:**
