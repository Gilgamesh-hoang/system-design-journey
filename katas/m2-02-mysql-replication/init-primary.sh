#!/bin/bash
set -e

echo "Initializing primary database: creating replicator user and test table..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create replication user
    CREATE ROLE replicator WITH REPLICATION PASSWORD 'repl_password' LOGIN;

    -- Create a test table
    CREATE TABLE users (
        id SERIAL PRIMARY KEY,
        name VARCHAR(50),
        created_at TIMESTAMP DEFAULT NOW()
    );

    -- Insert some initial data
    INSERT INTO users (name) VALUES ('Alice'), ('Bob');
EOSQL

echo "Configuring pg_hba.conf to allow replication connections..."
echo "host replication replicator 0.0.0.0/0 md5" >> "$PGDATA/pg_hba.conf"
