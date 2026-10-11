export interface Product {
  id: number;
  sku: string;
  name: string;
  category: string;
  price: number;
  stock: number;
  flashSale: boolean;
  updatedAt: string;
}

export interface PagePayload<T> {
  content?: T[];
  number?: number;
  size?: number;
  totalPages?: number;
  totalElements?: number;
  first?: boolean;
  last?: boolean;
  numberOfElements?: number;
  empty?: boolean;
}

export interface PageResult<T> {
  items: T[];
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface PurchaseRequest {
  productId: number;
  quantity: number;
  customerIdentifier: string;
  displayedPrice: number;
}
