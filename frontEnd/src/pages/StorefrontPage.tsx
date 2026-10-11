import { useEffect, useMemo, useState } from "react";
import { ApiError } from "@/api/client";
import { getCategories, getFlashSaleProducts, getProductById, getProducts, getProductsByCategory, searchProducts } from "@/api/products";
import { purchaseProduct } from "@/api/orders";
import { AlertMessage } from "@/components/AlertMessage";
import { CategoryFilter } from "@/components/CategoryFilter";
import { Header } from "@/components/Header";
import { Pagination } from "@/components/Pagination";
import { ProductGrid } from "@/components/ProductGrid";
import { PurchaseDialog } from "@/components/PurchaseDialog";
import { SearchBar } from "@/components/SearchBar";
import type { PageResult, Product } from "@/types/product";

interface StorefrontPageProps {
  onNavigate: (path: "/" | "/dashboard") => void;
}

const PAGE_SIZE = 12;
const STORAGE_KEY = "flash-sale-storefront.customerIdentifier";

function generateDemoCustomerId() {
  return `demo-customer-${Math.random().toString(36).slice(2, 6)}-${Date.now().toString(36)}`;
}

function formatError(error: unknown) {
  if (error instanceof ApiError) {
    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "An unexpected error occurred.";
}

export function StorefrontPage({ onNavigate }: StorefrontPageProps) {
  const [products, setProducts] = useState<Product[]>([]);
  const [flashSaleProducts, setFlashSaleProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [pageState, setPageState] = useState<PageResult<Product>>({
    items: [],
    page: 0,
    size: PAGE_SIZE,
    totalPages: 0,
    totalElements: 0,
    hasNext: false,
    hasPrevious: false,
  });

  const [searchInput, setSearchInput] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [loadingCatalog, setLoadingCatalog] = useState(true);
  const [loadingFlashSale, setLoadingFlashSale] = useState(true);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [catalogError, setCatalogError] = useState("");
  const [flashSaleError, setFlashSaleError] = useState("");
  const [categoriesError, setCategoriesError] = useState("");
  const [purchaseError, setPurchaseError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [purchaseLoading, setPurchaseLoading] = useState(false);
  const [purchasePreviewLoading, setPurchasePreviewLoading] = useState(false);
  const [customerIdentifier, setCustomerIdentifier] = useState(() => {
    if (typeof window === "undefined") {
      return generateDemoCustomerId();
    }

    return window.localStorage.getItem(STORAGE_KEY) || generateDemoCustomerId();
  });

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, customerIdentifier);
  }, [customerIdentifier]);

  useEffect(() => {
    if (!selectedProduct) {
      setPurchasePreviewLoading(false);
      return;
    }

    const controller = new AbortController();
    setPurchasePreviewLoading(true);

    void getProductById(selectedProduct.id, controller.signal)
      .then((latestProduct) => {
        if (!controller.signal.aborted) {
          setSelectedProduct(latestProduct);
        }
      })
      .catch((error) => {
        if (!controller.signal.aborted) {
          setPurchaseError(formatError(error));
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setPurchasePreviewLoading(false);
        }
      });

    return () => controller.abort();
  }, [selectedProduct?.id]);

  useEffect(() => {
    const controller = new AbortController();

    async function loadFlashSale() {
      setLoadingFlashSale(true);
      setFlashSaleError("");

      try {
        const result = await getFlashSaleProducts(controller.signal);
        setFlashSaleProducts(result.items.filter((product) => product.stock > 0 || product.stock === 0));
      } catch (error) {
        if (!controller.signal.aborted) {
          setFlashSaleError(formatError(error));
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoadingFlashSale(false);
        }
      }
    }

    async function loadCategories() {
      setLoadingCategories(true);
      setCategoriesError("");

      try {
        const result = await getCategories(controller.signal);
        setCategories(result);
      } catch (error) {
        if (!controller.signal.aborted) {
          setCategoriesError(formatError(error));
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoadingCategories(false);
        }
      }
    }

    void Promise.all([loadFlashSale(), loadCategories()]);

    return () => controller.abort();
  }, []);

  useEffect(() => {
    const controller = new AbortController();

    async function loadCatalog() {
      setLoadingCatalog(true);
      setCatalogError("");
      setSuccessMessage("");
      setPurchaseError("");

      try {
        let result: PageResult<Product>;

        if (searchTerm.trim().length > 0) {
          result = await searchProducts(searchTerm.trim(), pageState.page, PAGE_SIZE, controller.signal);
        } else if (selectedCategory !== "All") {
          result = await getProductsByCategory(selectedCategory, pageState.page, PAGE_SIZE, controller.signal);
        } else {
          result = await getProducts(pageState.page, PAGE_SIZE, controller.signal);
        }

        setProducts(result.items);
        setPageState((current) => ({ ...current, ...result, size: PAGE_SIZE }));
      } catch (error) {
        if (!controller.signal.aborted) {
          setCatalogError(formatError(error));
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoadingCatalog(false);
        }
      }
    }

    void loadCatalog();

    return () => controller.abort();
  }, [pageState.page, searchTerm, selectedCategory]);

  const activeModeText = useMemo(() => {
    if (searchTerm.trim().length > 0) {
      return `Search results for "${searchTerm.trim()}"`;
    }

    if (selectedCategory !== "All") {
      return `Category: ${selectedCategory}`;
    }

    return "All products";
  }, [searchTerm, selectedCategory]);

  async function handlePurchase(product: Product, quantity: number, overrideCustomerIdentifier?: string) {
    const identifier = (overrideCustomerIdentifier || customerIdentifier).trim() || generateDemoCustomerId();

    await purchaseProduct(
      {
        productId: product.id,
        quantity: product.flashSale ? 1 : quantity,
        customerIdentifier: identifier,
        displayedPrice: product.price,
      },
    );

    setCustomerIdentifier(identifier);
    setSelectedProduct(null);
    setPurchaseError("");
    setSuccessMessage(`Purchase confirmed for ${product.name}. Stock has been refreshed from the backend.`);

    const controller = new AbortController();
    try {
      const [flashSaleResult, catalogResult] = await Promise.all([
        getFlashSaleProducts(controller.signal),
        searchTerm.trim().length > 0
          ? searchProducts(searchTerm.trim(), pageState.page, PAGE_SIZE, controller.signal)
          : selectedCategory !== "All"
            ? getProductsByCategory(selectedCategory, pageState.page, PAGE_SIZE, controller.signal)
            : getProducts(pageState.page, PAGE_SIZE, controller.signal),
      ]);

      setFlashSaleProducts(flashSaleResult.items);
      setProducts(catalogResult.items);
      setPageState((current) => ({ ...current, ...catalogResult, size: PAGE_SIZE }));
    } catch (error) {
      setCatalogError(formatError(error));
    }
  }

  function openPurchaseDialog(product: Product) {
    setPurchaseError("");
    setSuccessMessage("");
    setSelectedProduct(product);
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <Header onNavigate={onNavigate} currentPath="/" />

      <main>
        <section className="relative isolate min-h-[84vh] overflow-hidden">
          <img
            src="/images/flash-sale-hero.jpg"
            alt="Bright grocery storefront with flash sale displays"
            className="absolute inset-0 h-full w-full object-cover"
          />
          <div className="absolute inset-0 bg-gradient-to-r from-slate-950/85 via-slate-950/55 to-slate-950/20" />

          <div className="relative mx-auto flex min-h-[84vh] max-w-7xl flex-col justify-end px-4 pb-14 pt-28 sm:px-6 lg:px-8">
            <div className="max-w-3xl text-white">
              <p className="text-xs font-semibold uppercase tracking-[0.3em] text-white/70">Flash Sale Storefront</p>
              <h1 className="mt-4 text-5xl font-semibold tracking-tight sm:text-6xl">Flash Sale Storefront</h1>
              <p className="mt-5 max-w-2xl text-base leading-7 text-white/80 sm:text-lg">
                Shop live inventory, catch flash-sale drops, and place orders against the backend in real time. Search and category filters call the actual storefront APIs.
              </p>

              <div className="mt-8 flex flex-wrap gap-3">
                <a
                  href="#flash-sale"
                  className="inline-flex items-center justify-center rounded-full bg-white px-5 py-3 text-sm font-semibold text-slate-950 transition hover:bg-slate-100"
                >
                  Shop flash sale
                </a>
                <button
                  type="button"
                  onClick={() => onNavigate("/dashboard")}
                  className="inline-flex items-center justify-center rounded-full border border-white/20 bg-white/10 px-5 py-3 text-sm font-semibold text-white backdrop-blur-sm transition hover:bg-white/20"
                >
                  Open dashboard
                </button>
              </div>
            </div>
          </div>
        </section>

        <section className="border-b border-slate-200 bg-white/80 px-4 py-8 backdrop-blur sm:px-6 lg:px-8">
          <div className="mx-auto max-w-7xl space-y-4">
            <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_280px]">
              <SearchBar
                value={searchInput}
                onChange={setSearchInput}
                onSubmit={(value) => {
                  setSearchTerm(value.trim());
                  setPageState((current) => ({ ...current, page: 0 }));
                }}
                onClear={() => {
                  setSearchInput("");
                  setSearchTerm("");
                  setPageState((current) => ({ ...current, page: 0 }));
                }}
                loading={loadingCatalog}
              />

              <CategoryFilter
                categories={categories}
                value={selectedCategory}
                onChange={(value) => {
                    setSelectedCategory(value);
                    setSearchInput("");
                    setSearchTerm("");
                    setPageState((current) => ({ ...current, page: 0 }));
                }}
                loading={loadingCategories}
              />
            </div>

            <p className="text-sm text-slate-500">
              Search takes precedence over the category filter. Clear search to return to category browsing.
            </p>

            {categoriesError ? <AlertMessage tone="error" title="Category load failed" message={categoriesError} /> : null}
            {flashSaleError ? <AlertMessage tone="error" title="Flash-sale load failed" message={flashSaleError} /> : null}
            {catalogError ? <AlertMessage tone="error" title="Catalog load failed" message={catalogError} /> : null}
            {purchaseError ? <AlertMessage tone="error" title="Purchase failed" message={purchaseError} /> : null}
            {successMessage ? <AlertMessage tone="success" title="Purchase confirmed" message={successMessage} /> : null}
          </div>
        </section>

        <section id="flash-sale" className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
          <div className="mb-6 flex items-end justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-500">Flash sale</p>
              <h2 className="mt-1 text-2xl font-semibold tracking-tight text-slate-950">Limited-time items</h2>
            </div>
            <p className="text-sm text-slate-500">{loadingFlashSale ? "Refreshing sale items..." : `${flashSaleProducts.length} active item(s)`}</p>
          </div>

          <ProductGrid
            title="Flash sale products"
            products={flashSaleProducts}
            emptyMessage="No flash-sale products are available right now."
            loading={loadingFlashSale}
            onBuy={openPurchaseDialog}
          />
        </section>

        <section className="mx-auto max-w-7xl px-4 pb-16 sm:px-6 lg:px-8">
          <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-500">Catalog</p>
              <h2 className="mt-1 text-2xl font-semibold tracking-tight text-slate-950">{activeModeText}</h2>
            </div>
            <p className="text-sm text-slate-500">
              Showing {products.length} item{products.length === 1 ? "" : "s"} on page {pageState.page + 1}
            </p>
          </div>

          <ProductGrid
            title="Browse products"
            products={products}
            emptyMessage={searchTerm.trim().length > 0 ? `No products matched "${searchTerm.trim()}".` : "No products found for the selected category."}
            loading={loadingCatalog}
            onBuy={openPurchaseDialog}
          />

          <Pagination
            page={pageState.page}
            totalPages={pageState.totalPages}
            onPageChange={(nextPage) => {
              if (nextPage < 0 || nextPage === pageState.page) {
                return;
              }
              setPageState((current) => ({ ...current, page: nextPage }));
            }}
            loading={loadingCatalog}
          />
        </section>
      </main>

      {selectedProduct ? (
        <PurchaseDialog
          product={selectedProduct}
          customerIdentifier={customerIdentifier}
          onCustomerIdentifierChange={setCustomerIdentifier}
          onClose={() => {
            setPurchaseError("");
            setSelectedProduct(null);
          }}
          loading={purchaseLoading}
          previewLoading={purchasePreviewLoading}
          onConfirm={async ({ customerIdentifier: identifier, quantity }) => {
            setPurchaseLoading(true);
            try {
              await handlePurchase(selectedProduct, quantity, identifier);
            } catch (error) {
              setPurchaseError(formatError(error));
            } finally {
              setPurchaseLoading(false);
            }
          }}
        />
      ) : null}
    </div>
  );
}
