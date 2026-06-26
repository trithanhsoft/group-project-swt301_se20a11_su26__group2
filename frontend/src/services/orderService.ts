export const BASE_URL = 'http://localhost:8080/nonstopcoding';

export interface ApiResponse<T> {
  code: number;
  message: string;
  result: T;
}

export interface PurchaseItemResponse {
  courseId: number;
  courseTitle: string;
  instructorName: string;
  priceAtPurchase: number;
}

export interface PurchaseHistoryResponse {
  orderId: number;
  totalAmount: number;
  status: string;
  purchaseDate: string;
  items: PurchaseItemResponse[];
}

export interface PageResponse<T> {
  page: number;
  size: number;
  numberOfElements: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  content: T[];
}

const getHeaders = () => {
  return {
    'Content-Type': 'application/json',
  };
};

export const checkoutApi = async (courseIds: number[]): Promise<boolean> => {
  try {
    const response = await fetch(`${BASE_URL}/orders/checkout`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ courseIds }),
      credentials: 'include',
    });
    
    if (!response.ok) {
      const errData = await response.json().catch(() => null);
      console.error("Checkout failed:", errData);
      return false;
    }
    
    const data: ApiResponse<any> = await response.json();
    return data.code === 1000;
  } catch (error) {
    console.error("Failed to checkout:", error);
    return false;
  }
};

export const getPurchaseHistory = async (page: number = 0, size: number = 10): Promise<PageResponse<PurchaseHistoryResponse>> => {
  try {
    const response = await fetch(`${BASE_URL}/orders/purchase-history?page=${page}&size=${size}`, {
      method: 'GET',
      headers: getHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Error fetching purchase history: ${response.statusText}`);
    }

    const data: ApiResponse<PageResponse<PurchaseHistoryResponse>> = await response.json();
    if (data.code === 1000) {
      return data.result;
    } else {
      throw new Error(data.message || 'Error fetching purchase history');
    }
  } catch (error) {
    console.error("Failed to fetch purchase history:", error);
    throw error;
  }
};
