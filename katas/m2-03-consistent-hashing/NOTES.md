# M2-03: Consistent Hashing + Virtual Nodes (tự viết bằng Java)

> **Module:** M2 — Databases
> **Concept:** Phân phối key qua N node sao cho thêm/bớt node chỉ xáo trộn tối thiểu.
> **Time-box:** ~2 buổi.
> **Prereq:** Hiểu hash function, `TreeMap`/sorted structure.

---

## 1. Vấn đề — vì sao có kata này
`hash(key) % N` phân phối đều, nhưng khi **thêm/bớt 1 node** (N đổi) thì **gần như toàn bộ key phải remap** → cache miss hàng loạt / rebalance khổng lồ. **Consistent hashing** giải quyết: thêm/bớt 1 node chỉ ảnh hưởng ~`1/N` số key. RideNow dùng nó khi shard `driver-location` theo node.

## 2. Mục tiêu (sau kata này tôi làm được gì)
Tự cài **vòng hash** + **virtual nodes** bằng Java, và **chứng minh bằng số đo** rằng consistent hashing xáo trộn ít hơn hẳn `% N` khi thay đổi số node.

## 3. Yêu cầu (Requirements)
**Chức năng:**
- [ ] Ring dùng cấu trúc sorted (vd `TreeMap<Long, Node>`); `getNode(key)` = node đầu tiên theo chiều kim đồng hồ.
- [ ] `addNode` / `removeNode` cập nhật ring.
- [ ] Mỗi node vật lý map thành **V virtual nodes** (vd 100–200) để phân phối đều.

**Ràng buộc / phi chức năng:**
- [ ] Test phân phối: rải **1 triệu key** vào ring, đo **độ lệch tải** giữa các node (min/max/stddev).
- [ ] Test rebalance: đo **% key phải di chuyển** khi thêm 1 node và khi bớt 1 node — so sánh với `% N`.

**Stack đề xuất:** Java thuần, hash 64-bit (vd MurmurHash/`Hashing.murmur3` của Guava hoặc SHA-1 rút gọn).

## 4. Definition of Done — Expected / Acceptance criteria
- [ ] Bảng số liệu: với V virtual nodes tăng dần → độ lệch tải **giảm** (chứng minh vì sao cần vnode).
- [ ] Thêm 1 node vào cụm N node: consistent hashing di chuyển **~1/(N+1)** số key; `% N` di chuyển **~toàn bộ**. Có con số so sánh.
- [ ] 1 test tự kiểm (`assert`) rằng % key di chuyển < 1 ngưỡng khi thêm node.

## 5. Gợi ý cách làm (không phải lời giải)
1. Đặt node lên ring tại `hash(nodeId + "#" + i)` với `i = 0..V-1`.
2. `getNode(key)`: `ring.ceilingEntry(hash(key))`, nếu null thì `firstEntry` (vòng qua 0).
3. Đo rebalance: snapshot map key→node trước và sau khi add/remove, đếm số key đổi chủ.

## 6. Bẫy & Trade-off (Liên hệ DDIA - Chương 6)
- **Hash Partitioning vs Range Partitioning:** Hash giúp rải data đều (chống Hot Spot), nhưng đánh đổi là **mất khả năng Range Query** (query theo khoảng). Range query thì ngược lại.
- **Không có virtual nodes → tải lệch** nặng (node "may mắn" ôm cung lớn). Vnode là mấu chốt, không phải chi tiết phụ. V lớn → phân phối đều hơn nhưng tốn bộ nhớ ring + tra cứu chậm hơn.
- Khả năng Rebalancing: Khi scale up/down, ta muốn lượng dữ liệu phải chuyển mạng là tối thiểu. (Fixed number of partitions vs Dynamic partitioning).
- Lỗi kinh điển: Hot Partition do **lựa chọn Shard Key sai** (ví dụ: Shard theo ngày tháng, dẫn đến data hôm nay dồn toàn bộ vào 1 node).

## 7. Câu hỏi phỏng vấn liên quan
- "Làm sao để Partitioning dữ liệu? Đánh đổi giữa Key Range và Key Hash là gì?"
- "Consistent hashing giải quyết vấn đề gì so với `hash % N` truyền thống?"
- "Virtual node để làm gì? Bỏ đi thì sao?"
- "Thêm 1 node thì bao nhiêu % key phải di chuyển trong Consistent Hashing?"
- "Làm sao để xử lý Hot Spots hoặc Skewed Workloads khi một ID có quá nhiều request?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:** 
  - Cài đặt vòng tròn hash (Ring) bằng cấu trúc `TreeMap<Long, Node>` của Java để có thể lấy Node tiếp theo trên vòng một cách tự động thông qua hàm `tailMap(hash).firstKey()`.
  - Thay vì dùng `String.hashCode()` (dễ đụng độ và phân phối kém), tôi đã dùng thuật toán băm mã hóa MD5 cắt lấy 64-bit đầu tiên (`long`) để phân phối key đều hơn trên vòng 64-bit.
  - Áp dụng "Virtual Nodes" bằng cách thêm hậu tố `#0`, `#1`... vào tên Node trước khi băm, giúp 1 server vật lý xuất hiện V lần trên vòng tròn.
- **Kết quả đo (độ lệch tải, % rebalance):** (Mô phỏng 1 triệu key)
  - **Phân phối tải (10 Nodes):**
    - `hash % N` truyền thống: Phân phối gần như hoàn hảo (Độ lệch chuẩn: 196)
    - Consistent Hashing (V = 1): Cực kỳ lệch. Node ít nhất nhận 17k key, Node nhiều nhất nhận 226k key (Độ lệch chuẩn: 61,000)
    - Consistent Hashing (V = 100): Phân phối tốt hơn rất nhiều. Min: 81k, Max: 120k (Độ lệch chuẩn giảm xuống còn 10,654)
  - **Rebalancing (Thêm 1 Node vào cụm 10 Nodes):**
    - `hash % N` truyền thống: **90.91%** key bị thay đổi Node (Thảm họa cache miss).
    - Consistent Hashing (V = 100): Chỉ **8.26%** key bị dịch chuyển (Rất sát với lý thuyết `1/11 ~ 9.09%`).
- **Bài học / điều bất ngờ:**
  - Vòng băm nhất quán tự thân nó KHÔNG HỀ chia đều tải như mọi người lầm tưởng. Nếu không dùng Virtual Nodes (V=1), tải sẽ bị lệch thê thảm.
  - Số lượng Virtual Node càng cao (V=100) thì tải càng đều, nhưng sẽ tốn thêm chi phí bộ nhớ cho TreeMap và thời gian tra cứu `O(log(V*N))` thay vì `O(log(N))`.
