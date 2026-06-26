export interface TransactionStatisticResponse {
  date: string;
  type: string;
  amount: number;
  status: string;
}

export interface PaymentTransactionStatisticResponse {
  date: string;
  transactionCode: string;
  type: string;
  amount: number;
  status: string;
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

const BASE_URL = 'http://localhost:8080/nonstopcoding';

export const paymentService = {
  createDepositLink: async (amount: number) => {
    const response = await fetch(`${BASE_URL}/payment/deposit`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify({ amount })
    });

    if (!response.ok) {
      throw new Error('Failed to create payment link');
    }

    const data = await response.json();
    return data.result; // contains checkoutUrl, transactionCode, qrCode
  },

  getBalance: async (): Promise<number> => {
    const response = await fetch(`${BASE_URL}/payment/balance`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch balance');
    }

    const data = await response.json();
    return Number(data.result);
  },

  getWalletTransactions: async (page: number = 0, size: number = 10, type: string = ''): Promise<PageResponse<TransactionStatisticResponse>> => {
    const typeQuery = type ? `&type=${type}` : '';
    const response = await fetch(`${BASE_URL}/wallet/transactions?page=${page}&size=${size}${typeQuery}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch wallet transactions');
    }

    const data = await response.json();
    return data.result;
  },

  getPaymentTransactions: async (page: number = 0, size: number = 10, type: string = ''): Promise<PageResponse<PaymentTransactionStatisticResponse>> => {
    const typeQuery = type ? `&type=${type}` : '';
    const response = await fetch(`${BASE_URL}/payment/transactions?page=${page}&size=${size}${typeQuery}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to fetch payment transactions');
    }

    const data = await response.json();
    return data.result;
  },

  cancelPayment: async (transactionCode: string): Promise<void> => {
    const response = await fetch(`${BASE_URL}/payment/cancel/${transactionCode}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to cancel payment');
    }
  }
};

