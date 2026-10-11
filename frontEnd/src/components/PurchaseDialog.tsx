import { useEffect, useState } from "react";
import type { Product } from "@/types/product";

interface PurchaseDialogProps {
  product: Product;
  customerIdentifier: string;
  onCustomerIdentifierChange: (value: string) => void;
  onClose: () => void;
  onConfirm: (payload: { customerIdentifier: string; quantity: number }) => Promise<void>;
  loading?: boolean;
  previewLoading?: boolean;
}

function formatPrice(price: number) {
  return new Intl.NumberFormat("en-US", {
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
  }).format(price);
}

export function PurchaseDialog({
  product,
  customerIdentifier,
  onCustomerIdentifierChange,
  onClose,
  onConfirm,
  loading,
  previewLoading,
}: PurchaseDialogProps) {
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    setQuantity(1);
  }, [product]);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 px-4 py-6 backdrop-blur-sm"
      onClick={onClose}
      role="presentation"
    >
      <div
        className="w-full max-w-lg rounded-3xl border border-slate-200 bg-white p-6 shadow-2xl"
        onClick={(event) => event.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="purchase-dialog-title"
      >
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-500">Confirm purchase</p>
            <h3 id="purchase-dialog-title" className="mt-1 text-xl font-semibold tracking-tight text-slate-950">
              {product.name}
            </h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full border border-slate-200 px-3 py-1.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
          >
            Close
          </button>
        </div>

        <div className="mt-5 space-y-4 text-sm text-slate-600">
          {previewLoading ? (
            <div className="rounded-2xl border border-sky-200 bg-sky-50 px-4 py-3 text-sky-900">
              Refreshing the current price and stock from the backend...
            </div>
          ) : null}
          <div className="flex items-center justify-between rounded-2xl bg-slate-50 px-4 py-3">
            <span>Displayed price</span>
            <span className="font-semibold text-slate-950">{formatPrice(product.price)}</span>
          </div>
          <div className="flex items-center justify-between rounded-2xl bg-slate-50 px-4 py-3">
            <span>SKU</span>
            <span className="font-semibold text-slate-950">{product.sku}</span>
          </div>
        </div>

        <div className="mt-5 space-y-4">
          <label className="block space-y-2">
            <span className="text-sm font-medium text-slate-700">Customer identifier</span>
            <input
              value={customerIdentifier}
              onChange={(event) => onCustomerIdentifierChange(event.target.value)}
              placeholder="demo-customer-001"
              className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-slate-400 focus:ring-4 focus:ring-slate-200"
            />
          </label>

          <p className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
            This is a demo identifier for the session. It is used for the storefront purchase request and is not authentication.
          </p>

          <label className="block space-y-2">
            <span className="text-sm font-medium text-slate-700">Quantity</span>
            <input
              type="number"
              min={1}
              max={product.flashSale ? 1 : Math.max(product.stock, 1)}
              value={quantity}
              disabled={product.flashSale || previewLoading || product.stock <= 0}
              onChange={(event) => {
                const nextValue = Number(event.target.value) || 1;
                const cappedValue = Math.min(Math.max(1, nextValue), product.flashSale ? 1 : Math.max(product.stock, 1));
                setQuantity(cappedValue);
              }}
              className="w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400 focus:ring-4 focus:ring-slate-200 disabled:bg-slate-50"
            />
          </label>

          {product.stock <= 0 ? (
            <p className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">This product is sold out and cannot be purchased.</p>
          ) : null}
        </div>

        <div className="mt-6 flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            className="rounded-2xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={() => onConfirm({ customerIdentifier: customerIdentifier.trim(), quantity })}
            disabled={loading || previewLoading || customerIdentifier.trim().length === 0 || product.stock <= 0}
            className="rounded-2xl bg-slate-950 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {loading ? "Submitting..." : "Confirm purchase"}
          </button>
        </div>
      </div>
    </div>
  );
}
