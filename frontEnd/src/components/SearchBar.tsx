interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onSubmit: (value: string) => void;
  onClear: () => void;
  loading?: boolean;
}

export function SearchBar({ value, onChange, onSubmit, onClear, loading }: SearchBarProps) {
  return (
    <form
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit(value);
      }}
      className="flex w-full flex-col gap-3 sm:flex-row"
    >
      <label className="flex-1">
        <span className="sr-only">Search products</span>
        <input
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder="Search products, SKUs, or categories"
          className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-400 focus:ring-4 focus:ring-slate-200"
        />
      </label>

      <div className="flex gap-3 sm:w-auto">
        <button
          type="submit"
          disabled={loading}
          className="inline-flex items-center justify-center rounded-2xl bg-slate-950 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-wait disabled:opacity-70"
        >
          {loading ? "Searching..." : "Search"}
        </button>
        <button
          type="button"
          onClick={onClear}
          className="inline-flex items-center justify-center rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
        >
          Clear
        </button>
      </div>
    </form>
  );
}
