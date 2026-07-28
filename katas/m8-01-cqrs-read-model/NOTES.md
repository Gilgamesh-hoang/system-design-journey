# M8-01: CQRS + Materialized Read Model

> **Module:** M8 — Advanced Patterns
> **Concept:** Tách đường ghi (command) khỏi đường đọc (read model dựng sẵn từ event).
> **Time-box:** ~2–3 buổi.
> **Prereq:** M4 (event/Kafka); hiểu eventual consistency.

---

## 1. Vấn đề — vì sao có kata này
Khi **đọc và ghi lệch tải** hoặc view đọc **phức tạp** (join nhiều bảng, thống kê), ép cùng 1 model phục vụ cả hai → chậm. **CQRS** tách: đường ghi lưu event, đường đọc là **materialized view** cập nhật từ event, tối ưu riêng cho truy vấn. Điểm chín của middle là biết **khi nào KHÔNG cần CQRS** (nó rất dễ bị lạm dụng).

## 2. Mục tiêu (sau kata này tôi làm được gì)
Dựng CQRS nhỏ: command service ghi event → read model (materialized view) cập nhật từ event → phục vụ query đọc nhanh; giải thích được đánh đổi (eventual consistency, phức tạp vận hành) và **khi nào không nên dùng**.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] **Command side**: nhận lệnh (vd `CompleteTrip`), ghi vào write store, phát **event** (`TripCompleted`).
- [ ] **Read side**: consumer nhận event, cập nhật **read model** (bảng/materialized view tối ưu cho đọc — vd `driver_daily_stats`: số chuyến, doanh thu theo tài xế/ngày).
- [ ] **Query API** đọc thẳng từ read model (không join write side).

**Ràng buộc / phi chức năng:**
- [ ] Read model **eventual consistency** — chấp nhận trễ; đo/nêu độ trễ command→query thấy được.
- [ ] Cập nhật read model **idempotent** (event trùng không cộng đôi — nối lại M4).
- [ ] Nêu rõ ranh giới: phần nào cần **strong** (số dư ví) thì **không** đẩy qua đường đọc eventual này.

**Stack đề xuất:** Spring Boot + Kafka (event) + Postgres (write store + read model riêng). Có thể nhẹ: 2 bảng khác nhau trong cùng DB.

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Ghi lệnh → sau độ trễ ngắn, query read model phản ánh đúng (vd stats cộng dồn đúng).
- [ ] Gửi event **trùng** → read model **không** cộng nhầm (idempotent) — có test.
- [ ] Đo được **cửa sổ eventual consistency** (ms giữa command và lúc read model cập nhật).
- [ ] Viết được **1 đoạn "khi nào KHÔNG dùng CQRS"** cho chính RideNow (vd CRUD tài xế đơn giản → thừa).

## 5. Gợi ý cách làm (không phải lời giải)
1. Read model là **denormalized**: dựng sẵn đúng hình dạng query cần, chấp nhận trùng lặp dữ liệu.
2. Idempotent update: dựa trên `eventId`/`(driverId, day)` upsert, không "cộng thêm" mù quáng.
3. Nếu read model lệch → có thể **rebuild** từ chuỗi event (chạm nhẹ event sourcing).

## 6. Bẫy & Trade-off phải giải thích được
- CQRS đổi lấy hiệu năng đọc bằng **eventual consistency + phức tạp vận hành** (2 store, sync qua event). Không phải free.
- **Lạm dụng CQRS/Event Sourcing** là lỗi phổ biến — biết tiết chế = dấu hiệu chín.
- Materialized view vs query trực tiếp: view nhanh nhưng phải maintain + có thể stale.
- Denormalization đánh đổi: đọc nhanh ↔ ghi phức tạp + dữ liệu trùng.

## 7. Câu hỏi phỏng vấn liên quan
- "CQRS là gì? Khi nào bạn KHÔNG dùng nó?"
- "Event sourcing đánh đổi gì? Có bắt buộc đi cùng CQRS không?"
- "Read model bị lệch/sai thì khôi phục thế nào?" (→ rebuild từ event)
- "Materialized view giải quyết và tạo ra vấn đề gì?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả (độ trễ eventual, idempotency):**
- **Bài học / điều bất ngờ:**
