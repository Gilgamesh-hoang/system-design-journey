# M3-01: LRU Cache tự viết (HashMap + Doubly Linked List)

> **Module:** M3 — Caching
> **Concept:** Cache dung lượng cố định, evict phần tử Least-Recently-Used ở O(1).
> **Time-box:** ~1 buổi.
> **Prereq:** Hiểu HashMap, linked list, Big-O.

---

## 1. Vấn đề — vì sao có kata này
Cache có dung lượng hữu hạn → khi đầy phải bỏ bớt. **LRU** bỏ phần tử lâu nhất không dùng. Đây vừa là nền tảng eviction policy của Redis/Guava, vừa là **bài coding phỏng vấn ra rất thường xuyên** (LeetCode 146). Yêu cầu cả `get` lẫn `put` đều **O(1)** — đó là chỗ ăn điểm.

## 2. Mục tiêu (sau kata này tôi làm được gì)
Tự cài LRU cache **O(1)** cho cả `get` và `put`, giải thích được vì sao cần **kết hợp** HashMap (tra nhanh) + doubly linked list (đổi thứ tự nhanh), và so được với LFU.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] `LRUCache<K,V>` với `capacity` cố định.
- [ ] `get(key)`: có → trả value **và** đánh dấu vừa dùng (đưa lên đầu); không → miss.
- [ ] `put(key, value)`: thêm/cập nhật; nếu vượt capacity → **evict** phần tử LRU.

**Ràng buộc / phi chức năng:**
- [ ] `get` và `put` phải **O(1)** — không được duyệt list để tìm phần tử cũ nhất.
- [ ] Tự cài doubly linked list (không dùng thẳng `LinkedHashMap(accessOrder=true)` cho bản chính — nhưng nên viết thêm bản `LinkedHashMap` để đối chiếu).

**Stack đề xuất:** Java thuần.

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Test tự kiểm (`assert`): chuỗi thao tác chuẩn LeetCode 146 cho ra đúng kết quả (đúng phần tử bị evict).
- [ ] Test: sau khi vượt capacity, phần tử **LRU đúng** bị loại (không phải LIFO/FIFO nhầm).
- [ ] Chứng minh O(1): mô tả vì sao mỗi thao tác chỉ đụng vào vài node cố định.
- [ ] (Bonus) Bản `LinkedHashMap` cho ra kết quả **giống hệt** bản tự cài.

## 5. Gợi ý cách làm (không phải lời giải)
1. `HashMap<K, Node>` để tra O(1); doubly linked list giữ **thứ tự dùng** (đầu = mới nhất, đuôi = LRU).
2. Dùng **dummy head/tail** để khỏi xử lý null lằng nhằng khi thêm/xoá đầu-đuôi.
3. `get`/`put` = unlink node khỏi vị trí cũ rồi move-to-head; evict = xoá node trước tail.

## 6. Bẫy & Trade-off phải giải thích được
- Vì sao **không** dùng mỗi HashMap? → không biết cái nào cũ nhất. Vì sao không dùng mỗi list? → tra O(n). Cần **cả hai**.
- LRU vs **LFU**: LRU bỏ theo *thời điểm*, LFU theo *tần suất*. Hot key hiếm khi dùng nhưng quan trọng → LRU có thể evict oan.
- Thread-safety: bản này chưa đồng bộ; production cần lock/segment (nhắc là được, đừng over-engineer trong kata).

## 7. Câu hỏi phỏng vấn liên quan
- "Cài LRU cache O(1)." (coding trực tiếp)
- "Vì sao cần cả HashMap lẫn linked list?"
- "LRU vs LFU khác gì? Redis dùng cái nào?"
- "Làm LRU cache thread-safe thế nào?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả / test:**
- **Bài học / điều bất ngờ:**
