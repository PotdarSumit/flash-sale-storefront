import type { Product } from "@/types/product";
import { ProductCard } from "@/components/ProductCard";

interface ProductGridProps {
  title: string;
  products: Product[];
  emptyMessage: string;
  loading?: boolean;
  onBuy: (product: Product) => void;
}

export function ProductGrid({ title, products, emptyMessage, loading, onBuy }: ProductGridProps) {
  return (
    <section className="space-y-5">
      <div className="flex items-end justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-500">Store selection</p>
          <h2 className="mt-1 text-2xl font-semibold tracking-tight text-slate-950">{title}</h2>
        </div>
        <p className="text-sm text-slate-500">Current inventory from the live backend</p>
      </div>

      {loading ? (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <div key={index} className="h-72 animate-pulse rounded-3xl border border-slate-200 bg-slate-100/80" />
          ))}
        </div>
      ) : products.length === 0 ? (
        <div className="rounded-3xl border border-dashed border-slate-200 bg-white px-6 py-10 text-center text-slate-600">
          {emptyMessage}
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} onBuy={onBuy} />
          ))}
        </div>
      )}
    </section>
  );
}
