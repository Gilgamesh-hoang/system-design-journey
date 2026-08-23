# Kata: Phân tách Đọc/Ghi & Quan sát Replication Lag (PostgreSQL)

Kata này thiết lập một cụm PostgreSQL Primary-Replica bằng Docker Compose. 
Mục tiêu là hiểu rõ về kiến trúc Master-Slave, độ trễ đồng bộ (Replication Lag) và cách ứng dụng bị ảnh hưởng khi có độ trễ.

## 🚀 Kiến trúc
- **Primary (Master)**: Chạy ở port `5432`. Chỉ nhận các thao tác GHI (Write - INSERT, UPDATE, DELETE).
- **Replica (Slave)**: Chạy ở port `5433`. Chỉ nhận các thao tác ĐỌC (Read - SELECT).
- **Replication Lag**: Replica được cấu hình độ trễ nhân tạo (Artificial Delay) là **5 giây** (`recovery_min_apply_delay = '5s'`). Bất kỳ dữ liệu nào ghi vào Primary sẽ mất 5 giây mới xuất hiện ở Replica.

## 🛠️ Hướng dẫn thực hành

### Bước 1: Khởi động cụm Database
```bash
docker-compose up -d
```
Đợi khoảng 10-15 giây để quá trình sao chép base backup hoàn tất và Replica khởi động thành công.

### Bước 2: Chạy Script mô phỏng Application

Để chạy ứng dụng mô phỏng (tự động route ghi vào Primary, đọc từ Replica và đo lường độ trễ):

**1. Cài đặt thư viện kết nối Database (nếu chưa có):**
```bash
pip install psycopg2-binary
```

**2. Chạy Script:**
```bash
python app.py
```

---

### MỞ RỘNG: Kiến trúc thực tế với Middleware (Pgpool-II) & Spring Boot
Thay vì code tay nhiều kết nối, tôi đã bổ sung thêm 1 container `pgpool` vào `docker-compose.yml`. Đây là con Proxy đứng trung gian. 
Ngoài ra, tôi đã tạo sẵn cho bạn dự án mẫu **Spring Boot** trong thư mục `spring-demo/`. 

**Kiến trúc:**
- Ứng dụng Spring Boot chỉ kết nối tới đúng 1 địa chỉ duy nhất là `localhost:9999` (Cổng của Pgpool). App không hề biết ở dưới có Primary hay Replica.
- Pgpool sẽ tự động Parsing SQL: Gặp `INSERT` nó đẩy vào Primary (5432), gặp `SELECT` nó đẩy vào Replica (5433).

**Cách test nghiệm thu:**
1. Khởi động lại cụm Docker để bật thêm Pgpool:
```bash
docker-compose down -v
docker-compose up -d
```
2. Chạy ứng dụng Spring Boot (Cần cài đặt sẵn Maven/Java 17):
```bash
cd spring-demo
mvn spring-boot:run
```
3. Mở 1 terminal khác và gọi API Ghi:
```bash
curl -X POST "http://localhost:8080/api/write?name=SpringUser"
```
*(Ghi nhớ ID trả về)*
4. Ngay lập tức gọi API Đọc với ID vừa nhận:
```bash
curl "http://localhost:8080/api/read/<ID_VỪA_TẠO>"
```
Bạn sẽ thấy API trả về chuỗi **STALE READ DETECTED**. Chờ 5 giây gọi lại mới thấy **SUCCESS**. Điều này chứng minh Pgpool đã làm nhiệm vụ định tuyến cực kỳ xuất sắc và trong suốt (transparent) với lập trình viên!

---

### Bước 3: Hậu quả của Replication Lag đối với ứng dụng thực tế
- Nếu User vừa đăng ký tài khoản (Ghi vào Primary) và ngay lập tức trang web tự động chuyển hướng sang trang Profile (Đọc từ Replica) -> Hệ thống báo "User không tồn tại". 
- **Giải pháp xử lý (Trade-offs):**
  1. *Read-your-own-writes (Đọc lại dữ liệu vừa ghi):* Cấu hình Application để nếu một user vừa thực hiện Write, các request Read tiếp theo của chính user đó trong vòng vài giây tới sẽ trỏ thẳng vào Primary, không qua Replica.
  2. *Đồng bộ đồng thời (Synchronous Replication):* Bắt Primary phải đợi Replica confirm đã nhận data rồi mới báo thành công cho User. Đánh đổi: Write latency tăng cao và giảm Availability của hệ thống nếu Replica tèo.

## 🧹 Dọn dẹp
```bash
docker-compose down -v
```
