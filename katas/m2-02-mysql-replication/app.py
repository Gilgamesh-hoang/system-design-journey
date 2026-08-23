import psycopg2
import time
from datetime import datetime

# Connection strings
PRIMARY_DSN = "dbname=app_db user=admin password=password host=localhost port=5432"
REPLICA_DSN = "dbname=app_db user=admin password=password host=localhost port=5433"

def print_replication_lag(primary_conn):
    """Query pg_stat_replication on Primary to see the lag."""
    with primary_conn.cursor() as cur:
        # Check pg_stat_replication
        cur.execute("""
            SELECT 
                client_addr, 
                state, 
                sync_state,
                pg_wal_lsn_diff(pg_current_wal_lsn(), replay_lsn) AS lag_bytes,
                write_lag, flush_lag, replay_lag
            FROM pg_stat_replication;
        """)
        rows = cur.fetchall()
        if not rows:
            print("  [Lag Monitor] No active replicas found!")
        for row in rows:
            print(f"  [Lag Monitor] Replica {row[0]} | State: {row[1]} | Sync: {row[2]} | Lag Bytes: {row[3]} | Replay Lag Time: {row[6]}")

def main():
    print("Connecting to databases...")
    try:
        primary_conn = psycopg2.connect(PRIMARY_DSN)
        primary_conn.autocommit = True
        replica_conn = psycopg2.connect(REPLICA_DSN)
        replica_conn.autocommit = True
    except Exception as e:
        print(f"Connection failed. Make sure 'docker-compose up -d' is running and ready. Error: {e}")
        return

    print("\n--- SIMULATING APP ROUTING (Write -> Primary, Read -> Replica) ---")
    
    # 1. WRITE TO PRIMARY
    user_name = f"TestUser_{int(time.time())}"
    print(f"[App] Writing new user '{user_name}' to PRIMARY...")
    with primary_conn.cursor() as cur:
        cur.execute("INSERT INTO users (name) VALUES (%s) RETURNING id;", (user_name,))
        new_id = cur.fetchone()[0]
    
    print(f"[App] Write successful! Inserted ID: {new_id}")

    # 2. IMMEDIATE READ FROM REPLICA
    print(f"\n[App] Reading ID {new_id} from REPLICA immediately...")
    with replica_conn.cursor() as cur:
        cur.execute("SELECT id, name FROM users WHERE id = %s;", (new_id,))
        row = cur.fetchone()
        
    if row is None:
        print("  => STALE READ DETECTED! The replica does not have the data yet.")
    else:
        print(f"  => SUCCESS: Found {row}")

    # 3. MEASURE REPLICATION LAG
    print("\n[App] Checking Replication Lag Metrics from Primary...")
    print_replication_lag(primary_conn)

    # 4. WAIT AND RETRY
    print("\n[App] Waiting for Replica to catch up (configured delay is 5s)...")
    for i in range(1, 7):
        time.sleep(1)
        print(f"  ... {i}s")
        with replica_conn.cursor() as cur:
            cur.execute("SELECT id, name FROM users WHERE id = %s;", (new_id,))
            row = cur.fetchone()
            if row is not None:
                print(f"  => EVENTUAL CONSISTENCY ACHIEVED at {i}s: Found {row}")
                break
    
    primary_conn.close()
    replica_conn.close()

if __name__ == "__main__":
    main()
