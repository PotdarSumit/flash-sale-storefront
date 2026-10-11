import { requestJson } from "@/api/client";
import type { PagePayload, PageResult, Product } from "@/types/product";

function normalizePage<T>(payload: unknown, fallbackPage = 0, fallbackSize = 20): PageResult<T> {
  if (Array.isArray(payload)) {
    return {
      items: payload as T[],
      page: fallbackPage,
      size: fallbackSize,
      totalPages: 1,
      totalElements: payload.length,
      hasNext: false,
      hasPrevious: false,
    };
  }

  const typed = payload as PagePayload<T> | undefined;
  const items = Array.isArray(typed?.content) ? typed.content : [];
  const page = typeof typed?.number === "number" ? typed.number : fallbackPage;
  const size = typeof typed?.size === "number" ? typed.size : fallbackSize;
  const totalPages = typeof typed?.totalPages === "number" ? typed.totalPages : items.length > 0 ? 1 : 0;
  const totalElements = typeof typed?.totalElements === "number" ? typed.totalElements : items.length;

  return {
    items,
    page,
    size,
    totalPages,
    totalElements,
    hasNext: totalPages > 0 ? page < totalPages - 1 : false,
    hasPrevious: page > 0,
  };
}

export async function getProducts(page = 0, size = 12, signal?: AbortSignal) {
  const payload = await requestJson<unknown>(`/api/products?page=${page}&size=${size}`, { signal });
  return normalizePage<Product>(payload, page, size);
}

export async function getFlashSaleProducts(signal?: AbortSignal) {
  const payload = await requestJson<unknown>("/api/products/flash-sale", { signal });
  return normalizePage<Product>(payload);
}

export async function getProductById(id: number, signal?: AbortSignal) {
  return requestJson<Product>(`/api/products/${id}`, { signal });
}

export async function searchProducts(keyword: string, page = 0, size = 12, signal?: AbortSignal) {
  const payload = await requestJson<unknown>(
    `/api/products/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`,
    { signal },
  );
  return normalizePage<Product>(payload, page, size);
}

export async function getCategories(signal?: AbortSignal) {
  const payload = await requestJson<unknown>("/api/categories", { signal });

  if (Array.isArray(payload)) {
    return payload.filter((value): value is string => typeof value === "string").map((value) => value.trim()).filter(Boolean);
  }

  if (payload && typeof payload === "object") {
    const candidate = (payload as { categories?: unknown }).categories;
    if (Array.isArray(candidate)) {
      return candidate.filter((value): value is string => typeof value === "string").map((value) => value.trim()).filter(Boolean);
    }
  }

  return [];
}

export async function getProductsByCategory(category: string, page = 0, size = 12, signal?: AbortSignal) {
  const payload = await requestJson<unknown>(
    `/api/products/category/${encodeURIComponent(category)}?page=${page}&size=${size}`,
    { signal },
  );
  return normalizePage<Product>(payload, page, size);
}
