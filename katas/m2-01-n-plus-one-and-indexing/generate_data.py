import csv
import random
import os

NUM_DRIVERS = 10_000
NUM_TRIPS = 2_000_000

CITIES = ['Hanoi', 'Ho Chi Minh', 'Da Nang', 'Can Tho', 'Hai Phong']
STATUSES = ['COMPLETED', 'CANCELED', 'ON_GOING', 'WAITING']
NAMES_FIRST = ['Nguyen', 'Tran', 'Le', 'Pham', 'Hoang']
NAMES_LAST = ['Anh', 'Binh', 'Cuong', 'Dung', 'Em', 'Phong', 'Trang']

print("Generating seed/drivers.csv...")
with open('seed/drivers.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    for i in range(1, NUM_DRIVERS + 1):
        name = f"{random.choice(NAMES_FIRST)} {random.choice(NAMES_LAST)}"
        city = random.choice(CITIES)
        writer.writerow([i, name, city])

print("Generating seed/trips.csv (2 million rows)...")
with open('seed/trips.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    for i in range(1, NUM_TRIPS + 1):
        driver_id = random.randint(1, NUM_DRIVERS)
        status = random.choice(STATUSES)
        amount = round(random.uniform(5.0, 50.0), 2)
        writer.writerow([i, driver_id, status, amount])

print("Generation complete!")
