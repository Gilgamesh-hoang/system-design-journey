# M2-03: N+1 Query & Indexing — phát hiện và sửa bằng EXPLAIN

> **Module:** M2 — Databases
> **Concept:** Đọc EXPLAIN, diệt N+1 trong JPA/Hibernate, thêm index đúng chỗ.
> **Time-box:** ~2 buổi.
> **Prereq:** JPA/Hibernate cơ bản, biết bật SQL log.

---

## 1. Vấn đề — vì sao có kata này
Đây là 2 antipattern **gặp nhiều nhất khi đi làm**: (1) **N+1** — load list N entity rồi lặp truy cập association → phát sinh N query con; (2) thiếu **index** → full table scan trên bảng lớn. Cả hai đều "chạy đúng ở local với 10 dòng" nhưng **sập ở production với vài triệu dòng**.

## 2. Mục tiêu (sau kata này tôi làm được gì)
**Đọc được `EXPLAIN ANALYZE`**, nhận ra Seq Scan vs Index Scan, phát hiện & sửa N+1 (fetch join / `@EntityGraph` / batch size), và **đo được** thời gian trước/sau khi thêm index.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] Seed bảng lớn: vd `trips` **~2–5 triệu dòng** + `drivers` (script sinh dữ liệu, không insert tay).
- [ ] **Tái hiện N+1**: load list trip rồi truy cập `trip.getDriver()` trong vòng lặp → bật `show-sql` đếm số query.
- [ ] **Tái hiện full scan**: query lọc theo cột chưa index (vd `WHERE status = ?`) trên bảng triệu dòng.

**Ràng buộc / phi chức năng:**
- [ ] Với mỗi vấn đề: chạy `EXPLAIN ANALYZE` **trước và sau** khi sửa, lưu output.
- [ ] Sửa N+1 bằng **ít nhất 2 cách** và so sánh (fetch join vs `@EntityGraph` vs `hibernate.default_batch_fetch_size`).

**Stack đề xuất:** Spring Data JPA + Postgres, Hikari, `spring.jpa.show-sql` + `p6spy` (đếm query).

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Bảng số liệu: N+1 giảm từ **N+1 query → 1–2 query**; latency trước/sau (ms).
- [ ] Full scan `Seq Scan (cost, rows, time)` → sau khi thêm index thành `Index Scan`; đo thời gian giảm bao nhiêu lần.
- [ ] Nêu được **1 trường hợp index KHÔNG được dùng** (vd hàm bọc cột, leading wildcard `LIKE '%x'`, kiểu lệch) — và vì sao.

## 5. Gợi ý cách làm (không phải lời giải)
1. Đếm query: bật `p6spy` hoặc Hibernate statistics thay vì đếm mắt.
2. Index: thử **composite index** đúng thứ tự cột theo mệnh đề WHERE + ORDER BY; thử **covering index** để tránh truy bảng.
3. Luôn `EXPLAIN ANALYZE` (không chỉ `EXPLAIN`) để có thời gian thật + số rows thật.

## 6. Bẫy & Trade-off phải giải thích được
- N+1 ẩn: `FetchType.EAGER` global "trông như" hết N+1 nhưng lại nạp thừa (extraneous fetching) — dự án cấm EAGER, dùng `@EntityGraph` per-query.
- Index tăng tốc **đọc** nhưng làm **chậm ghi** + tốn dung lượng → không index bừa.
- Thứ tự cột trong composite index quyết định nó có được dùng cho query không (leftmost prefix).
- Keyset pagination vs `OFFSET` lớn — OFFSET lớn vẫn quét bỏ hàng triệu dòng.

## 7. Câu hỏi phỏng vấn liên quan
- "Query này chậm, bạn debug thế nào?" (→ EXPLAIN → index/N+1)
- "N+1 là gì, phát hiện và sửa trong Hibernate ra sao?"
- "Khi nào index KHÔNG được dùng?"
- "Composite index: thứ tự cột có quan trọng không? Vì sao?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả đo (query count, latency, EXPLAIN trước/sau):**
- **Bài học / điều bất ngờ:**
