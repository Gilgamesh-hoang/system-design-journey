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
- **Approach thực tế:**
- **Kết quả đo / quan sát:**
- **Bài học / điều bất ngờ:**
