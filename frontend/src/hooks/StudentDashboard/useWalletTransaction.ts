import { useState, useRef, useEffect } from 'react';
import { type PurchaseHistoryResponse } from '../../services/orderService';

export const useWalletTransaction = () => {
  const [walletTransactions, setWalletTransactions] = useState<any[]>([]);
  const [walletTxPage, setWalletTxPage] = useState<number>(0);
  const [walletTxTotalPages, setWalletTxTotalPages] = useState<number>(1);
  const [walletTxTotalElements, setWalletTxTotalElements] = useState<number>(0);
  const [isWalletTxLoading, setIsWalletTxLoading] = useState<boolean>(false);
  const [selectedTxType, setSelectedTxType] = useState<string>('');

  const [paymentTransactions, setPaymentTransactions] = useState<any[]>([]);
  const [paymentTxPage, setPaymentTxPage] = useState<number>(0);
  const [paymentTxTotalPages, setPaymentTxTotalPages] = useState<number>(1);
  const [paymentTxTotalElements, setPaymentTxTotalElements] = useState<number>(0);
  const [isPaymentTxLoading, setIsPaymentTxLoading] = useState<boolean>(false);

  const [purchaseHistory, setPurchaseHistory] = useState<PurchaseHistoryResponse[]>([]);
  const [purchaseHistoryPage, setPurchaseHistoryPage] = useState<number>(0);
  const [purchaseHistoryTotalPages, setPurchaseHistoryTotalPages] = useState<number>(1);
  const [purchaseHistoryTotalElements, setPurchaseHistoryTotalElements] = useState<number>(0);
  const [isPurchaseHistoryLoading, setIsPurchaseHistoryLoading] = useState<boolean>(false);

  const [txSubTab, setTxSubTab] = useState<'internal' | 'banking'>('internal');
  const [isTxTypeDropdownOpen, setIsTxTypeDropdownOpen] = useState(false);
  const txTypeDropdownRef = useRef<HTMLDivElement>(null);

  // Outside click handler for dropdown
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (txTypeDropdownRef.current && !txTypeDropdownRef.current.contains(event.target as Node)) {
        setIsTxTypeDropdownOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  return {
    // Wallet transactions
    walletTransactions,
    setWalletTransactions,
    walletTxPage,
    setWalletTxPage,
    walletTxTotalPages,
    setWalletTxTotalPages,
    walletTxTotalElements,
    setWalletTxTotalElements,
    isWalletTxLoading,
    setIsWalletTxLoading,
    selectedTxType,
    setSelectedTxType,
    // Payment transactions
    paymentTransactions,
    setPaymentTransactions,
    paymentTxPage,
    setPaymentTxPage,
    paymentTxTotalPages,
    setPaymentTxTotalPages,
    paymentTxTotalElements,
    setPaymentTxTotalElements,
    isPaymentTxLoading,
    setIsPaymentTxLoading,
    // Purchase history
    purchaseHistory,
    setPurchaseHistory,
    purchaseHistoryPage,
    setPurchaseHistoryPage,
    purchaseHistoryTotalPages,
    setPurchaseHistoryTotalPages,
    purchaseHistoryTotalElements,
    setPurchaseHistoryTotalElements,
    isPurchaseHistoryLoading,
    setIsPurchaseHistoryLoading,
    // UI state
    txSubTab,
    setTxSubTab,
    isTxTypeDropdownOpen,
    setIsTxTypeDropdownOpen,
    txTypeDropdownRef
  };
};

