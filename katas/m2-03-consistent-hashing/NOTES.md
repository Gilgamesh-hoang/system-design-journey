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

## 6. Bẫy & Trade-off phải giải thích được
- **Không có virtual nodes → tải lệch** nặng (node "may mắn" ôm cung lớn). Vnode là mấu chốt, không phải chi tiết phụ.
- V lớn → phân phối đều hơn nhưng tốn bộ nhớ ring + tra cứu chậm hơn.
- Consistent hashing giải bài **rebalance**, không giải bài **hot key** (1 key nóng vẫn dồn 1 node).
- Đây là nền của Redis Cluster, Cassandra, DynamoDB partitioning.

## 7. Câu hỏi phỏng vấn liên quan
- "Consistent hashing giải quyết vấn đề gì so với `hash % N`?"
- "Virtual node để làm gì? Bỏ đi thì sao?"
- "Consistent hashing có chống được hot partition không?"
- "Thêm 1 node thì bao nhiêu % key phải di chuyển?"

## 8. Ghi chú của tôi *(điền sau khi làm)*
- **Approach thực tế:**
- **Kết quả đo (độ lệch tải, % rebalance):**
- **Bài học / điều bất ngờ:**
