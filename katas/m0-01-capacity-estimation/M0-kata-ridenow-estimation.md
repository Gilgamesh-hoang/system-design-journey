# Kata 1: Ước lượng tải cho RideNow (Back-of-the-envelope)

**Yêu cầu/Giả định:**
- Hệ thống RideNow (gọi xe tương tự Uber/Grab).
- Lượng chuyến xe hoàn thành: **100,000 chuyến / ngày**.
- Số lượng tài xế hoạt động (DAU): Giả sử trung bình 1 tài xế chạy 10 chuyến/ngày ➔ Có **10,000 tài xế** active mỗi ngày.
- Thời gian gửi GPS: Tài xế đang bật app sẽ gửi vị trí (vĩ độ, kinh độ) về server **mỗi 5 giây**.

---

## 1. Ước lượng QPS (Queries Per Second)

Hệ thống gọi xe luôn có 2 luồng riêng biệt với đặc thù tải hoàn toàn khác nhau. Chúng ta sẽ tính riêng từng luồng.

### A. Luồng Khách hàng (Rider) - Đặt xe
*Đặc điểm: Ít request, nhưng business logic phức tạp (tính tiền, tìm tài xế).*
- Giả sử để đặt thành công 1 chuyến xe, khách hàng gọi 5 API requests (Mở app tải giá, Nhập điểm đến, Bấm tìm tài xế, Chờ match, Xác nhận).
- Tổng request 1 ngày: `100,000 chuyến × 5 = 500,000 requests`.
- Sử dụng Magic Number (1 ngày = 100,000 giây):
- **Average QPS:** `500,000 / 100,000 = 5 QPS`.
- **Peak QPS (Giờ cao điểm mưa bão):** `5 QPS × 5 = 25 QPS`.
> **Kết luận:** Tải đặt xe cực kỳ nhỏ. Một con Web Server + Database cùi bắp nhất cũng gánh được 25 QPS vô tư.

### B. Luồng Tài xế (Driver) - Bắn tọa độ GPS
*Đặc điểm: Cực nhiều request, ghi liên tục, logic cực mỏng.*
- Có 10,000 tài xế active. Cứ 5 giây họ bắn tọa độ 1 lần.
- Số lượng request mỗi giây (QPS) = `10,000 / 5 = 2,000 QPS`.
- **Average Write QPS:** `2,000 QPS`.
- **Peak QPS:** Khung giờ cao điểm có thể lên `5,000 QPS`.
> **Kết luận:** Đây mới là nút thắt cổ chai (bottleneck) của ứng dụng gọi xe. Hệ thống phải chịu được 5,000 lệnh Ghi (Write) mỗi giây. Lúc này MySQL thông thường bắt đầu "thở dốc", ta phải dùng Caching (Redis GEO) hoặc NoSQL (Cassandra/DynamoDB) để gánh luồng bắn GPS này.

---

## 2. Ước lượng Dung lượng lưu trữ (Storage) trong 1 Năm

### A. Lưu trữ thông tin Chuyến đi (Trip Data)
- **Kích thước:** Thông tin 1 chuyến xe (ID, RiderID, DriverID, Giá tiền, Điểm A, Điểm B, Timestamps) rất nhẹ, khoảng **1 KB**.
- **1 ngày:** `100,000 chuyến × 1 KB = 100,000 KB ~ 100 MB`.
- **1 năm:** `100 MB × 365 = 36.5 GB`.
> **Kết luận:** Lịch sử chuyến đi 1 năm chỉ tốn 36.5 GB. Hoàn toàn có thể nhét gọn vào 1 con RDBMS (PostgreSQL/MySQL) thông thường mà chưa cần phải Sharding.

### B. Lưu trữ Lịch sử tọa độ GPS của Tài xế
*(Giả sử ta cần lưu lại toàn bộ đường đi của tài xế để sau này giải quyết khiếu nại, tính quãng đường)*
- **Kích thước:** 1 điểm GPS (DriverID, Lat, Long, Timestamp) khoảng **100 Bytes**.
- **Lượng ghi 1 giây:** `2,000 QPS × 100 Bytes = 200,000 Bytes = 200 KB / giây`.
- **Lượng ghi 1 ngày:** `200 KB/s × 100,000 giây = 20,000,000 KB = 20 GB / ngày`.
- **Lượng ghi 1 năm:** `20 GB × 365 = 7,300 GB = 7.3 TB`.
> **Kết luận:** 7.3 Terabytes một năm! Dữ liệu này quá lớn để nhét vào RDBMS truyền thống. Phải thiết kế hệ thống tách biệt: Vị trí "hiện tại" thì lưu ở RAM (Redis), còn vị trí "lịch sử" cứ vài phút lại batch đẩy ra Object Storage (AWS S3) hoặc Time-Series Database để lưu trữ lạnh (Cold Storage).

---
*Ghi chú: Bản thiết kế tính nhẩm này sẽ được dùng làm nền tảng để viết 1-page design doc cho RideNow v1 ở bài thực hành tiếp theo.*
