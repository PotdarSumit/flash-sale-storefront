import { useEffect, useMemo, useState } from "react";
import { DashboardPage } from "@/pages/DashboardPage";
import { StorefrontPage } from "@/pages/StorefrontPage";

type Route = "/" | "/dashboard";

function getRoute(pathname: string): Route {
  return pathname.startsWith("/dashboard") ? "/dashboard" : "/";
}

export default function App() {
  const [route, setRoute] = useState<Route>(() => getRoute(window.location.pathname));

  useEffect(() => {
    const onPopState = () => setRoute(getRoute(window.location.pathname));
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);

  const navigate = useMemo(
    () => (path: Route) => {
      if (path === route) {
        return;
      }

      window.history.pushState({}, "", path);
      setRoute(path);
      window.scrollTo({ top: 0, behavior: "smooth" });
    },
    [route],
  );

  if (route === "/dashboard") {
    return <DashboardPage onNavigate={navigate} />;
  }

  return <StorefrontPage onNavigate={navigate} />;
}
