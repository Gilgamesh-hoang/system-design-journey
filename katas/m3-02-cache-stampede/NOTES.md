# M3-02: Cache Stampede — Lock / Request Coalescing

> **Module:** M3 — Caching
> **Concept:** Chống nhiều thread cùng miss 1 key hết hạn rồi đồng loạt đánh DB.
> **Time-box:** ~1–2 buổi.
> **Prereq:** Kata LRU; hiểu cache-aside, TTL, concurrency cơ bản.

---

## 1. Vấn đề — vì sao có kata này
Cache-aside: 1 hot key hết TTL đúng lúc 1000 request đang tới → **tất cả cùng miss**, cùng lao xuống DB rebuild → DB **sập vì thundering herd** (cache stampede). Phải **nhìn thấy** số lần gọi DB tăng vọt, rồi sửa và đo lại — mới nói được trong phỏng vấn.

## 2. Mục tiêu (sau kata này tôi làm được gì)
Tái hiện được cache stampede và **đo** số lần gọi DB; sửa bằng **lock / request coalescing** (single-flight) và/hoặc **TTL jitter**; giải thích khác biệt giữa các cách chống.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] Cache-aside quanh 1 "DB call" giả (hàm `loadFromDb` có `Thread.sleep` + đếm số lần được gọi).
- [ ] Sinh tải: **N thread (vd 500)** cùng `get(sameKey)` ngay sau khi key hết hạn.
- [ ] Bản **có lỗi** (naive): đếm số lần `loadFromDb` bị gọi (kỳ vọng ~N).
- [ ] Bản **đã sửa**: chỉ **1** thread rebuild, N-1 thread còn lại chờ & dùng chung kết quả.

**Ràng buộc / phi chức năng:**
- [ ] Có counter đo `loadFromDb` calls để so trước/sau (phải thấy N → ~1).
- [ ] Thử **ít nhất 2** kỹ thuật và so sánh: (a) mutex/lock per-key coalescing, (b) TTL jitter / early recompute.

**Stack đề xuất:** Java (`ConcurrentHashMap` + `CompletableFuture`/lock) cho bản in-process; bonus: distributed lock bằng Redis `SETNX`.

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Số liệu: naive ~**N** DB calls → sau khi coalescing còn **1–2** DB calls cho cùng burst.
- [ ] Giải thích được vì sao lock per-key **tốt hơn** 1 global lock (không chặn key khác).
- [ ] Nêu được vì sao **TTL jitter** giảm xác suất nhiều key nóng cùng hết hạn 1 lúc.
- [ ] 1 test tự kiểm `assert dbCalls <= 2` sau burst.

## 5. Gợi ý cách làm (không phải lời giải)
1. Coalescing in-process: `ConcurrentHashMap<Key, CompletableFuture<V>>` — thread đầu tạo future & load, các thread sau `join()` future đó.
2. Lock per-key thay vì global để không nghẽn toàn bộ cache.
3. Distributed: `SETNX lock:key` — ai chiếm được thì rebuild, còn lại chờ + đọc lại (hoặc trả stale).

## 6. Bẫy & Trade-off phải giải thích được
- **Lock/coalescing** giảm DB call nhưng các thread phải **chờ** → tăng latency đuôi. Trade-off latency vs tải DB.
- **Serve-stale-while-revalidate**: trả data cũ + rebuild nền → nhanh, nhưng chấp nhận stale.
- **Cache penetration** (key không tồn tại) khác stampede → chống bằng cache giá trị null / bloom filter (phân biệt được là ăn điểm).
- Distributed lock có rủi ro lock chết (TTL lock + fencing).

## 7. Câu hỏi phỏng vấn liên quan
- "Cache stampede / thundering herd là gì, chống thế nào?"
- "Khác nhau giữa cache stampede, cache penetration, hot key?"
- "TTL jitter để làm gì?"
- "Distributed lock để rebuild cache có rủi ro gì?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả đo (DB calls trước/sau):**
- **Bài học / điều bất ngờ:**
