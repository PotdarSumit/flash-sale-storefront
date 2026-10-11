interface CategoryFilterProps {
  categories: string[];
  value: string;
  onChange: (value: string) => void;
  loading?: boolean;
}

export function CategoryFilter({ categories, value, onChange, loading }: CategoryFilterProps) {
  return (
    <label className="flex flex-col gap-2">
      <span className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">Category</span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400 focus:ring-4 focus:ring-slate-200 disabled:cursor-wait disabled:bg-slate-50"
        disabled={loading || categories.length === 0}
      >
        <option value="All">All categories</option>
        {categories.map((category) => (
          <option key={category} value={category}>
            {category}
          </option>
        ))}
      </select>
    </label>
  );
}
