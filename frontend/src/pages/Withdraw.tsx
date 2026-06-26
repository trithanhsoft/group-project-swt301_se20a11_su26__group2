import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../context/AppContext';

export const Withdraw: React.FC = () => {
  const { user, refreshBalance } = useApp();
  const [bankName, setBankName] = useState<string>('');
  const [accNumber, setAccNumber] = useState<string>('');
  const [accName, setAccName] = useState<string>('');
  const [amount, setAmount] = useState<string>('');
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [isSuccess, setIsSuccess] = useState<boolean>(false);

  useEffect(() => {
    if (user) {
      refreshBalance().catch(console.error);
    }
  }, [user?.id]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!bankName || !accNumber || !accName || !amount) {
      setErrorMsg('Please fill in all fields.');
      return;
    }

    const numAmount = Number(amount);
    if (numAmount < 100000) {
      setErrorMsg('Minimum withdrawal amount is 100,000 VND.');
      return;
    }

    const currentBalance = user?.walletBalance || 0;
    if (numAmount > currentBalance) {
      setErrorMsg('Cannot withdraw more than your current balance.');
      return;
    }

    setIsSuccess(true);
    setErrorMsg('');
    setTimeout(() => {
      setIsSuccess(false);
      // Reset form
      setBankName('');
      setAccNumber('');
      setAccName('');
      setAmount('');
    }, 3000);
  };

  return (
    <div className="flex-grow max-w-[1280px] w-full mx-auto px-6 md:px-16 py-12 flex flex-col gap-8 text-left relative z-10">
      
      {/* Navigation & Balance Row */}
      <div className="flex flex-col md:flex-row justify-between items-center border-b border-gray-200 mb-2 pb-2 md:pb-0 gap-4">
        <div className="flex h-12 gap-6 overflow-x-auto whitespace-nowrap w-full md:w-auto">
          <Link className="text-text-muted hover:text-primary transition-colors h-full flex items-center text-sm" to="/dashboard#wallet-transaction">Wallet Transaction</Link>
          <Link className="text-text-muted hover:text-primary transition-colors h-full flex items-center text-sm" to="/dashboard#deposit">Deposit</Link>
          <Link className="text-primary font-bold border-b-2 border-primary h-full flex items-center text-sm" to="/withdraw">Withdraw</Link>
          <Link className="text-text-muted hover:text-primary transition-colors h-full flex items-center text-sm" to="/dashboard#payment-transaction">Payment Transaction</Link>
        </div>
        <div className="bg-white py-2 px-4 rounded-xl shadow-[0_2px_12px_rgba(26,54,93,0.06)] flex items-center gap-3 min-w-[250px] mb-2 md:mb-0 shrink-0 border border-gray-200">
          <div className="w-9 h-9 rounded-full bg-green-50 flex items-center justify-center text-green-600">
            <span className="material-symbols-outlined text-xl icon-fill">account_balance_wallet</span>
          </div>
          <div>
            <p className="text-[11px] text-text-muted uppercase tracking-wider font-semibold">Current Balance</p>
            <p className="text-[17px] font-bold text-green-600 font-mono leading-none mt-0.5">{user?.walletBalance?.toLocaleString('vi-VN') || 0} ₫</p>
          </div>
        </div>
      </div>

      {isSuccess ? (
        <div className="bg-white rounded-xl shadow-[0_4px_20px_rgba(26,54,93,0.08)] p-12 border border-gray-200/60 text-center flex flex-col items-center justify-center gap-4 max-w-xl mx-auto w-full">
          <div className="w-16 h-16 rounded-full bg-green-100 text-green-600 flex items-center justify-center border border-green-200">
            <span className="material-symbols-outlined text-4xl icon-fill">check_circle</span>
          </div>
          <h3 className="text-xl font-bold text-brand-blue">Withdrawal Requested Successfully!</h3>
          <p className="text-sm text-text-muted">
            Your request to withdraw <strong>{Number(amount).toLocaleString('vi-VN')} ₫</strong> to bank account <strong>{accNumber}</strong> has been received and is being processed.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          
          {/* Left Column: Form */}
          <div className="lg:col-span-8 bg-white rounded-xl shadow-[0_4px_20px_rgba(26,54,93,0.08)] p-6 md:p-8 border border-gray-100">
            {errorMsg && (
              <div className="bg-red-50 border border-red-200 text-red-600 p-4 rounded-xl font-bold flex items-center gap-2 mb-6 text-sm">
                <span className="material-symbols-outlined text-[20px]">error</span>
                {errorMsg}
              </div>
            )}
            <form onSubmit={handleSubmit} className="flex flex-col gap-6">
              <div className="flex flex-col gap-2">
                <label className="text-sm font-semibold text-brand-blue" htmlFor="bankName">Bank Name</label>
                <select
                  className="w-full bg-white border border-gray-350 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  id="bankName"
                  name="bankName"
                  required
                  value={bankName}
                  onChange={(e) => setBankName(e.target.value)}
                >
                  <option value="" disabled>Select your bank</option>
                  <option value="vcb">Vietcombank</option>
                  <option value="tcb">Techcombank</option>
                  <option value="bidv">BIDV</option>
                  <option value="mbb">MB Bank</option>
                  <option value="vtb">VietinBank</option>
                </select>
              </div>
              <div className="flex flex-col gap-2">
                <label className="text-sm font-semibold text-brand-blue" htmlFor="accNumber">Account Number</label>
                <input
                  className="w-full bg-white border border-gray-350 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  id="accNumber"
                  name="accNumber"
                  placeholder="e.g. 1903456789012"
                  required
                  type="text"
                  value={accNumber}
                  onChange={(e) => setAccNumber(e.target.value)}
                />
              </div>
              <div className="flex flex-col gap-2">
                <label className="text-sm font-semibold text-brand-blue" htmlFor="accName">Account Name</label>
                <input
                  className="w-full bg-white border border-gray-350 rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all uppercase"
                  id="accName"
                  name="accName"
                  placeholder="e.g. NGUYEN VAN A"
                  required
                  type="text"
                  value={accName}
                  onChange={(e) => setAccName(e.target.value)}
                />
                <p className="text-xs text-text-muted mt-1">Must exactly match the name registered with your bank.</p>
              </div>
              <div className="flex flex-col gap-2 mt-4">
                <label className="text-sm font-semibold text-brand-blue" htmlFor="amount">Withdrawal Amount (VND)</label>
                <div className="relative">
                  <input
                    className="w-full bg-white border border-gray-350 rounded-lg px-4 py-3 pl-8 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all text-lg font-semibold"
                    id="amount"
                    max="2500000"
                    min="100000"
                    name="amount"
                    placeholder="0"
                    required
                    step="10000"
                    type="number"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                  />
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 text-text-muted font-bold">₫</span>
                </div>
              </div>
              <div className="pt-4 border-t border-gray-200 mt-4">
                <button
                  className="w-full bg-primary hover:bg-primary-hover text-white py-3 rounded-full transition-all shadow-md hover:shadow-lg flex justify-center items-center gap-2 font-bold text-sm"
                  id="submitBtn"
                  type="submit"
                >
                  <span>Withdraw Funds</span>
                  <span className="material-symbols-outlined text-sm">arrow_forward</span>
                </button>
              </div>
            </form>
          </div>

          {/* Right Column: Rules */}
          <div className="lg:col-span-4 flex flex-col gap-6">
            <div className="bg-brand-blue text-white rounded-xl p-6 md:p-8 relative overflow-hidden shadow-[0_4px_20px_rgba(26,54,93,0.15)]">
              <div
                className="absolute inset-0 opacity-10"
                style={{
                  backgroundImage: 'radial-gradient(circle at 2px 2px, white 1px, transparent 0)',
                  backgroundSize: '20px 20px',
                }}
              ></div>
              <div className="relative z-10 flex flex-col gap-6">
                <div className="flex items-center gap-3 border-b border-white/20 pb-4">
                  <span className="material-symbols-outlined text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>info</span>
                  <h3 className="text-md font-bold text-white">Withdrawal Rules</h3>
                </div>
                <ul className="flex flex-col gap-4 text-sm text-white/70">
                  <li className="flex gap-3 items-start">
                    <span className="material-symbols-outlined text-primary text-sm mt-0.5 icon-fill">check_circle</span>
                    <span>Minimum withdrawal amount is <strong>100,000 VND</strong>.</span>
                  </li>
                  <li className="flex gap-3 items-start">
                    <span className="material-symbols-outlined text-primary text-sm mt-0.5 icon-fill">schedule</span>
                    <span>Processing time takes typically <strong>2-4 hours</strong> during business days.</span>
                  </li>
                  <li className="flex gap-3 items-start">
                    <span className="material-symbols-outlined text-primary text-sm mt-0.5 icon-fill">badge</span>
                    <span>The bank account name <strong>must exactly match</strong> your registered profile name.</span>
                  </li>
                  <li className="flex gap-3 items-start">
                    <span className="material-symbols-outlined text-primary text-sm mt-0.5 icon-fill">warning</span>
                    <span>Withdrawals requested on weekends or holidays will be processed on the next business day.</span>
                  </li>
                </ul>
              </div>
            </div>
            {/* Support Card */}
            <div className="bg-white border border-gray-200 rounded-xl p-6 flex items-start gap-4 shadow-sm">
              <span className="material-symbols-outlined text-text-muted mt-1">support_agent</span>
              <div>
                <h4 className="font-semibold text-brand-blue mb-1 text-sm">Need help?</h4>
                <p className="text-xs text-text-muted mb-2">If you experience issues with your withdrawal, contact support.</p>
                <a className="text-sm text-primary hover:underline font-semibold" href="#">Contact Support</a>
              </div>
            </div>
          </div>

        </div>
      )}

    </div>
  );
};
