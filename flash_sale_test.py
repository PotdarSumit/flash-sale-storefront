
import concurrent.futures
import threading
import requests
from collections import Counter

BASE_URL = "http://localhost:8080"
PRODUCT_ID = 29
PRICE = 2447.45
SHOPPERS = 300

# Update this if your OrderController has a different class-level mapping.
ORDER_URL = f"{BASE_URL}/api/orders"

barrier = threading.Barrier(SHOPPERS)
session_local = threading.local()


def get_session():
    if not hasattr(session_local, "session"):
        session_local.session = requests.Session()
    return session_local.session


def buy(customer_number):
    payload = {
        "productId": PRODUCT_ID,
        "quantity": 1,
        "customerIdentifier": f"flash-test-customer-{customer_number}",
        "displayedPrice": PRICE
    }

    try:
        # Make all shoppers start as close together as possible.
        barrier.wait(timeout=30)
        response = get_session().post(
            ORDER_URL,
            json=payload,
            timeout=60
        )
        return response.status_code, response.text[:250]
    except Exception as exc:
        return "ERROR", str(exc)


if __name__ == "__main__":
    print(f"Sending {SHOPPERS} purchase attempts to {ORDER_URL}")
    with concurrent.futures.ThreadPoolExecutor(
        max_workers=SHOPPERS
    ) as executor:
        results = list(executor.map(buy, range(1, SHOPPERS + 1)))

    counts = Counter(status for status, _ in results)
    print("\nHTTP result counts:")
    for status, count in sorted(counts.items(), key=lambda item: str(item[0])):
        print(f"  {status}: {count}")

    successes = counts.get(201, 0)
    print(f"\nSuccessful orders: {successes}")
    print("Expected for starting stock 10: 10 successful orders")

    if successes == 10:
        print("HTTP-level check PASSED; verify database stock and order records.")
    else:
        print("HTTP-level check FAILED or needs investigation.")

    errors = [(s, body) for s, body in results if s == "ERROR"]
    if errors:
        print("\nSample connection/client error:", errors[0][1])
