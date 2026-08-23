#!/bin/bash
set -e

# Wait for primary to be ready
echo "Waiting for primary to accept connections..."
until pg_isready -h primary -p 5432 -U admin; do
  sleep 2
done

# We need to make sure the primary has fully initialized the DB and tables
echo "Waiting a bit more for primary to run init scripts..."
sleep 5

echo "Starting base backup from primary..."
# Clear the data directory just in case
rm -rf /var/lib/postgresql/data/*

# Use pg_basebackup to copy data from primary
pg_basebackup -h primary -D /var/lib/postgresql/data -U replicator -vP -w

echo "Setting up replication delay..."
# Add replica configuration to postgresql.auto.conf
cat >> /var/lib/postgresql/data/postgresql.auto.conf <<EOF
primary_conninfo = 'host=primary port=5432 user=replicator password=repl_password'
# TẠO ĐỘ TRỄ NHÂN TẠO 5 GIÂY ĐỂ QUAN SÁT REPLICATION LAG
recovery_min_apply_delay = '5s'
EOF

# Create standby.signal to indicate this node should start in standby (replica) mode
touch /var/lib/postgresql/data/standby.signal

echo "Starting PostgreSQL in replica mode..."
# Execute the standard postgres entrypoint so it runs properly
exec docker-entrypoint.sh postgres
