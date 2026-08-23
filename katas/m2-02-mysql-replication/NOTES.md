# M2-02: Master-Slave Replication + Read Replica (Docker Compose)

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

## 6. Bẫy & Trade-off (Liên hệ DDIA - Chương 5)
- **Async replication** (mặc định): master không chờ slave → nhanh, nhưng slave trễ → rủi ro mất data nếu master chết trước khi replicate. **Sync**: an toàn hơn, nhưng một node slave chết có thể kéo sập hệ thống (chặn write). Giải pháp thực tế thường là Semi-synchronous.
- **Replication Lag Anomalies:** 
  - Đọc lại dữ liệu vừa ghi nhưng không thấy (Giải pháp: **Read-your-write consistency**).
  - Thấy dữ liệu đi lùi thời gian (Giải pháp: **Monotonic reads**).
  - Vi phạm quan hệ nhân quả (Giải pháp: **Consistent prefix reads**).
- Replica **không** giúp scale ghi — chỉ scale đọc. Đây là bẫy phỏng vấn hay gặp.

## 7. Câu hỏi phỏng vấn liên quan
- "Read replica gây ra vấn đề gì về consistency? Kể tên 3 hiện tượng phổ biến do Replication Lag (như Read-your-write) và cách khắc phục?"
- "Phân biệt Single-leader, Multi-leader và Leaderless replication. Khi nào dùng Multi-leader?"
- "Sync vs async replication — đánh đổi? Nếu dùng Async mà Master chết thì sao?"
- "Khi nào read replica hết tác dụng và bạn phải Partitioning (Shard)?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:** 
  - Sử dụng PostgreSQL 15, thiết lập Streaming Replication qua Docker Compose.
  - Sử dụng tham số `recovery_min_apply_delay = '5s'` trong `postgresql.auto.conf` ở node Replica để tạo độ trễ nhân tạo thay vì dùng Tool giả lập nghẽn mạng.
- **Kết quả đo / quan sát (lag ~?ms):**
  - Cố tình tạo lag đúng 5000ms (5 giây).
  - Khi INSERT dữ liệu trên Primary, lập tức SELECT bên Replica sẽ gặp hiện tượng **Stale Read** (không thấy data). Chính xác sau 5 giây thì dữ liệu mới xuất hiện.
- **Bài học / điều bất ngờ:**
  - Nhận thấy rõ ràng rủi ro của Asynchronous Replication. Nếu hệ thống Read-Heavy sử dụng chiến lược này, bắt buộc phải có cơ chế **Read-Your-Own-Writes** ở Application layer (ví dụ: route request của user về Primary trong 10 giây đầu sau khi họ thực hiện Write) để tránh lỗi UX.
