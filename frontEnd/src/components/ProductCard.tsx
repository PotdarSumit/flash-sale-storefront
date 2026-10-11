import type { Product } from "@/types/product";

interface ProductCardProps {
  product: Product;
  onBuy: (product: Product) => void;
}

function formatPrice(price: number) {
  return new Intl.NumberFormat("en-US", {
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
  }).format(price);
}

function getStockText(stock: number) {
  if (stock <= 0) {
    return "Sold out";
  }

  if (stock === 1) {
    return "1 unit left";
  }

  return `${stock} units available`;
}

export function ProductCard({ product, onBuy }: ProductCardProps) {
  const soldOut = product.stock <= 0;

  return (
    <article
      className={`flex h-full flex-col rounded-3xl border bg-white p-5 shadow-sm transition ${
        product.flashSale ? "border-amber-200 ring-1 ring-amber-100" : "border-slate-200"
      } ${soldOut ? "opacity-80" : "hover:-translate-y-0.5 hover:shadow-md"}`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">{product.category}</p>
            {product.flashSale ? (
              <span className="rounded-full bg-amber-100 px-2.5 py-1 text-xs font-semibold text-amber-700">Flash sale</span>
            ) : null}
          </div>
          <h3 className="text-lg font-semibold tracking-tight text-slate-950">{product.name}</h3>
        </div>

        <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-600">SKU {product.sku}</span>
      </div>

      <div className="mt-5 space-y-4">
        <div className="flex items-end justify-between gap-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">Price</p>
            <p className="text-2xl font-semibold text-slate-950">{formatPrice(product.price)}</p>
          </div>
          <div className="text-right">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">Stock</p>
            <p className={`text-sm font-medium ${soldOut ? "text-rose-600" : "text-slate-700"}`}>{getStockText(product.stock)}</p>
          </div>
        </div>

        <p className="text-sm text-slate-500">Updated {new Date(product.updatedAt).toLocaleString()}</p>

        <button
          type="button"
          onClick={() => onBuy(product)}
          disabled={soldOut}
          className="mt-auto inline-flex w-full items-center justify-center rounded-2xl bg-slate-950 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500"
        >
          {soldOut ? "Sold out" : product.flashSale ? "Buy flash-sale item" : "Buy now"}
        </button>
      </div>
    </article>
  );
}
