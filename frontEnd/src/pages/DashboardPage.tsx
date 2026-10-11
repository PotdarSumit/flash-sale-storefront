
import { useCallback, useEffect, useState } from "react";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

type Metrics = Record<string, string | number | boolean | null>;

type LoadTestResult = {
  status?: string;
  message?: string;
  durationSeconds?: number;
  requestsPerSecond?: number;
  totalRequests?: number;
  successfulRequests?: number;
  failedRequests?: number;
  p95LatencyMs?: number;
  databaseReadsPerSecond?: number;
  r3Passed?: boolean | null;
  r4Passed?: boolean | null;
  r5Passed?: boolean | null;
};

export  function DashboardPage() {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("");
  const [authHeader, setAuthHeader] = useState("");
  const [metrics, setMetrics] = useState<Metrics | null>(null);
  const [loadTestResult, setLoadTestResult] =
    useState<LoadTestResult | null>(null);
  const [loadingMetrics, setLoadingMetrics] = useState(false);
  const [runningTest, setRunningTest] = useState(false);
  const [error, setError] = useState("");
  const [loginMessage, setLoginMessage] = useState("");

  const fetchMetrics = useCallback(async (header: string) => {
    setLoadingMetrics(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/dashboard/metrics`, {
        method: "GET",
        headers: {
          Authorization: header,
          Accept: "application/json",
        },
      });

      if (response.status === 401 || response.status === 403) {
        throw new Error(
          "Authentication failed. Check your dashboard username and password."
        );
      }

      if (!response.ok) {
        throw new Error(`Could not load metrics. HTTP ${response.status}`);
      }

      const data: Metrics = await response.json();
      setMetrics(data);
      setError("");
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Could not connect to the backend."
      );
    } finally {
      setLoadingMetrics(false);
    }
  }, []);

  useEffect(() => {
    if (!authHeader) return;

    void fetchMetrics(authHeader);

    const intervalId = window.setInterval(() => {
      void fetchMetrics(authHeader);
    }, 5000);

    return () => window.clearInterval(intervalId);
  }, [authHeader, fetchMetrics]);

  function handleLogin(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setLoginMessage("");

    if (!username.trim() || !password) {
      setLoginMessage("Enter your username and password.");
      return;
    }

    const header = `Basic ${btoa(`${username.trim()}:${password}`)}`;
    setAuthHeader(header);
    setLoginMessage("Checking credentials...");
    void fetchMetrics(header).then(() => {
      setLoginMessage("");
    });
  }

  function handleLogout() {
    setAuthHeader("");
    setPassword("");
    setMetrics(null);
    setLoadTestResult(null);
    setError("");
    setLoginMessage("");
  }

  async function runLoadTest() {
    if (!authHeader) {
      setError("Log in to the dashboard before running the load test.");
      return;
    }

    const confirmed = window.confirm(
      "Run the 2-minute load test? This will generate requests against your backend."
    );

    if (!confirmed) return;

    setRunningTest(true);
    setError("");
    setLoadTestResult(null);

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/dashboard/load-test`,
        {
          method: "POST",
          headers: {
            Authorization: authHeader,
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          body: JSON.stringify({ durationSeconds: 120 }),
        }
      );

      const responseText = await response.text();

      if (!response.ok) {
        throw new Error(
          `Load test failed to start (HTTP ${response.status}). ${
            responseText || "Check that the backend endpoint exists."
          }`
        );
      }

      const result = JSON.parse(responseText) as LoadTestResult;
      setLoadTestResult(result);
      await fetchMetrics(authHeader);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "An unexpected error occurred while running the load test."
      );
    } finally {
      setRunningTest(false);
    }
  }

  function displayValue(value: unknown): string {
    if (value === null || value === undefined) return "—";
    if (typeof value === "number") return value.toLocaleString();
    return String(value);
  }

  const metricLabels: Record<string, string> = {
    totalProducts: "Total products",
    flashSaleProducts: "Flash-sale products",
    soldOutProducts: "Sold-out products",
    productsWithPendingWarehouseQuantity:
      "Products with pending warehouse quantity",
    pendingOrders: "Pending orders",
    unsyncedOrders: "Unsynced orders",
    databaseReadPermitsGranted: "Database read permits granted",
    databaseReadRequestsRejected: "Database read requests rejected",
    databaseProductReadsPerSecond: "Database product reads/sec",
    r3MonitoringStatus: "R3 monitoring status",
  };

  const resultLabels: [keyof LoadTestResult, string][] = [
    ["status", "Overall status"],
    ["message", "Message"],
    ["durationSeconds", "Duration (seconds)"],
    ["requestsPerSecond", "Requests per second"],
    ["totalRequests", "Total requests"],
    ["successfulRequests", "Successful requests"],
    ["failedRequests", "Failed requests"],
    ["p95LatencyMs", "P95 latency (ms)"],
    ["databaseReadsPerSecond", "Database reads per second"],
    ["r3Passed", "R3 — Database reads ≤ 25/sec"],
    ["r4Passed", "R4 — Inventory freshness"],
    ["r5Passed", "R5 — No overselling"],
  ];

  function formatResultValue(value: unknown): string {
    if (value === true) return "PASS";
    if (value === false) return "FAIL";
    if (value === null || value === undefined) return "NOT REPORTED";
    return displayValue(value);
  }

  return (
    <main
      style={{
        maxWidth: 1100,
        margin: "0 auto",
        padding: 24,
        fontFamily: "Arial, sans-serif",
        color: "#202124",
      }}
    >
      <header
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          flexWrap: "wrap",
          gap: 12,
        }}
      >
        <div>
          <h1 style={{ marginBottom: 6 }}>Flash Sale Storefront Dashboard</h1>
          <p style={{ color: "#666", marginTop: 0 }}>
            Inventory, orders, database reads, and load-test results
          </p>
        </div>

        {authHeader && (
          <button onClick={handleLogout} style={secondaryButtonStyle}>
            Log out
          </button>
        )}
      </header>

      {!authHeader && (
        <section style={panelStyle}>
          <h2>Dashboard Login</h2>
          <form onSubmit={handleLogin}>
            <label style={labelStyle} htmlFor="dashboard-username">
              Username
            </label>
            <input
              id="dashboard-username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              style={inputStyle}
            />

            <label style={labelStyle} htmlFor="dashboard-password">
              Password
            </label>
            <input
              id="dashboard-password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              style={inputStyle}
            />

            <button type="submit" style={primaryButtonStyle}>
              Log in
            </button>
          </form>
          {loginMessage && <p>{loginMessage}</p>}
        </section>
      )}

      {error && (
        <div
          role="alert"
          style={{
            background: "#fff0f0",
            border: "1px solid #d93025",
            color: "#a50e0e",
            padding: 12,
            borderRadius: 8,
            margin: "16px 0",
            overflowWrap: "anywhere",
          }}
        >
          {error}
        </div>
      )}

      <section style={panelStyle}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            flexWrap: "wrap",
            gap: 12,
          }}
        >
          <div>
            <h2 style={{ marginBottom: 6 }}>System Metrics</h2>
            <p style={{ color: "#666", marginTop: 0 }}>
              {authHeader
                ? "Refreshes automatically every 5 seconds."
                : "Log in to view protected metrics."}
            </p>
          </div>

          <button
            onClick={() => authHeader && void fetchMetrics(authHeader)}
            disabled={!authHeader || loadingMetrics}
            style={secondaryButtonStyle}
          >
            {loadingMetrics ? "Refreshing..." : "Refresh Metrics"}
          </button>
        </div>

        {metrics && (
          <div style={gridStyle}>
            {Object.entries(metrics).map(([key, value]) => (
              <article key={key} style={metricCardStyle}>
                <div style={{ fontSize: 13, color: "#666" }}>
                  {metricLabels[key] ?? key}
                </div>
                <div
                  style={{
                    fontSize: 23,
                    fontWeight: 700,
                    marginTop: 10,
                    overflowWrap: "anywhere",
                  }}
                >
                  {displayValue(value)}
                </div>
              </article>
            ))}
          </div>
        )}

        {!metrics && (
          <p style={{ color: "#666" }}>
            {authHeader
              ? "Loading dashboard metrics..."
              : "Metrics will appear here after login."}
          </p>
        )}
      </section>

      <section style={panelStyle}>
        <h2>Load Test</h2>
        <p style={{ color: "#555" }}>
          Start a 120-second test and display the results returned by the
          backend. Run this against a test environment because it generates
          application traffic.
        </p>

        <button
          type="button"
          onClick={() => void runLoadTest()}
          disabled={!authHeader || runningTest}
          style={{
            ...primaryButtonStyle,
            opacity: !authHeader || runningTest ? 0.6 : 1,
            cursor: !authHeader || runningTest ? "not-allowed" : "pointer",
          }}
        >
          {runningTest
            ? "Running Load Test..."
            : "Run 2-Minute Load Test"}
        </button>

        {!authHeader && (
          <p style={{ color: "#666" }}>
            Log in to enable the load-test button.
          </p>
        )}

        {runningTest && (
          <p role="status">
            The request was submitted. Wait for the backend to return the
            result; a full test may take two minutes.
          </p>
        )}

        {loadTestResult && (
          <div style={{ marginTop: 20 }}>
            <h3>Load Test Results</h3>
            <div style={gridStyle}>
              {resultLabels.map(([key, label]) => {
                const value = loadTestResult[key];

                if (value === undefined) return null;

                const isFailure = value === false;
                const isPass = value === true;

                return (
                  <article
                    key={key}
                    style={{
                      ...metricCardStyle,
                      borderColor: isFailure
                        ? "#d93025"
                        : isPass
                          ? "#188038"
                          : "#ddd",
                    }}
                  >
                    <div style={{ fontSize: 13, color: "#666" }}>{label}</div>
                    <div
                      style={{
                        fontWeight: 700,
                        marginTop: 10,
                        overflowWrap: "anywhere",
                        color: isFailure
                          ? "#d93025"
                          : isPass
                            ? "#188038"
                            : "#202124",
                      }}
                    >
                      {formatResultValue(value)}
                    </div>
                  </article>
                );
              })}
            </div>
          </div>
        )}
      </section>
    </main>
  );
}

