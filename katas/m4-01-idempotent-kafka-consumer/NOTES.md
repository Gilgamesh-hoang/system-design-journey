# M4-01: Idempotent Kafka Consumer + Dead-Letter Queue

> **Module:** M4 — Asynchronism & Message Queue
> **Concept:** At-least-once delivery → consumer phải idempotent; message độc → đẩy DLQ.
> **Time-box:** ~2–3 buổi.
> **Prereq:** Kafka cơ bản (topic, partition, consumer group, offset).

---

## 1. Vấn đề — vì sao có kata này
Kafka đảm bảo **at-least-once**, không phải exactly-once: consumer có thể nhận **trùng** message (rebalance, retry, commit offset lỗi). Nếu consumer không idempotent → xử lý 2 lần → trừ tiền 2 lần / gửi 2 notification. Và message **luôn lỗi** (poison pill) mà cứ retry sẽ **kẹt cả partition** → cần **DLQ**. "Exactly-once là ảo tưởng, thực tế là at-least-once + idempotent consumer" — câu này ghi điểm lớn khi phỏng vấn.

## 2. Mục tiêu (sau kata này tôi làm được gì)
Cài được **idempotent consumer** bằng dedup key, xử lý message trùng **không gây side-effect 2 lần**, và định tuyến message lỗi sang **DLQ** sau số lần retry giới hạn.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] Producer gửi event (vd `TripCompleted{tripId, ...}`), **cố ý gửi trùng** cùng 1 event nhiều lần.
- [ ] Consumer xử lý event; dùng **dedup key** (vd `eventId`/`tripId`) lưu Redis/DB để bỏ qua bản trùng.
- [ ] Message xử lý lỗi → **retry có giới hạn**; quá ngưỡng → publish sang **topic DLQ** kèm lý do.

**Ràng buộc / phi chức năng:**
- [ ] Side-effect (vd cộng điểm/ghi bảng) chỉ xảy ra **đúng 1 lần** dù nhận N lần.
- [ ] Poison message **không** chặn các message sau trong partition (đẩy DLQ rồi commit offset).
- [ ] Nêu rõ chiến lược commit offset (sau xử lý thành công) để tránh mất/nhân đôi.

**Stack đề xuất:** Spring Kafka + Redis (dedup store) + Docker Compose (Kafka).

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Test: gửi **cùng 1 event 5 lần** → side-effect chỉ 1 lần (đếm được).
- [ ] Test: 1 message luôn lỗi → sau K lần retry xuất hiện ở **topic DLQ**, consumer chính **không kẹt**.
- [ ] Giải thích được at-least-once vs at-most-once vs "exactly-once" (và vì sao cái cuối là ảo tưởng ở tầng messaging).

## 5. Gợi ý cách làm (không phải lời giải)
1. Dedup: trước khi xử lý, `SETNX processed:{eventId}` (TTL) — đã có → skip.
2. Đảm bảo **idempotent key** đi kèm message từ producer, đừng sinh ở consumer.
3. DLQ: dùng `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` (Spring Kafka) với số lần retry cố định.

## 6. Bẫy & Trade-off phải giải thích được
- **Dedup store lỗi/mất** → mất tính idempotent; chọn TTL đủ dài che retry window.
- Ordering chỉ đảm bảo **trong 1 partition** → chọn **partition key** hợp lý (vd theo `tripId`).
- Commit offset **trước** khi xử lý = at-most-once (mất message); **sau** = at-least-once (có thể trùng) → chọn sau + idempotent.
- Kafka vs RabbitMQ: Kafka mạnh throughput/replay; Rabbit mạnh routing linh hoạt & message riêng lẻ.

## 7. Câu hỏi phỏng vấn liên quan
- "Làm sao đảm bảo không xử lý trùng message?" (→ idempotent consumer + dedup key)
- "Exactly-once có thật không? Thực tế làm thế nào?"
- "DLQ giải quyết vấn đề gì?"
- "Kafka đảm bảo thứ tự thế nào? Khi consumer chậm hơn producer thì sao?" (back pressure, lag)

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả (side-effect count, DLQ):**
- **Bài học / điều bất ngờ:**
