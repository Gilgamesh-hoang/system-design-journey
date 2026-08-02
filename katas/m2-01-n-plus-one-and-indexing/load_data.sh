#!/bin/bash
echo "Loading drivers.csv..."
docker exec -i m2-01-postgres psql -U user -d katadb -c "\COPY drivers(id, name, city) FROM '/seed/drivers.csv' WITH (FORMAT CSV);"
echo "Loading trips.csv..."
docker exec -i m2-01-postgres psql -U user -d katadb -c "\COPY trips(id, driver_id, status, amount) FROM '/seed/trips.csv' WITH (FORMAT CSV);"
echo "Updating sequences..."
docker exec -i m2-01-postgres psql -U user -d katadb -c "SELECT setval('drivers_id_seq', (SELECT MAX(id) FROM drivers)); SELECT setval('trips_id_seq', (SELECT MAX(id) FROM trips));"
echo "Data load complete!"