const panelStyle: React.CSSProperties = {
  border: "1px solid #ddd",
  borderRadius: 12,
  padding: 20,
  marginTop: 20,
  background: "#fff",
};

const gridStyle: React.CSSProperties = {
  display: "grid",
  gridTemplateColumns: "repeat(auto-fit, minmax(190px, 1fr))",
  gap: 12,
  marginTop: 16,
};

const metricCardStyle: React.CSSProperties = {
  border: "1px solid #ddd",
  borderRadius: 8,
  padding: 16,
  minWidth: 0,
};

const labelStyle: React.CSSProperties = {
  display: "block",
  marginTop: 12,
  marginBottom: 6,
  fontWeight: 600,
};

const inputStyle: React.CSSProperties = {
  display: "block",
  boxSizing: "border-box",
  width: "100%",
  maxWidth: 450,
  padding: 10,
  border: "1px solid #bbb",
  borderRadius: 6,
  marginBottom: 8,
  fontSize: 15,
};

const primaryButtonStyle: React.CSSProperties = {
  background: "#175cd3",
  color: "#fff",
  border: "none",
  borderRadius: 7,
  padding: "12px 18px",
  fontSize: 15,
  fontWeight: 700,
  cursor: "pointer",
  marginTop: 8,
};

const secondaryButtonStyle: React.CSSProperties = {
  background: "#fff",
  color: "#202124",
  border: "1px solid #bbb",
  borderRadius: 7,
  padding: "10px 14px",
  fontSize: 14,
  cursor: "pointer",
};
