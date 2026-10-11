
import concurrent.futures
import threading
import requests
from collections import Counter

BASE = "http://localhost:8080"
SHOPPERS = 300
session_local = threading.local()

def session():
    if not hasattr(session_local, "value"):
        session_local.value = requests.Session()
    return session_local.value

# Find a flash-sale product with available stock.
r = requests.get(f"{BASE}/api/products/flash-sale", timeout=15)
r.raise_for_status()
products = r.json()

available = [p for p in products if p.get("stock", 0) > 0]
if not available:
    print("No flash-sale products currently have stock. Check warehouse data.")
    raise SystemExit(1)

product = available[0]
product_id = product["id"]
price = product["price"]
initial_stock = product["stock"]

print(f"Testing product ID: {product_id}")
print(f"Displayed price: {price}")
print(f"Initial displayed stock: {initial_stock}")
print(f"Sending {SHOPPERS} simultaneous attempts...")

barrier = threading.Barrier(SHOPPERS)

def buy(n):
    payload = {
        "productId": product_id,
        "quantity": 1,
        "customerIdentifier": f"flash-test-{n}",
        "displayedPrice": price
    }
    try:
        barrier.wait(timeout=45)
        response = session().post(
            f"{BASE}/api/orders", json=payload, timeout=60
        )
        return response.status_code, response.text[:250]
    except Exception as e:
        return "ERROR", str(e)

with concurrent.futures.ThreadPoolExecutor(max_workers=SHOPPERS) as pool:
    results = list(pool.map(buy, range(1, SHOPPERS + 1)))

counts = Counter(status for status, _ in results)
successes = counts.get(201, 0)

print("\nHTTP result counts:")
for status, count in sorted(counts.items(), key=lambda x: str(x[0])):
    print(f"  {status}: {count}")

print(f"\nSuccessful orders: {successes}")
print(f"Initial displayed stock: {initial_stock}")

errors = [(s, body) for s, body in results if s not in (201, 400)]
if errors:
    print("Unexpected response sample:", errors[0])

if successes <= initial_stock:
    print("No HTTP-level evidence of overselling.")
else:
    print("POTENTIAL OVERSELLING: investigate immediately.")

if successes == 0:
    print("No successful orders. Inspect the HTTP response bodies.")

