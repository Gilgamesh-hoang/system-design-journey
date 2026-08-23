import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simulator showing how Fencing Tokens prevent a "Zombie Client" (caused by GC Pauses)
 * from overwriting data in a distributed system.
 */
public class FencingTokenSimulator {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Bắt đầu mô phỏng Bóng ma Zombie & Fencing Tokens ===");
        
        LockService lockService = new LockService();
        StorageService storageService = new StorageService();

        // Client 1 (Nạn nhân bị GC Pause)
        Thread client1 = new Thread(() -> {
            try {
                System.out.println("[Client 1] Xin cấp khóa (Lease)...");
                LockResponse lease = lockService.grantLease("Client 1");
                System.out.println("[Client 1] Nhận được khóa! Token của tôi là: " + lease.token);
                
                // MÔ PHỎNG GC PAUSE: 
                // Client 1 bị hệ thống đóng băng 3 giây (vượt qua thời gian hiệu lực của khóa)
                System.out.println("[Client 1] 💀 Bị dính Stop-The-World GC Pause! Đang ngủ say...");
                Thread.sleep(3000); 
                System.out.println("[Client 1] 🧟 Tỉnh dậy! Ảo tưởng sức mạnh tưởng mình vẫn đang cầm khóa.");
                
                // Cố gắng ghi đè dữ liệu
                System.out.println("[Client 1] Gọi lệnh ghi Storage...");
                storageService.writeData("Client 1", "Dữ liệu RÁC của Client 1", lease.token);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Client 2
        Thread client2 = new Thread(() -> {
            try {
                // Đợi một lúc để đảm bảo Client 1 đang bị GC Pause và khóa của nó hết hạn
                Thread.sleep(1500);
                
                System.out.println("\n[Client 2] Thấy khóa trống, xin cấp khóa (Lease)...");
                LockResponse lease = lockService.grantLease("Client 2");
                System.out.println("[Client 2] Nhận được khóa! Token của tôi là: " + lease.token);
                
                System.out.println("[Client 2] Gọi lệnh ghi Storage...");
                storageService.writeData("Client 2", "Dữ liệu VÀNG THẬT của Client 2", lease.token);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        client1.start();
        client2.start();

        client1.join();
        client2.join();
        
        System.out.println("\n=== KẾT QUẢ CUỐI CÙNG TRONG DATABASE ===");
        System.out.println(storageService.getDatabaseContent());
    }

}

// ==========================================
// THÀNH PHẦN 1: Zookeeper / Lock Service
// ==========================================
class LockService {
    // Fencing token tăng dần đơn điệu (Monotonic)
    private final AtomicLong tokenGenerator = new AtomicLong(0);
    private final ReentrantLock internalLock = new ReentrantLock();

    public LockResponse grantLease(String clientId) {
        internalLock.lock();
        try {
            long token = tokenGenerator.incrementAndGet();
            return new LockResponse(token, clientId);
        } finally {
            internalLock.unlock();
        }
    }
}

class LockResponse {
    long token;
    String owner;

    public LockResponse(long token, String owner) {
        this.token = token;
        this.owner = owner;
    }
}

// ==========================================
// THÀNH PHẦN 2: Storage Service / Database
// ==========================================
class StorageService {
    // Storage nhớ Token lớn nhất mà nó từng phục vụ (The Fencing Check)
    private long lastSeenToken = 0;
    
    // Nơi lưu trữ dữ liệu thực tế
    private String databaseContent = "Trống";
    private final ReentrantLock storageLock = new ReentrantLock();

    public void writeData(String clientId, String data, long requestToken) {
        storageLock.lock();
        try {
            System.out.printf("[Storage] Nhận request từ %s (Token: %d). Token cao nhất hiện tại: %d\n", 
                                clientId, requestToken, lastSeenToken);
            
            // FENCING CHECK: Rào chắn chặn Zombie
            if (requestToken < lastSeenToken) {
                System.err.printf("[Storage] 🛑 TỪ CHỐI %s! Token %d đã cũ. Hệ thống đã phục vụ token lớn hơn (%d).\n", 
                                  clientId, requestToken, lastSeenToken);
                return;
            }

            // Chấp nhận ghi
            System.out.printf("[Storage] ✅ Chấp nhận ghi dữ liệu từ %s\n", clientId);
            this.lastSeenToken = requestToken;
            this.databaseContent = data;

        } finally {
            storageLock.unlock();
        }
    }

    public String getDatabaseContent() {
        return databaseContent;
    }
}
