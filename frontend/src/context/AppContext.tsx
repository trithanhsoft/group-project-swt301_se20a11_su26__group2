import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';
import { fetchCart, addToCartApi, removeFromCartApi, clearCartApi } from '../services/cartService';
import { checkoutApi } from '../services/orderService';
import { paymentService } from '../services/paymentService';

export interface User {
  id: string;
  name: string;
  username: string;
  email: string;
  role: 'student' | 'instructor' | 'admin';
  avatar: string;
  walletBalance: number;
  status?: 'ACTIVE' | 'LOCKED';
  lockReason?: string;
  lockAppeal?: string;
}

export interface WalletTransaction {
  id: string;
  type: 'deposit' | 'withdraw';
  amount: number;
  status: 'Completed' | 'Pending' | 'Failed';
  method: string;
  date: string;
}

export interface PaymentTransaction {
  id: string;
  courseTitle: string;
  amount: number;
  status: 'Success' | 'Failed';
  date: string;
}

export interface CodeSubmission {
  id: string;
  problemId: string;
  problemTitle: string;
  lang: string;
  code: string;
  status: 'Accepted' | 'Wrong Answer' | 'Time Limit Exceeded' | 'Runtime Error' | 'Pending';
  runtime: string;
  memory: string;
  submittedAt: string;
  contestId?: string;
}

