export const BASE_URL = 'http://localhost:8080/nonstopcoding';

export interface ApiResponse<T> {
  code: number;
  message: string;
  result: T;
}

const getHeaders = () => {
  return {
    'Content-Type': 'application/json',
  };
};

export const fetchCart = async (): Promise<number[]> => {
  try {
    const response = await fetch(`${BASE_URL}/cart`, {
      method: 'GET',
      headers: getHeaders(),
      credentials: 'include',
    });
    
    if (!response.ok) {
      return [];
    }
    
    const data: ApiResponse<number[]> = await response.json();
    return data.result || [];
  } catch (error) {
    console.error("Failed to fetch cart:", error);
    return [];
  }
};

export const addToCartApi = async (courseId: number | string): Promise<boolean> => {
  try {
    const response = await fetch(`${BASE_URL}/cart/${courseId}`, {
      method: 'POST',
      headers: getHeaders(),
      credentials: 'include',
    });
    return response.ok;
  } catch (error) {
    console.error("Failed to add to cart:", error);
    return false;
  }
};

export const removeFromCartApi = async (courseId: number | string): Promise<boolean> => {
  try {
    const response = await fetch(`${BASE_URL}/cart/${courseId}`, {
      method: 'DELETE',
      headers: getHeaders(),
      credentials: 'include',
    });
    return response.ok;
  } catch (error) {
    console.error("Failed to remove from cart:", error);
    return false;
  }
};

export const clearCartApi = async (): Promise<boolean> => {
  try {
    const response = await fetch(`${BASE_URL}/cart/clear`, {
      method: 'DELETE',
      headers: getHeaders(),
      credentials: 'include',
    });
    return response.ok;
  } catch (error) {
    console.error("Failed to clear cart:", error);
    return false;
  }
};
