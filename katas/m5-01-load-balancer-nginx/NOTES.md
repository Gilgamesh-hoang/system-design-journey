# M5-01: Nginx Load Balancing + Health Check

> **Module:** M5 — Scalability & Kiến trúc dịch vụ
> **Concept:** Scale ngang stateless service sau nginx; health check tự loại node chết.
> **Time-box:** ~1–2 buổi.
> **Prereq:** Docker Compose; hiểu stateless service.

---

## 1. Vấn đề — vì sao có kata này
Scale ngang = chạy nhiều instance của **cùng** 1 service, đặt sau **load balancer**. Nhưng chỉ scale ngang được nếu service **stateless** (state đẩy ra Redis/DB). Và LB phải **health check** để tự ngừng gửi request tới node chết. Đây là nền của mọi kiến trúc production và là bẫy phỏng vấn "làm sao service scale ngang được?".

## 2. Mục tiêu (sau kata này tôi làm được gì)
Chạy **3 instance** 1 service Spring Boot sau **nginx**, quan sát request được **phân phối** giữa các instance, kill 1 instance và thấy nginx **định tuyến lại** nhờ health check; giải thích L4 vs L7 và vì sao tránh sticky session.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] 1 service Spring Boot có endpoint `/whoami` trả về **instance id** (để thấy request đi đâu) và `/health`.
- [ ] Docker Compose chạy **3 instance** + 1 **nginx** làm reverse proxy / LB (`upstream` block).
- [ ] Gọi qua nginx nhiều lần → thấy response luân phiên giữa 3 instance.

**Ràng buộc / phi chức năng:**
- [ ] Bật **health check** (passive `max_fails`/`fail_timeout`, hoặc active nếu dùng nginx plus/module) → node fail bị loại.
- [ ] Service **stateless**: không giữ session in-memory; chứng minh kill 1 node giữa chừng không mất "trạng thái người dùng".
- [ ] Thử **ít nhất 2** thuật toán: round-robin (mặc định) và least-connections.

**Stack đề xuất:** Spring Boot + nginx + Docker Compose. Bonus: `ab`/`hey` để bắn tải.

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Demo: gọi `/whoami` 10 lần → thấy phân phối qua cả 3 instance.
- [ ] Kill 1 instance → request tiếp theo **vẫn 200**, chỉ còn phân phối qua 2 node còn sống (lưu log nginx).
- [ ] Instance sống lại → nginx đưa trở lại pool.
- [ ] Giải thích round-robin vs least-connections khác nhau khi request có thời lượng lệch nhau.

## 5. Gợi ý cách làm (không phải lời giải)
1. `upstream backend { server app1; server app2; server app3; }` + `proxy_pass`.
2. Instance id: đọc từ ENV/hostname container để `/whoami` phân biệt được.
3. Bắn tải bằng `hey -n 1000 -c 50` rồi kill 1 container giữa chừng, xem có request nào 5xx không.

## 6. Bẫy & Trade-off phải giải thích được
- **L4 (transport, theo IP/port) vs L7 (HTTP-aware, route theo path/header)** — nginx làm L7.
- **Sticky session** giúp giữ state nhưng phá cân bằng tải + kill node là mất session → nên **stateless + shared store**.
- LB vs reverse proxy vs API gateway: chồng lấn khái niệm, phải phân biệt được.
- Health check passive vs active — passive chỉ phát hiện khi đã có request lỗi.

## 7. Câu hỏi phỏng vấn liên quan
- "Làm sao để service scale ngang được?" (→ stateless)
- "L4 và L7 load balancing khác nhau chỗ nào?"
- "Vì sao nên tránh sticky session?"
- "Load balancer, reverse proxy, API gateway khác gì nhau?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả (phân phối, hành vi khi kill node):**
- **Bài học / điều bất ngờ:**
