# Flash Sale Storefront

Responsive React + Vite frontend for the flash-sale storefront backend.

## Folder Structure

```text
src/
├── api/
│   ├── client.ts
│   ├── orders.ts
│   └── products.ts
├── components/
│   ├── AlertMessage.tsx
│   ├── CategoryFilter.tsx
│   ├── Header.tsx
│   ├── Pagination.tsx
│   ├── ProductCard.tsx
│   ├── ProductGrid.tsx
│   ├── PurchaseDialog.tsx
│   └── SearchBar.tsx
├── pages/
│   ├── DashboardPage.tsx
│   └── StorefrontPage.tsx
├── types/
│   └── product.ts
├── App.tsx
├── index.css
└── main.tsx
```

## Environment

Copy `.env.example` to `.env` if you need to override the backend URLs.

## Run Instructions in Windows PowerShell

```powershell
npm install
npm run dev
```

Vite normally serves the app at `http://localhost:5173/` unless that port is already taken.

## Backend Endpoints Used

- `GET /api/products?page=0&size=20`
- `GET /api/products/flash-sale`
- `GET /api/products/{id}`
- `GET /api/products/search?keyword=milk&page=0&size=20`
- `GET /api/categories`
- `GET /api/products/category/{category}?page=0&size=20`
- `POST /api/orders`

## Assumptions That Still Need Verification

- The order endpoint path was set to `/api/orders` based on the user-provided local test. The backend source was not present in this repository, so the class-level `@RequestMapping` could not be verified here.
- Spring `Page` responses were handled defensively because the exact JSON shape was not available in source.
- The dashboard metrics and simulation endpoints were not implemented because no verified backend endpoints were available in this project.

## Manual Test Checklist

- Load the homepage and confirm the hero, search, categories, flash-sale section, and product grid render.
- Search for a keyword and confirm the storefront calls the search endpoint.
- Select a category and confirm the storefront calls the category endpoint.
- Open a product purchase dialog, enter a demo customer identifier, and confirm a purchase succeeds.
- Verify the product list refreshes after a successful purchase.
- Confirm sold-out products are disabled and cannot be purchased.
- Open the dashboard route and confirm it shows a truthful placeholder instead of fake metrics.