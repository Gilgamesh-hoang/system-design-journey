# M7-01: Observability — Prometheus + Grafana + Alert

> **Module:** M7 — Observability & Performance Antipatterns
> **Concept:** Expose metrics từ Spring Boot, scrape bằng Prometheus, dashboard + alert.
> **Time-box:** ~2 buổi.
> **Prereq:** Spring Boot; Docker Compose.

---

## 1. Vấn đề — vì sao có kata này
"Production chậm/hay lỗi, điều tra từ đâu?" → **metrics → traces → logs**. Không có dashboard thì debug production bằng cách đoán. Đây là kỹ năng **rất hot 2025** và khó bị AI thay. Kata này dựng trụ cột đầu tiên (**metrics**): p95/p99 latency, error rate, QPS — và đặt **alert** đúng chỉ số.

## 2. Mục tiêu (sau kata này tôi làm được gì)
Expose metrics từ Spring Boot (Micrometer/Actuator), scrape bằng **Prometheus**, dựng **dashboard Grafana** (latency p95/p99, error rate, QPS), và đặt **1 alert** kích hoạt được; giải thích 3 trụ cột observability.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] Service Spring Boot bật **Actuator + Micrometer** expose `/actuator/prometheus`.
- [ ] Docker Compose: service + **Prometheus** (scrape config) + **Grafana**.
- [ ] Dashboard Grafana có **≥3 panel**: latency **p95/p99**, **error rate** (%5xx), **QPS**.
- [ ] **1 alert rule** (vd error rate > 5% trong 1 phút hoặc p99 > ngưỡng).

**Ràng buộc / phi chức năng:**
- [ ] Có endpoint để **cố ý tạo lỗi/độ trễ** (để bắn alert & thấy đồ thị nhảy).
- [ ] Metric phải phân biệt theo `uri`/`status` (dùng tag của Micrometer).

**Stack đề xuất:** Spring Boot Actuator + Micrometer Prometheus registry + Prometheus + Grafana (Docker Compose). Bắn tải bằng `hey`.

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Prometheus **Targets = UP**, scrape được metric của service.
- [ ] Dashboard hiển thị p95/p99, error rate, QPS **thay đổi thật** khi bắn tải.
- [ ] Cố ý gây lỗi → **alert chuyển sang FIRING** (screenshot).
- [ ] Giải thích được khác biệt **metrics vs logs vs traces** và khi nào dùng cái nào.

## 5. Gợi ý cách làm (không phải lời giải)
1. `management.endpoints.web.exposure.include=prometheus,health`; thêm `micrometer-registry-prometheus`.
2. PromQL: p99 = `histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[1m])) by (le))`.
3. Error rate = `sum(rate(...{status=~"5.."}[1m])) / sum(rate(...[1m]))`.

## 6. Bẫy & Trade-off phải giải thích được
- **Log nhiều ≠ observability** — cần structured log + trace id để nối across service (trụ cột 2 & 3, kata này chưa làm nhưng phải nói được).
- Alert quá nhạy → **alert fatigue**; chọn ngưỡng + `for:` window hợp lý.
- p99 phản ánh trải nghiệm đuôi tốt hơn average (average che giấu tail latency).
- Antipattern nối lại M2: chatty I/O / extraneous fetching = biến thể N+1 — phát hiện qua metric/trace.

## 7. Câu hỏi phỏng vấn liên quan
- "Production chậm/lỗi, bạn điều tra từ đâu?" (→ metrics → traces → logs)
- "Phân biệt metrics, logs, traces."
- "Vì sao nhìn p99 thay vì average?"
- "Distributed tracing giúp gì trong microservices?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả (dashboard, alert firing):**
- **Bài học / điều bất ngờ:**
