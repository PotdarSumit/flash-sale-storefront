
import { useEffect, useState } from "react";

interface DashboardPageProps {
  onNavigate: (path: "/" | "/dashboard") => void;
}

interface DashboardMetrics {
  totalProducts: number;
  flashSaleProducts: number;
  soldOutProducts: number;
  productsWithPendingWarehouseQuantity: number;
  pendingOrders: number;
  unsyncedOrders: number;
}

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const metricCards: {
  key: keyof DashboardMetrics;
  label: string;
  description: string;
}[] = [
  {
    key: "totalProducts",
    label: "Total products",
    description: "Products in the storefront database",
  },
  {
    key: "flashSaleProducts",
    label: "Flash-sale products",
    description: "Products marked for flash sales",
  },
  {
    key: "soldOutProducts",
    label: "Sold-out products",
    description: "Products with zero or negative storefront stock",
  },
  {
    key: "productsWithPendingWarehouseQuantity",
    label: "Awaiting reconciliation",
    description: "Products with outstanding warehouse quantities",
  },
  {
    key: "pendingOrders",
    label: "Pending orders",
    description: "Orders with PENDING status",
  },
  {
    key: "unsyncedOrders",
    label: "Awaiting warehouse batch",
    description: "Unbatched orders not yet synchronized",
  },
];

export function DashboardPage({ onNavigate }: DashboardPageProps) {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  useEffect(() => {
    let active = true;

    async function loadMetrics() {
      try {
        const response = await fetch(
          `${API_BASE_URL}/api/dashboard/metrics`
        );

        if (!response.ok) {
          throw new Error(`Dashboard API returned HTTP ${response.status}`);
        }

        const data: DashboardMetrics = await response.json();

        if (active) {
          setMetrics(data);
          setError(null);
          setLastUpdated(new Date());
        }
      } catch (err) {
        if (active) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load dashboard metrics."
          );
        }
      }
    }

    void loadMetrics();

    const intervalId = window.setInterval(() => {
      void loadMetrics();
    }, 5000);

    return () => {
      active = false;
      window.clearInterval(intervalId);
    };
  }, []);

  return (
    <div className="min-h-screen bg-slate-950 text-white">
      <header className="border-b border-white/10 px-4 py-5 sm:px-6 lg:px-8">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4">
          <button
            type="button"
            onClick={() => onNavigate("/")}
            className="text-left transition hover:opacity-90"
          >
            <p className="text-xs font-semibold uppercase tracking-[0.22em] text-white/60">
              Flash Sale Storefront
            </p>
            <h1 className="mt-1 text-2xl font-semibold tracking-tight">
              Protected dashboard
            </h1>
          </button>

          <button
            type="button"
            onClick={() => onNavigate("/")}
            className="rounded-full border border-white/15 px-4 py-2 text-sm font-medium text-white/85 transition hover:bg-white/10"
          >
            Back to store
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
        <section className="mb-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.22em] text-white/50">
                Live backend data
              </p>
              <h2 className="mt-2 text-3xl font-semibold tracking-tight">
                System overview
              </h2>
              <p className="mt-2 text-sm text-white/60">
                Metrics refresh automatically every 5 seconds.
              </p>
            </div>

            <div className="rounded-full border border-white/10 px-4 py-2 text-sm text-white/70">
              {lastUpdated
                ? `Updated ${lastUpdated.toLocaleTimeString()}`
                : "Waiting for data"}
            </div>
          </div>
        </section>

        {error && (
          <div
            role="alert"
            className="mb-6 rounded-2xl border border-red-400/30 bg-red-400/10 p-4 text-sm text-red-200"
          >
            <p className="font-semibold">Unable to refresh dashboard</p>
            <p className="mt-1">{error}</p>
            <p className="mt-2 text-red-200/75">
              Check that the backend is running and the frontend can access
              the dashboard API.
            </p>
          </div>
        )}

        {!metrics && !error && (
          <div className="rounded-2xl border border-white/10 bg-white/5 p-8 text-white/70">
            Loading dashboard metrics...
          </div>
        )}

        {metrics && (
          <>
            <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {metricCards.map((card) => (
                <article
                  key={card.key}
                  className="rounded-2xl border border-white/10 bg-white/5 p-6 transition hover:border-white/20"
                >
                  <p className="text-sm font-medium text-white/65">
                    {card.label}
                  </p>
                  <p className="mt-3 text-4xl font-semibold tracking-tight tabular-nums">
                    {metrics[card.key].toLocaleString()}
                  </p>
                  <p className="mt-3 text-sm leading-6 text-white/45">
                    {card.description}
                  </p>
                </article>
              ))}
            </section>

            <section className="mt-8 rounded-2xl border border-white/10 bg-white/5 p-6">
              <h3 className="text-lg font-semibold">Requirement monitoring</h3>
              <p className="mt-2 text-sm leading-6 text-white/60">
                These inventory and order totals are live. Performance
                requirements still need dedicated measurements and tests.
              </p>

              <div className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {[
                  ["R3", "Database reads limited to 25/sec"],
                  ["R4", "Stock and price freshness within 5 sec"],
                  ["R5", "No overselling under concurrency"],
                  ["R6", "Two backend instances behind a load balancer"],
                  ["R7", "One flash-sale unit per customer"],
                  ["R1/R2", "Load simulation and measured report"],
                ].map(([requirement, description]) => (
                  <div
                    key={requirement}
                    className="rounded-xl border border-white/10 bg-black/20 p-4"
                  >
                    <p className="text-sm font-semibold">{requirement}</p>
                    <p className="mt-2 text-sm leading-5 text-white/55">
                      {description}
                    </p>
                   <p className="mt-3 text-xs font-medium text-slate-400">
                     Awaiting test results
                   </p>
                  </div>
                ))}
              </div>
            </section>
          </>
        )}
      </main>
    </div>
  );
}