interface AppContextType {
  user: User | null;
  walletTransactions: WalletTransaction[];
  paymentTransactions: PaymentTransaction[];
  cart: string[]; // Course IDs
  enrolledCourses: string[]; // Course IDs
  submissions: CodeSubmission[];
  registeredContests: string[]; // Contest IDs
  login: (username: string, password: string) => Promise<User>;
  googleLogin: (idToken: string) => Promise<User>;
  register: (registerData: any) => Promise<User>;
  logout: () => Promise<void>;
  depositFunds: (amount: number, method: string) => void;
  withdrawFunds: (amount: number, bank: string, account: string) => boolean;
  addToCart: (courseId: string) => void;
  removeFromCart: (courseId: string) => void;
  clearCart: () => void;
  checkoutCart: (totalPrice: number, courseItems: { id: string; title: string; price: number }[]) => Promise<boolean>;
  submitCodeSolution: (
    problemId: string,
    problemTitle: string,
    lang: string,
    code: string,
    contestId?: string
  ) => Promise<CodeSubmission>;
  registerForContest: (contestId: string) => void;
  refreshBalance: () => Promise<void>;
  updateUser: (updatedFields: Partial<User>) => void;
  refreshAuth: () => Promise<User>;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Persist user state from localStorage
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('user_info');
    return saved ? JSON.parse(saved) : null;
  });

  const [cart, setCart] = useState<string[]>(() => {
    const savedCart = localStorage.getItem('guest_cart');
    return savedCart ? JSON.parse(savedCart) : [];
  });

  const [walletTransactions, setWalletTransactions] = useState<WalletTransaction[]>([
    { id: 'TX-1002', type: 'deposit', amount: 500000, status: 'Completed', method: 'VNPAY', date: '2026-05-24 14:30' },
    { id: 'TX-1001', type: 'deposit', amount: 2000000, status: 'Completed', method: 'Momo', date: '2026-05-20 09:15' },
  ]);

  const [paymentTransactions, setPaymentTransactions] = useState<PaymentTransaction[]>([
    { id: 'PAY-2001', courseTitle: 'Cấu trúc dữ liệu và Giải thuật với Java', amount: 499000, status: 'Success', date: '2026-05-21 10:00' },
  ]);

  const [enrolledCourses, setEnrolledCourses] = useState<string[]>(['c1']); // Starts enrolled in 'c1'
  const [submissions, setSubmissions] = useState<CodeSubmission[]>([
    {
      id: 'SUB-4001',
      problemId: 'p1',
      problemTitle: 'Two Sum',
      lang: 'C++',
      code: '#include <vector>\nusing namespace std;\n...',
      status: 'Accepted',
      runtime: '12 ms',
      memory: '10.4 MB',
      submittedAt: '2026-05-24 22:10',
    }
  ]);
  const [registeredContests, setRegisteredContests] = useState<string[]>([]);

  // Fetch cart from backend when user logs in or app loads
  useEffect(() => {
    if (user && user.role !== 'admin') {
      fetchCart().then(async ids => {
        // Hợp nhất giỏ hàng khách với DB
        const guestCartStr = localStorage.getItem('guest_cart');
        const guestCart: string[] = guestCartStr ? JSON.parse(guestCartStr) : [];

        let mergedCart = [...new Set([...ids.map(id => id.toString()), ...guestCart])];

        // Push guest items to backend
        for (const cId of guestCart) {
          if (!ids.includes(Number(cId))) {
            await addToCartApi(cId).catch(console.error);
          }
        }

        localStorage.removeItem('guest_cart'); // Clear after merge
        setCart(mergedCart);
      }).catch(err => {
        console.error("Lỗi khi fetch giỏ hàng từ DB:", err);
      });
    } else if (!user) {
      // Khi logout, giữ nguyên giỏ hàng DB cuối cùng hoặc xóa?
      // Thường thì nên tải lại từ guest_cart
      const savedCart = localStorage.getItem('guest_cart');
      setCart(savedCart ? JSON.parse(savedCart) : []);
    }
  }, [user?.id, user?.role]);

  const login = async (username: string, password: string): Promise<User> => {
    const result = await authService.login(username, password);
    let userRole: 'student' | 'instructor' | 'admin' = 'student';
    if (result.roles?.includes('ADMIN')) {
      userRole = 'admin';
    } else if (result.roles?.includes('INSTRUCTOR')) {
      userRole = 'instructor';
    }

    const loggedInUser: User = {
      id: result.id.toString(),
      name: result.displayName || username,
      username: result.username || username,
      email: result.email || '',
      role: userRole,
      avatar: result.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(result.displayName || username)}&background=F36F21&color=fff`,
      walletBalance: result.balance !== undefined ? Number(result.balance) : 0,
      status: result.status as 'ACTIVE' | 'LOCKED' || 'ACTIVE',
      lockReason: result.lockReason || '',
      lockAppeal: result.lockAppeal || '',
    };

    setUser(loggedInUser);
    localStorage.setItem('user_info', JSON.stringify(loggedInUser));
    return loggedInUser;
  };

  const googleLogin = async (idToken: string): Promise<User> => {
    const result = await authService.googleLogin(idToken);
    let userRole: 'student' | 'instructor' | 'admin' = 'student';
    if (result.roles?.includes('ADMIN')) {
      userRole = 'admin';
    } else if (result.roles?.includes('INSTRUCTOR')) {
      userRole = 'instructor';
    }

    const loggedInUser: User = {
      id: result.id.toString(),
      name: result.displayName || 'Google User',
      username: result.username || 'google_user',
      email: result.email || '',
      role: userRole,
      avatar: result.avatarUrl || `https://ui-avatars.com/api/?name=User&background=F36F21&color=fff`,
      walletBalance: result.balance !== undefined ? Number(result.balance) : 0,
      status: result.status as 'ACTIVE' | 'LOCKED' || 'ACTIVE',
      lockReason: result.lockReason || '',
      lockAppeal: result.lockAppeal || '',
    };

    setUser(loggedInUser);
    localStorage.setItem('user_info', JSON.stringify(loggedInUser));
    return loggedInUser;
  };

  const register = async (registerData: any): Promise<User> => {
    const result = await authService.register(registerData);
    let userRole: 'student' | 'instructor' | 'admin' = 'student';
    if (result.roles?.includes('ADMIN')) {
      userRole = 'admin';
    } else if (result.roles?.includes('INSTRUCTOR')) {
      userRole = 'instructor';
    }

    const loggedInUser: User = {
      id: result.id.toString(),
      name: result.displayName || registerData.displayname,
      username: result.username || registerData.username,
      email: result.email || registerData.email,
      role: userRole,
      avatar: result.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(result.displayName || registerData.displayname)}&background=F36F21&color=fff`,
      walletBalance: result.balance !== undefined ? Number(result.balance) : 0,
      status: result.status as 'ACTIVE' | 'LOCKED' || 'ACTIVE',
      lockReason: result.lockReason || '',
      lockAppeal: result.lockAppeal || '',
    };

    setUser(loggedInUser);
    localStorage.setItem('user_info', JSON.stringify(loggedInUser));
    return loggedInUser;
  };

  const logout = async (): Promise<void> => {
    try {
      await authService.logout();
    } catch (e) {
      console.error('Logout API failed, forcing local logout:', e);
    } finally {
      setUser(null);
      localStorage.removeItem('user_info');
    }
  };

  const depositFunds = (amount: number, method: string) => {
    if (!user) return;
    const newTx: WalletTransaction = {
      id: `TX-${Math.floor(1000 + Math.random() * 9000)}`,
      type: 'deposit',
      amount: amount,
      status: 'Completed',
      method: method,
      date: new Date().toISOString().replace('T', ' ').substring(0, 16),
    };
    setUser(prev => prev ? { ...prev, walletBalance: prev.walletBalance + amount } : null);
    setWalletTransactions(prev => [newTx, ...prev]);
  };

  const withdrawFunds = (amount: number, bank: string, account: string): boolean => {
    if (!user || user.walletBalance < amount) return false;
    const newTx: WalletTransaction = {
      id: `TX-${Math.floor(1000 + Math.random() * 9000)}`,
      type: 'withdraw',
      amount: amount,
      status: 'Completed',
      method: `${bank} (${account.substring(account.length - 4).padStart(account.length, '*')})`,
      date: new Date().toISOString().replace('T', ' ').substring(0, 16),
    };
    setUser(prev => prev ? { ...prev, walletBalance: prev.walletBalance - amount } : null);
    setWalletTransactions(prev => [newTx, ...prev]);
    return true;
  };

  const addToCart = async (courseId: string) => {
    if (!cart.includes(courseId)) {
      const newCart = [...cart, courseId];
      setCart(newCart);

      if (user) {
        try {
          const success = await addToCartApi(courseId);
          if (!success) {
            console.error("Backend API returned false for addToCart");
            alert("Có lỗi xảy ra khi lưu giỏ hàng vào Database. Vui lòng kiểm tra backend!");
          }
        } catch (error) {
          console.error("Error calling addToCartApi", error);
          alert("Lỗi kết nối Backend: Không thể gọi API lưu giỏ hàng. Chắc chắn bạn đã restart backend chưa?");
        }
      } else {
        // Lưu tạm vào localStorage cho khách chưa đăng nhập
        localStorage.setItem('guest_cart', JSON.stringify(newCart));
      }
    }
  };

  const removeFromCart = (courseId: string) => {
    setCart(prev => prev.filter(id => id !== courseId));
    if (user) {
      removeFromCartApi(courseId).catch(console.error);
    }
  };

  const clearCart = () => {
    setCart([]);
    if (user) {
      clearCartApi().catch(console.error);
    } else {
      localStorage.removeItem('guest_cart');
    }
  };

  const checkoutCart = async (totalPrice: number, courseItems: { id: string; title: string; price: number }[]): Promise<boolean> => {
    if (!user || user.walletBalance < totalPrice) return false;

    const courseIds = courseItems.map(c => Number(c.id));
    const success = await checkoutApi(courseIds);
    if (!success) return false;

    // Deduct money
    await refreshBalance();

    // Enroll in all checkout courses
    const courseIdStrs = courseItems.map(c => c.id);
    setEnrolledCourses(prev => [...new Set([...prev, ...courseIdStrs])]);

    // Create payment transaction records
    const newPayments = courseItems.map(item => ({
      id: `PAY-${Math.floor(2000 + Math.random() * 8000)}`,
      courseTitle: item.title,
      amount: item.price,
      status: 'Success' as const,
      date: new Date().toISOString().replace('T', ' ').substring(0, 16),
    }));

    setPaymentTransactions(prev => [...newPayments, ...prev]);
    setCart([]); // Clear cart locally
    if (user) {
      clearCartApi().catch(console.error); // Clear cart in backend
    }
    return true;
  };

  const submitCodeSolution = (
    problemId: string,
    problemTitle: string,
    lang: string,
    code: string,
    contestId?: string
  ): Promise<CodeSubmission> => {
    return new Promise((resolve) => {
      const submissionId = `SUB-${Math.floor(4000 + Math.random() * 6000)}`;
      const newSub: CodeSubmission = {
        id: submissionId,
        problemId,
        problemTitle,
        lang,
        code,
        status: 'Pending',
        runtime: '--',
        memory: '--',
        submittedAt: new Date().toISOString().replace('T', ' ').substring(0, 16),
        contestId,
      };

      // Add pending submission
      setSubmissions(prev => [newSub, ...prev]);

      // Simulate compiler delay
      setTimeout(() => {
        // Random outcome: 80% chance of Accepted, 20% other issues
        const rand = Math.random();
        let status: CodeSubmission['status'] = 'Accepted';
        let runtime = `${Math.floor(5 + Math.random() * 80)} ms`;
        let memory = `${(8 + Math.random() * 12).toFixed(1)} MB`;

        if (rand > 0.8) {
          status = 'Wrong Answer';
          runtime = '--';
          memory = '--';
        } else if (rand > 0.9) {
          status = 'Time Limit Exceeded';
          runtime = '> 2000 ms';
        } else if (rand > 0.95) {
          status = 'Runtime Error';
          runtime = '--';
        }

        const evaluatedSub: CodeSubmission = {
          ...newSub,
          status,
          runtime,
          memory,
        };

        // Update in list
        setSubmissions(prev => prev.map(s => s.id === submissionId ? evaluatedSub : s));
        resolve(evaluatedSub);
      }, 1500);
    });
  };

  const registerForContest = (contestId: string) => {
    if (!registeredContests.includes(contestId)) {
      setRegisteredContests(prev => [...prev, contestId]);
    }
  };

  const refreshBalance = async () => {
    if (!user) return;
    try {
      const currentBalance = await paymentService.getBalance();
      setUser(prev => {
        if (!prev) return prev;
        const updatedUser = { ...prev, walletBalance: currentBalance };
        localStorage.setItem('user_info', JSON.stringify(updatedUser));
        return updatedUser;
      });
    } catch (error) {
      console.error("Failed to refresh balance", error);
    }
  };

  const updateUser = (updatedFields: Partial<User>) => {
    setUser(prev => {
      if (!prev) return null;
      const newU = { ...prev, ...updatedFields };
      localStorage.setItem('user_info', JSON.stringify(newU));
      return newU;
    });
  };

  const refreshAuth = async (): Promise<User> => {
    const result = await authService.refresh();
    let userRole: 'student' | 'instructor' | 'admin' = 'student';
    if (result.roles?.includes('ADMIN')) {
      userRole = 'admin';
    } else if (result.roles?.includes('INSTRUCTOR')) {
      userRole = 'instructor';
    }

    const loggedInUser: User = {
      id: result.id.toString(),
      name: result.displayName || result.username || '',
      username: result.username || '',
      email: result.email || '',
      role: userRole,
      avatar: result.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(result.displayName || result.username || '')}&background=F36F21&color=fff`,
      walletBalance: result.balance !== undefined ? Number(result.balance) : 0,
      status: result.status as 'ACTIVE' | 'LOCKED' || 'ACTIVE',
      lockReason: result.lockReason || '',
      lockAppeal: result.lockAppeal || '',
    };

    setUser(loggedInUser);
    localStorage.setItem('user_info', JSON.stringify(loggedInUser));
    return loggedInUser;
  };

  return (
    <AppContext.Provider
      value={{
        user,
        walletTransactions,
        paymentTransactions,
        cart,
        enrolledCourses,
        submissions,
        registeredContests,
        login,
        googleLogin,
        register,
        logout,
        depositFunds,
        withdrawFunds,
        addToCart,
        removeFromCart,
        clearCart,
        checkoutCart,
        submitCodeSolution,
        registerForContest,
        refreshBalance,
        updateUser,
        refreshAuth,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
