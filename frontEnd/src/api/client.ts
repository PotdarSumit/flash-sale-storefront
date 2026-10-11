import { cn } from "@/utils/cn";

export class ApiError extends Error {
  status: number;
  details?: string;

  constructor(status: number, message: string, details?: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details;
  }
}

function getBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL || "";
}

function normalizeErrorMessage(status: number, fallback: string) {
  switch (status) {
    case 400:
      return "The backend rejected the request as invalid.";
    case 404:
      return "The requested product or endpoint was not found.";
    case 409:
      return "The purchase could not be completed because inventory changed.";
    case 429:
      return "Too many requests. Please wait a moment and try again.";
    case 500:
      return "The backend returned an internal error.";
    case 503:
      return "The backend is temporarily unavailable.";
    default:
      return fallback || `Request failed with status ${status}.`;
  }
}

async function readErrorMessage(response: Response) {
  const contentType = response.headers.get("content-type") || "";

  try {
    if (contentType.includes("application/json")) {
      const body = (await response.json()) as unknown;

      if (body && typeof body === "object") {
        const candidate =
          (body as { message?: unknown }).message ??
          (body as { error?: unknown }).error ??
          (body as { detail?: unknown }).detail;

        if (typeof candidate === "string" && candidate.trim().length > 0) {
          return candidate.trim();
        }

        return JSON.stringify(body);
      }
    }

    const text = await response.text();
    return text.trim();
  } catch {
    return "";
  }
}

export async function requestJson<T>(path: string, init: RequestInit = {}): Promise<T> {
  const timeoutController = new AbortController();
  const timeout = window.setTimeout(() => timeoutController.abort(), 20000);
  const headers = new Headers(init.headers);

  if (init.signal) {
    if (init.signal.aborted) {
      timeoutController.abort();
    } else {
      init.signal.addEventListener("abort", () => timeoutController.abort(), { once: true });
    }
  }

  if (!headers.has("Accept")) {
    headers.set("Accept", "application/json");
  }

  if (init.body && !headers.has("Content-Type") && !(init.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  try {
    const response = await fetch(`${getBaseUrl()}${path}`, {
      ...init,
      headers,
      signal: timeoutController.signal,
    });

    if (!response.ok) {
      const details = await readErrorMessage(response);
      throw new ApiError(
        response.status,
        details || normalizeErrorMessage(response.status, response.statusText),
        details || undefined,
      );
    }

    if (response.status === 204) {
      return undefined as T;
    }

    const contentType = response.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
      return (await response.json()) as T;
    }

    const text = await response.text();
    return text as T;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }

    if (error instanceof DOMException && error.name === "AbortError") {
      throw new ApiError(0, "The request timed out.");
    }

    const message = error instanceof Error ? error.message : "Network request failed.";
    throw new ApiError(0, message);
  } finally {
    window.clearTimeout(timeout);
  }
}

export function joinClassNames(...inputs: Array<string | false | null | undefined>) {
  return cn(...inputs);
}
