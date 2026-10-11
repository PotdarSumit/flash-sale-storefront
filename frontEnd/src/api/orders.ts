import { requestJson } from "@/api/client";
import type { PurchaseRequest } from "@/types/product";

export async function purchaseProduct(body: PurchaseRequest, signal?: AbortSignal) {
  return requestJson<unknown>("/api/orders", {
    method: "POST",
    body: JSON.stringify(body),
    signal,
  });
}
