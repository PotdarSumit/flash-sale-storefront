
import concurrent.futures
import random
import time
import requests
from collections import Counter

BASE_URL = "http://localhost:8080"
REQUESTS_PER_SECOND = 50
DURATION_SECONDS = 30

results = []
results_lock = __import__("threading").Lock()
start_time = time.perf_counter()


def read_product(product_id):
    started = time.perf_counter()

    try:
        response = requests.get(
            f"{BASE_URL}/api/products/{product_id}",
            timeout=10
        )
        elapsed = time.perf_counter() - started

        result = {
            "status": response.status_code,
            "latency": elapsed,
            "product_id": product_id
        }
    except Exception as exc:
        result = {
            "status": "ERROR",
            "latency": time.perf_counter() - started,
            "product_id": product_id,
            "error": str(exc)
        }

    with results_lock:
        results.append(result)


def choose_product():
    # 80% popular products; 20% remaining products.
    if random.random() < 0.80:
        return random.randint(1, 1000)
    return random.randint(1001, 50000)


print("Starting R1 normal traffic test...")
print(f"Target: {REQUESTS_PER_SECOND} reads/sec for {DURATION_SECONDS} seconds")

with concurrent.futures.ThreadPoolExecutor(max_workers=50) as executor:
    futures = []

    for request_number in range(REQUESTS_PER_SECOND * DURATION_SECONDS):
        target_time = start_time + request_number / REQUESTS_PER_SECOND

        delay = target_time - time.perf_counter()
        if delay > 0:
            time.sleep(delay)

        product_id = choose_product()
        futures.append(executor.submit(read_product, product_id))

    concurrent.futures.wait(futures)

total_time = time.perf_counter() - start_time
counts = Counter(str(result["status"]) for result in results)
latencies = sorted(result["latency"] for result in results)

print("\n--- R1 RESULTS ---")
print(f"Requests completed: {len(results)}")
print(f"Elapsed time: {total_time:.2f} seconds")
print(f"Actual average throughput: {len(results) / total_time:.2f} requests/sec")
print(f"HTTP results: {dict(counts)}")

if latencies:
    p95_index = min(int(len(latencies) * 0.95), len(latencies) - 1)
    print(f"95th percentile latency: {latencies[p95_index] * 1000:.1f} ms")

errors = [r for r in results if r["status"] == "ERROR"]
if errors:
    print("Sample error:", errors[0].get("error"))

print("\nCheck that requests completed successfully and inspect backend metrics.")