interface PaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  loading?: boolean;
}

function getVisiblePages(page: number, totalPages: number) {
  const pages: number[] = [];
  const start = Math.max(0, page - 2);
  const end = Math.min(totalPages - 1, page + 2);

  for (let index = start; index <= end; index += 1) {
    pages.push(index);
  }

  return pages;
}

export function Pagination({ page, totalPages, onPageChange, loading }: PaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  const visiblePages = getVisiblePages(page, totalPages);

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-t border-slate-200 pt-6">
      <p className="text-sm text-slate-600">
        Page <span className="font-semibold text-slate-900">{page + 1}</span> of <span className="font-semibold text-slate-900">{totalPages}</span>
      </p>

      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0 || loading}
          className="rounded-full border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Previous
        </button>

        {visiblePages[0] > 0 ? <span className="px-2 text-slate-400">...</span> : null}
        {visiblePages.map((value) => (
          <button
            key={value}
            type="button"
            onClick={() => onPageChange(value)}
            disabled={loading}
            className={`h-10 min-w-10 rounded-full px-3 text-sm font-semibold transition ${
              value === page ? "bg-slate-950 text-white" : "border border-slate-200 text-slate-700 hover:bg-slate-50"
            }`}
          >
            {value + 1}
          </button>
        ))}
        {visiblePages[visiblePages.length - 1] < totalPages - 1 ? <span className="px-2 text-slate-400">...</span> : null}

        <button
          type="button"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1 || loading}
          className="rounded-full border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  );
}
