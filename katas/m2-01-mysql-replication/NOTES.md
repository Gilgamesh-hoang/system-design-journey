# M2-01: Master-Slave Replication + Read Replica (Docker Compose)

> **Module:** M2 — Databases
> **Concept:** Tách đọc/ghi qua read replica; quan sát replication lag & read-your-write.
> **Time-box:** ~2 buổi.
> **Prereq:** Hiểu transaction cơ bản; biết dùng Docker Compose.

---

## 1. Vấn đề — vì sao có kata này
Read-heavy system: 1 master ghi không kịp phục vụ hàng nghìn read. Giải pháp rẻ **trước khi sharding** là **read replica**: ghi vào master, đọc từ slave. Nhưng replica **trễ** vài chục ms → user vừa ghi xong đọc lại **không thấy** (read-your-write problem). Phải tự tay dựng và **nhìn thấy** cái lag này thì mới trả lời phỏng vấn được.

## 2. Mục tiêu (sau kata này tôi làm được gì)
Dựng được cụm **1 master + 1 slave** bằng Docker Compose, định tuyến write→master / read→slave, **cố ý tạo lag** và quan sát đọc dữ liệu cũ; giải thích được replication đồng bộ vs bất đồng bộ.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] `docker-compose.yml` khởi động 1 Postgres/MySQL **primary** + 1 **replica**, replica tự stream từ primary.
- [ ] Chứng minh: `INSERT` vào primary → sau đó `SELECT` trên replica thấy dữ liệu.
- [ ] Một app/script nhỏ định tuyến: mọi `INSERT/UPDATE` → primary; mọi `SELECT` → replica.

**Ràng buộc / phi chức năng:**
- [ ] Đo được **replication lag** (ms) — vd query `pg_stat_replication` / `SHOW SLAVE STATUS`.
- [ ] Tạo được kịch bản **stale read**: ghi vào master rồi đọc ngay ở slave và **bắt gặp** lúc chưa có dữ liệu.

**Stack đề xuất:** Postgres streaming replication (hoặc MySQL binlog) qua Docker Compose. App = script psql/Java tuỳ ý.

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] `docker compose up` → cụm chạy, replica ở chế độ read-only.
- [ ] Demo write→master, read→slave hoạt động.
- [ ] Bắt được **ít nhất 1 lần** stale read (write xong đọc slave chưa thấy) — lưu log/screenshot.
- [ ] Ghi lại con số lag quan sát được, và mô tả 1 cách khắc phục read-your-write.

## 5. Gợi ý cách làm (không phải lời giải)
1. Cấu hình primary bật WAL/binlog + tạo replication user; replica dùng `pg_basebackup`/`CHANGE MASTER`.
2. Muốn **phóng đại lag** để dễ quan sát: thêm `recovery_min_apply_delay` (Postgres) hoặc nhét tải ghi lớn vào master.
3. Kịch bản stale read: trong 1 vòng lặp, ghi 1 row rồi lập tức đọc trên replica; in ra "có/chưa có".

## 6. Bẫy & Trade-off phải giải thích được
- **Async replication** (mặc định): master không chờ slave → nhanh, nhưng slave trễ → stale read + rủi ro mất data nếu master chết trước khi replicate. **Sync**: an toàn hơn, chậm hơn.
- **Read-your-write fix:** đọc từ master cho user vừa ghi (sticky), hoặc chờ tới khi replica bắt kịp LSN.
- Replica **không** giúp scale ghi — chỉ scale đọc. Đây là bẫy phỏng vấn hay gặp.
- Read replica là bước **trước** sharding trong thang giải pháp.

## 7. Câu hỏi phỏng vấn liên quan
- "Read replica gây ra vấn đề gì về consistency? Read-your-write là gì, sửa sao?"
- "Phân biệt replication vs sharding vs partitioning vs federation."
- "Sync vs async replication — đánh đổi?"
- "Khi nào read replica hết tác dụng và bạn phải shard?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả đo / quan sát (lag ~?ms):**
- **Bài học / điều bất ngờ:**
