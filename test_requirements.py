
import concurrent.futures
import random
import threading
import time
import requests
import subprocess
import json
from collections import Counter

BASE = "http://localhost:8080"
WAREHOUSE = "http://localhost:8081"
RESULTS = {}

def report(name, passed, detail):
    RESULTS[name] = passed
    print(f"\n{'PASS' if passed else 'FAIL'} | {name}: {detail}")

def get_json(url, **kwargs):
    r = requests.get(url, timeout=15, **kwargs)
    r.raise_for_status()
    return r.json()

def test_health():
    try:
        r = requests.get(f"{BASE}/actuator/health", timeout=5)
        report("Backend health", r.status_code == 200, f"HTTP {r.status_code}")
    except Exception as e:
        report("Backend health", False, str(e))

def test_warehouse_health():
    try:
        r = requests.get(
            f"{WAREHOUSE}/api/warehouse/inventory",
            params={"page": 0, "size": 5}, timeout=10
        )
        report("Warehouse simulator", r.status_code == 200,
               f"HTTP {r.status_code}")
    except Exception as e:
        report("Warehouse simulator", False, str(e))

def test_r1():
    print("\nRunning R1: 50 reads/sec for 10 seconds...")
    results = []
    lock = threading.Lock()
    start = time.perf_counter()

    def read_one(_):
        product_id = random.randint(1, 1000) if random.random() < 0.8 else random.randint(1001, 50000)
        try:
            r = requests.get(f"{BASE}/api/products/{product_id}", timeout=15)
            result = r.status_code
        except Exception:
            result = "ERROR"
        with lock:
            results.append(result)

    try:
        with concurrent.futures.ThreadPoolExecutor(max_workers=50) as pool:
            futures = []
            for i in range(500):
                target = start + i / 50
                delay = target - time.perf_counter()
                if delay > 0:
                    time.sleep(delay)
                futures.append(pool.submit(read_one, i))
            concurrent.futures.wait(futures)

        elapsed = time.perf_counter() - start
        counts = Counter(results)
        throughput = len(results) / elapsed
        ok = counts.get(200, 0)
        report("R1 normal traffic", throughput >= 45 and ok == len(results),
               f"{throughput:.1f} req/sec; HTTP results={dict(counts)}")
    except Exception as e:
        report("R1 normal traffic", False, str(e))

def test_r3_metrics():
    print("\nChecking database-read metrics and limiter counters...")
    try:
        r = requests.get(
            f"{BASE}/actuator/metrics/storefront.database.product.reads",
            timeout=10
        )
        if r.status_code != 200:
            report("R3 read metric exposed", False, f"HTTP {r.status_code}: {r.text[:200]}")
        else:
            data = r.json()
            count = next((m["value"] for m in data.get("measurements", [])
                          if m["statistic"] == "COUNT"), None)
            report("R3 read metric exposed", count is not None,
                   f"instrumented read count={count}")
        r = requests.get(f"{BASE}/api/dashboard/metrics", timeout=10)
        if r.status_code == 200:
            data = r.json()
            print("Dashboard limiter counters:",
                  "granted=", data.get("databaseReadPermitsGranted"),
                  "rejected=", data.get("databaseReadRequestsRejected"))
            print("NOTE: cumulative permit counters do not prove the 25/sec limit.")
        else:
            report("Dashboard metrics", False, f"HTTP {r.status_code}")
    except Exception as e:
        report("R3 metrics", False, str(e))

def test_r4():
    print("\nChecking warehouse and backend inventory endpoints...")
    try:
        w = get_json(f"{WAREHOUSE}/api/warehouse/inventory",
                     params={"page": 0, "size": 5})
        b = get_json(f"{BASE}/api/internal/warehouse/inventory",
                     params={"page": 0, "size": 100})
        backend_by_id = {x.get("productId", x.get("id")): x for x in b}
        matches = 0
        checked = 0
        for item in w:
            pid = item.get("productId")
            if pid in backend_by_id:
                checked += 1
                other = backend_by_id[pid]
                stock = other.get("physicalStock", other.get("warehouseStock"))
                price = other.get("price")
                if stock == item.get("physicalStock") and price is not None and abs(float(price) - float(item["price"])) < 0.01:
                    matches += 1
        report("R4 inventory snapshot", checked > 0 and matches == checked,
               f"matching products={matches}/{checked}; snapshots may differ during updates")
        print("NOTE: This snapshot comparison does not prove the five-second requirement.")
    except Exception as e:
        report("R4 inventory endpoints", False, str(e))

def test_r6_docker():
    print("\nChecking Docker Compose configuration...")
    try:
        p = subprocess.run(
            ["docker", "compose", "config", "--services"],
            capture_output=True, text=True, timeout=15
        )
        services = [x.strip() for x in p.stdout.splitlines() if x.strip()]
        # This is only a configuration check, not proof that instances are running.
        has_backend = any("backend" in s.lower() or "app" in s.lower() for s in services)
        has_db = any("postgres" in s.lower() or "database" in s.lower() or s.lower() == "db" for s in services)
        has_redis = any("redis" in s.lower() for s in services)
        report("R6 Compose configuration", p.returncode == 0 and has_backend and has_db and has_redis,
               f"services={services or p.stderr[:250]}")
        print("NOTE: Verify two backend instances and a load balancer separately.")
    except Exception as e:
        report("R6 Compose configuration", False, str(e))

def test_flash_sale_endpoint():
    try:
        r = requests.get(f"{BASE}/api/products/flash-sale", timeout=10)
        report("Flash-sale endpoint", r.status_code == 200, f"HTTP {r.status_code}")
        if r.status_code == 200:
            print("Flash-sale response sample:", r.text[:250])
    except Exception as e:
        report("Flash-sale endpoint", False, str(e))

if __name__ == "__main__":
    print("FLASH SALE STOREFRONT - QUICK REQUIREMENT CHECK")
    print("Ensure backend, warehouse simulator, PostgreSQL and Redis are running.")
    test_health()
    test_warehouse_health()
    test_flash_sale_endpoint()
    test_r1()
    test_r3_metrics()
    test_r4()
    test_r6_docker()

    print("\n" + "=" * 55)
    print("SUMMARY")
    for name, passed in RESULTS.items():
        print(f"{'PASS' if passed else 'FAIL'} | {name}")
    print("\nImportant: this quick check does NOT fully prove R2, R3, R4, R5, or R7.")
    print("Run your existing 300-shopper concurrency test separately for R2/R5/R7.")

