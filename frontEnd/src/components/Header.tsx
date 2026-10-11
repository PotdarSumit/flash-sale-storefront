interface HeaderProps {
  onNavigate: (path: "/" | "/dashboard") => void;
  currentPath: "/" | "/dashboard";
}

export function Header({ onNavigate, currentPath }: HeaderProps) {
  return (
    <header className="absolute inset-x-0 top-0 z-20 px-4 pt-4 sm:px-6 lg:px-8">
      <div className="mx-auto flex max-w-7xl items-center justify-between rounded-full border border-white/10 bg-slate-950/35 px-4 py-3 text-white backdrop-blur-md">
        <button
          type="button"
          onClick={() => onNavigate("/")}
          className="flex items-center gap-3 text-left transition hover:opacity-90"
        >
          <span className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-white/15">
            <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
              <path d="M4 4h4l2.5 10h8.5l1.5-6H8.5" strokeLinecap="round" strokeLinejoin="round" />
              <circle cx="10" cy="20" r="1.5" fill="currentColor" stroke="none" />
              <circle cx="18" cy="20" r="1.5" fill="currentColor" stroke="none" />
            </svg>
          </span>
          <span className="leading-tight">
            <span className="block text-sm font-semibold uppercase tracking-[0.24em] text-white/70">Flash Sale Storefront</span>
            <span className="block text-base font-medium text-white">Flash Sale Storefront</span>
          </span>
        </button>

        <nav className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => onNavigate("/")}
            className={`rounded-full px-4 py-2 text-sm font-medium transition ${currentPath === "/" ? "bg-white text-slate-950" : "text-white/80 hover:bg-white/10"}`}
          >
            Shop
          </button>
          <button
            type="button"
            onClick={() => onNavigate("/dashboard")}
            className={`rounded-full px-4 py-2 text-sm font-medium transition ${currentPath === "/dashboard" ? "bg-white text-slate-950" : "text-white/80 hover:bg-white/10"}`}
          >
            Dashboard
          </button>
        </nav>
      </div>
    </header>
  );
}
