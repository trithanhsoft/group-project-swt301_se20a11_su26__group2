import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { adminService } from '../services/adminService';
import type { OrderDetails, AwardDetails, SaleDetails, PageResponse, AdminFinancialDetails, MonthlyFinancialBreakdown, TopRevenueCourse, PayoutDetails } from '../services/adminService';

const FinancialAllTimeReport: React.FC<{ details: AdminFinancialDetails | null }> = ({ details }) => {
  const [selectedYear, setSelectedYear] = useState<string>('ALL');

  const availableYears = useMemo(() => {
    const yearsSet = new Set<string>();
    (details?.monthlyBreakdowns || []).forEach((b: MonthlyFinancialBreakdown) => {
      if (b.datePrefix && b.datePrefix.length >= 4) {
        const year = b.datePrefix.substring(0, 4);
        yearsSet.add(year);
      }
    });
    return Array.from(yearsSet).sort().reverse();
  }, [details]);

  const filteredBreakdowns = useMemo(() => {
    const list = details?.monthlyBreakdowns || [];
    if (selectedYear === 'ALL') return list;
    return list.filter((b: MonthlyFinancialBreakdown) => b.datePrefix && b.datePrefix.startsWith(selectedYear));
  }, [details, selectedYear]);

  const summary = useMemo(() => {
    let gross = 0;
    let count = 0;
    let rewards = 0;
    let server = 0;
    let marketing = 0;
    let netProfit = 0;

    filteredBreakdowns.forEach((item: MonthlyFinancialBreakdown) => {
      gross += item.gross || 0;
      count += item.count || 0;
      rewards += item.rewards || 0;
      server += item.server || 0;
      marketing += item.marketing || 0;
      netProfit += item.netProfit || 0;
    });

    const platformShare = Math.round(gross * 0.3);
    const instructorShare = Math.round(gross * 0.7);
    const gatewayFees = Math.round(gross * 0.02);

    return {
      gross,
      count,
      rewards,
      server,
      marketing,
      netProfit,
      platformShare,
      instructorShare,
      gatewayFees
    };
  }, [filteredBreakdowns]);

  return (
    <div className="flex flex-col gap-5 text-slate-800">
      {/* Year Selector */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center bg-slate-50 p-4 rounded-2xl border border-slate-100 gap-3">
        <div className="flex items-center gap-2">
          <span className="font-bold text-slate-600">Filter by report year:</span>
          <select
            value={selectedYear}
            onChange={(e) => setSelectedYear(e.target.value)}
            className="bg-white border border-slate-200 px-3 py-1.5 rounded-xl text-xs font-bold text-brand-blue outline-none cursor-pointer"
          >
            <option value="ALL">All time operation</option>
            {availableYears.map(yr => (
              <option key={yr} value={yr}>Year {yr}</option>
            ))}
          </select>
        </div>
        <span className="text-[10px] font-black uppercase text-slate-400">
          Time frame: {selectedYear === 'ALL' ? 'Since the beginning' : `Year ${selectedYear}`}
        </span>
      </div>

      {/* KPI summaries for selected range */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-slate-50/50 p-4 rounded-xl border border-slate-100">
          <p className="text-[10px] text-slate-400 uppercase font-black">Gross Revenue</p>
          <p className="text-sm font-mono font-black text-slate-900 mt-1">{summary.gross.toLocaleString()} ₫</p>
        </div>
        <div className="bg-slate-50/50 p-4 rounded-xl border border-slate-100">
          <p className="text-[10px] text-slate-400 uppercase font-black">Platform Cut (30%)</p>
          <p className="text-sm font-mono font-black text-indigo-600 mt-1">{summary.platformShare.toLocaleString()} ₫</p>
        </div>
        <div className="bg-slate-50/50 p-4 rounded-xl border border-slate-100">
          <p className="text-[10px] text-slate-400 uppercase font-black">Courses Sold</p>
          <p className="text-sm font-mono font-black text-slate-900 mt-1">{summary.count.toLocaleString()} copies</p>
        </div>
        <div className="bg-slate-50/50 p-4 rounded-xl border border-slate-100">
          <p className="text-[10px] text-slate-400 uppercase font-black">Net Profit</p>
          <p className={`text-sm font-mono font-black mt-1 ${summary.netProfit >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
            {summary.netProfit.toLocaleString()} ₫
          </p>
        </div>
      </div>

      {/* Monthly Breakdown Sheet */}
      <div>
        <h4 className="font-display font-black text-slate-900 text-xs mb-3">
          Monthly detailed financial report table
        </h4>
        <div className="overflow-x-auto border border-slate-100 rounded-xl">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 text-[10px] font-black text-slate-500 border-b border-slate-100 uppercase tracking-wider">
                <th className="p-3">Month</th>
                <th className="p-3 text-right">Gross Revenue</th>
                <th className="p-3 text-right">Platform (30%)</th>
                <th className="p-3 text-right">Contest Prizes</th>
                <th className="p-3 text-right">Operating Costs</th>
                <th className="p-3 text-right">Net Profit</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-semibold text-slate-700">
              {filteredBreakdowns.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-4 text-center text-slate-400 italic">No data available.</td>
                </tr>
              ) : (
                filteredBreakdowns.map((b: MonthlyFinancialBreakdown, idx: number) => {
                  const gross = b.gross || 0;
                  const platformShare = Math.round(gross * 0.3);
                  const gatewayFees = Math.round(gross * 0.02);
                  const operCosts = (b.server || 0) + (b.marketing || 0) + gatewayFees;
                  return (
                    <tr key={idx} className="hover:bg-slate-50/50 transition-colors">
                      <td className="p-3 text-slate-900 font-bold">{b.label}</td>
                      <td className="p-3 text-right font-mono text-slate-900">{gross.toLocaleString()} ₫</td>
                      <td className="p-3 text-right font-mono text-indigo-600">+{platformShare.toLocaleString()} ₫</td>
                      <td className="p-3 text-right font-mono text-rose-500">-{b.rewards.toLocaleString()} ₫</td>
                      <td className="p-3 text-right font-mono text-slate-500" title={`Server: ${(b.server || 0).toLocaleString()} ₫, Marketing: ${(b.marketing || 0).toLocaleString()} ₫, Gateway Fee (2%): ${gatewayFees.toLocaleString()} ₫`}>
                        -{operCosts.toLocaleString()} ₫
                      </td>
                      <td className={`p-3 text-right font-mono font-bold ${b.netProfit >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
                        {b.netProfit.toLocaleString()} ₫
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export function AdminFinancialReports() {
  const navigate = useNavigate();
  const location = useLocation();
  const initialTab = location.state?.tab || 'gross';
  const [activeTab, setActiveTab] = useState<'gross' | 'instructor' | 'platform' | 'awards' | 'profit' | 'sales' | 'course-stats' | 'payouts'>(initialTab);
  
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const [page, setPage] = useState(1);
  const [limit] = useState(10);

  const [orders, setOrders] = useState<PageResponse<OrderDetails> | null>(null);
  const [awards, setAwards] = useState<PageResponse<AwardDetails> | null>(null);
  const [sales, setSales] = useState<PageResponse<SaleDetails> | null>(null);
  const [payouts, setPayouts] = useState<PageResponse<PayoutDetails> | null>(null);
  const [financialDetails, setFinancialDetails] = useState<AdminFinancialDetails | null>(null);
  const [topCourses, setTopCourses] = useState<TopRevenueCourse[] | null>(null);
  
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, [activeTab, page, startDate, endDate]);

  const fetchData = async () => {
    setLoading(true);
    try {
      if (activeTab === 'gross' || activeTab === 'instructor' || activeTab === 'platform') {
        const res = await adminService.getFinancialOrders(page, limit, startDate, endDate);
        setOrders(res);
      } else if (activeTab === 'awards') {
        const res = await adminService.getFinancialAwards(page, limit, startDate, endDate);
        setAwards(res);
      } else if (activeTab === 'sales') {
        const res = await adminService.getFinancialSales(page, limit, startDate, endDate);
        setSales(res);
      } else if (activeTab === 'profit') {
        if (!financialDetails) {
          const res = await adminService.getFinancialDetails();
          setFinancialDetails(res);
        }
      } else if (activeTab === 'course-stats') {
        const res = await adminService.getFinancialTopCourses();
        setTopCourses(res);
      } else if (activeTab === 'payouts') {
        const res = await adminService.getFinancialPayouts(page, limit, startDate, endDate);
        setPayouts(res);
      }
    } catch (error) {
      console.error('Failed to fetch data', error);
    } finally {
      setLoading(false);
    }
  };

  const clearFilters = () => {
    setStartDate('');
    setEndDate('');
    setPage(1);
  };

  const handleTabChange = (tab: 'gross' | 'instructor' | 'platform' | 'awards' | 'profit' | 'sales' | 'course-stats' | 'payouts') => {
    setActiveTab(tab);
    setPage(1);
  };

  return (
    <div className="p-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <button 
            onClick={() => navigate('/admin/financial')}
            className="text-slate-500 hover:text-primary font-bold text-sm flex items-center gap-1 mb-2 transition-colors cursor-pointer"
          >
            <span className="material-symbols-outlined text-[18px]">arrow_back</span>
            Back to Revenue
          </button>
          <h1 className="text-3xl font-black text-slate-900 tracking-tight flex items-center gap-3">
            <span className="material-symbols-outlined text-4xl text-primary">monitoring</span>
            Financial Reports
          </h1>
          <p className="text-slate-500 font-medium text-sm mt-1">
            Detailed transaction records, sales, and instructor payouts
          </p>
        </div>

        {/* Date Filters */}
        <div className="flex flex-wrap items-center gap-3 bg-white p-2 rounded-xl shadow-sm border border-slate-200">
          <div className="flex items-center gap-2 px-2">
            <span className="material-symbols-outlined text-slate-400 text-sm">calendar_month</span>
            <input 
              type="date" 
              className="text-sm font-bold text-slate-700 bg-transparent outline-none cursor-pointer"
              value={startDate}
              onChange={(e) => { setStartDate(e.target.value); setPage(1); }}
            />
          </div>
          <span className="text-slate-300 font-bold">-</span>
          <div className="flex items-center gap-2 px-2">
            <input 
              type="date" 
              className="text-sm font-bold text-slate-700 bg-transparent outline-none cursor-pointer"
              value={endDate}
              min={startDate}
              onChange={(e) => { setEndDate(e.target.value); setPage(1); }}
            />
          </div>
          {(startDate || endDate) && (
            <button 
              onClick={clearFilters}
              className="p-1 hover:bg-red-50 rounded-lg text-red-500 transition-colors tooltip cursor-pointer"
              title="Clear dates"
            >
              <span className="material-symbols-outlined text-[18px]">close</span>
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-1 border-b border-slate-200 pb-0 overflow-x-auto no-scrollbar">
        {[
          { id: 'gross', label: 'Gross Revenue', icon: 'account_balance_wallet' },
          { id: 'instructor', label: 'Instructor Share', icon: 'school' },
          { id: 'platform', label: 'Platform Cut', icon: 'pie_chart' },
          { id: 'awards', label: 'Contest Prizes', icon: 'emoji_events' },
          { id: 'payouts', label: 'Instructor Payouts', icon: 'account_balance' },
          { id: 'profit', label: 'Operating Profit', icon: 'trending_up' },
          { id: 'sales', label: 'Courses Sold', icon: 'shopping_cart' },
          { id: 'course-stats', label: 'Course Statistics', icon: 'auto_graph' }
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => handleTabChange(tab.id as any)}
            className={`flex items-center gap-1.5 px-4 py-2.5 font-bold text-xs border-b-2 transition-all cursor-pointer whitespace-nowrap ${
              activeTab === tab.id 
                ? 'border-primary text-primary bg-primary/5' 
                : 'border-transparent text-slate-500 hover:text-slate-700 hover:bg-slate-50'
            } rounded-t-xl`}
          >
            <span className="material-symbols-outlined text-[16px]">{tab.icon}</span>
            {tab.label}
          </button>
        ))}
      </div>

      {/* Content Area */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 min-h-[400px]">
        
        {/* Loading Overlay */}
        {loading ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          </div>
        ) : (
          <>
            {/* Orders Data (Gross, Instructor, Platform) */}
            {(activeTab === 'gross' || activeTab === 'instructor' || activeTab === 'platform') && orders && (
              <div className="flex flex-col gap-4">
                <div className="overflow-x-auto border border-slate-100 rounded-xl">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-slate-50 text-[10px] font-black text-slate-500 border-b border-slate-100 uppercase tracking-wider">
                        <th className="p-4 rounded-tl-xl">Order ID</th>
                        <th className="p-4">Date</th>
                        <th className="p-4">Customer</th>
                        <th className="p-4">Courses Bought</th>
                        {activeTab === 'gross' && <th className="p-4 text-right">Gross Amount</th>}
                        {activeTab === 'instructor' && <th className="p-4 text-right text-violet-600">Instructor (70%)</th>}
                        {activeTab === 'platform' && <th className="p-4 text-right text-indigo-600">Platform (30%)</th>}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 font-semibold text-slate-700">
                      {orders.content.length === 0 ? (
                        <tr>
                          <td colSpan={5} className="p-8 text-center text-slate-400 italic font-medium">No deposit records found.</td>
                        </tr>
                      ) : (
                        orders.content.map((o: any, idx: number) => (
                          <tr key={idx} className="hover:bg-slate-50/50 transition-colors">
                            <td className="p-4 font-mono text-[11px] text-slate-400">#{o.id}</td>
                            <td className="p-4 text-sm text-slate-600 font-bold">{new Date(o.date).toLocaleString('en-GB', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: 'short', year: 'numeric' })}</td>
                            <td className="p-4 text-slate-600 font-medium">
                              <div className="flex items-center gap-2">
                                <span className="material-symbols-outlined text-slate-400 text-lg">person</span>
                                {o.customerName}
                              </div>
                            </td>
                            <td className="p-4 text-sm text-slate-600 max-w-xs truncate" title={o.courses}>{o.courses || '-'}</td>
                            {activeTab === 'gross' && <td className="p-4 text-right font-mono font-bold text-slate-900">+{o.grossAmount.toLocaleString()} ₫</td>}
                            {activeTab === 'instructor' && <td className="p-4 text-right font-mono font-bold text-violet-600">+{(o.grossAmount * 0.7).toLocaleString()} ₫</td>}
                            {activeTab === 'platform' && <td className="p-4 text-right font-mono font-bold text-indigo-600">+{(o.grossAmount * 0.3).toLocaleString()} ₫</td>}
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
                {renderPagination(orders.page, orders.totalPages, setPage)}
              </div>
            )}
            {activeTab === 'sales' && sales && (
              <div className="flex flex-col gap-4">
                <div className="overflow-x-auto border border-slate-100 rounded-xl">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-slate-50 text-[10px] font-black text-slate-500 border-b border-slate-100 uppercase tracking-wider">
                        <th className="p-3">Course</th>
                        <th className="p-3">Instructor</th>
                        <th className="p-3">Customer</th>
                        <th className="p-3">Date</th>
                        <th className="p-3 text-right">Price Paid</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 font-semibold text-slate-700">
                      {sales.content.length === 0 ? (
                        <tr>
                          <td colSpan={5} className="p-8 text-center text-slate-400 italic font-medium">No course sales found.</td>
                        </tr>
                      ) : (
                        sales.content.map((s: any, idx: number) => (
                          <tr key={idx} className="hover:bg-slate-50/50 transition-colors">
                            <td className="p-3 text-slate-900 font-bold max-w-[200px] truncate" title={s.courseTitle}>{s.courseTitle}</td>
                            <td className="p-3 text-slate-600 text-sm font-bold">{s.instructorName}</td>
                            <td className="p-3 text-slate-500 text-sm">{s.customerName}</td>
                            <td className="p-3 text-sm text-slate-500 font-medium">{new Date(s.date).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}</td>
                            <td className="p-3 text-right font-mono font-bold text-slate-900">+{s.price.toLocaleString()} ₫</td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
                {renderPagination(sales.page, sales.totalPages, setPage)}
              </div>
            )}

            {activeTab === 'awards' && awards && (
              <div className="flex flex-col gap-4">
                <div className="overflow-x-auto border border-slate-100 rounded-xl">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-slate-50 text-[10px] font-black text-slate-500 border-b border-slate-100 uppercase tracking-wider">
                        <th className="p-3">Ref ID</th>
                        <th className="p-3">Date</th>
                        <th className="p-3">Instructor</th>
                        <th className="p-3 text-right">Amount Payout</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 font-semibold text-slate-700">
                      {awards.content.length === 0 ? (
                        <tr>
                          <td colSpan={4} className="p-8 text-center text-slate-400 italic font-medium">No payout records found.</td>
                        </tr>
                      ) : (
                        awards.content.map((a: any, idx: number) => (
                          <tr key={idx} className="hover:bg-slate-50/50 transition-colors">
                            <td className="p-3 font-mono text-[11px] text-slate-400">{a.referenceId || `#${a.id}`}</td>
                            <td className="p-3 text-sm text-slate-600 font-bold">{new Date(a.date).toLocaleString('en-GB', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: 'short', year: 'numeric' })}</td>
                            <td className="p-3">
                              <div className="flex flex-col">
                                <span className="text-slate-900 font-bold text-sm">{a.userName}</span>
                                <span className="text-xs text-slate-500 font-medium">{a.userEmail}</span>
                              </div>
                            </td>
                            <td className="p-3 text-right font-mono font-bold text-violet-600">+{a.amount.toLocaleString()} ₫</td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
                {renderPagination(awards.page, awards.totalPages, setPage)}
              </div>
            )}
            {/* Profit Tab */}
            {activeTab === 'profit' && (
              <div className="flex flex-col gap-4">
                <FinancialAllTimeReport details={financialDetails} />
              </div>
            )}

            {/* Course Statistics Tab */}
            {activeTab === 'course-stats' && (
              <div className="flex flex-col gap-4">
                <div className="overflow-x-auto border border-slate-100 rounded-xl">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-slate-50 text-[10px] font-black text-slate-500 border-b border-slate-100 uppercase tracking-wider">
                        <th className="p-3">Course Name</th>
                        <th className="p-3">Instructor</th>
                        <th className="p-3 text-center">Copies Sold</th>
                        <th className="p-3 text-right">Gross Revenue</th>
                        <th className="p-3 text-right text-violet-600">Instructor (70%)</th>
                        <th className="p-3 text-right text-indigo-600">Platform (30%)</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 font-semibold text-slate-700">
                      {(topCourses || []).length === 0 ? (
                        <tr>
                          <td colSpan={6} className="p-8 text-center text-slate-400 italic font-medium">No course revenue data available.</td>
                        </tr>
                      ) : (
                        (topCourses || []).map((c, idx) => (
                          <tr key={idx} className="hover:bg-slate-50/50 transition-colors">
                            <td className="p-3 text-slate-900 font-bold max-w-[200px] truncate" title={c.name}>{c.name}</td>
                            <td className="p-3 text-slate-500 font-extrabold">{c.tutor}</td>
                            <td className="p-3 text-center font-mono font-bold">{c.sold}</td>
                            <td className="p-3 text-right font-mono font-bold text-slate-900">{c.gross.toLocaleString()} ₫</td>
                            <td className="p-3 text-right font-mono font-semibold text-violet-600">+{c.payout.toLocaleString()} ₫</td>
                            <td className="p-3 text-right font-mono font-semibold text-indigo-600">+{c.plat.toLocaleString()} ₫</td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* Payouts Tab */}
            {activeTab === 'payouts' && payouts && (
              <div className="flex flex-col gap-4">
                <div className="overflow-x-auto border border-slate-100 rounded-xl">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-slate-50 text-[10px] font-black text-slate-500 border-b border-slate-100 uppercase tracking-wider">
                        <th className="p-3">Transaction ID</th>
                        <th className="p-3">Instructor</th>
                        <th className="p-3">Bank Account</th>
                        <th className="p-3">Status</th>
                        <th className="p-3 text-right">Amount Transferred</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 font-semibold text-slate-700">
                      {payouts.content.length === 0 ? (
                        <tr>
                          <td colSpan={5} className="p-8 text-center text-slate-400 italic font-medium">No payout records found.</td>
                        </tr>
                      ) : (
                        payouts.content.map((p: any, idx: number) => (
                          <tr key={idx} className="hover:bg-slate-50/50 transition-colors">
                            <td className="p-3 font-mono text-[11px] text-slate-400">{p.id}</td>
                            <td className="p-3">
                              <div className="flex flex-col">
                                <span className="text-slate-900 font-bold text-sm">{p.instructorName}</span>
                                <span className="text-xs text-slate-500 font-medium">{p.instructorEmail}</span>
                              </div>
                            </td>
                            <td className="p-3 font-mono text-sm text-slate-600">{p.bankAccount}</td>
                            <td className="p-3">
                              <span className={`px-2.5 py-1 rounded-full text-[10px] font-black tracking-wider ${
                                p.status === 'COMPLETED' ? 'bg-emerald-100 text-emerald-600' :
                                p.status === 'PENDING' ? 'bg-orange-100 text-orange-600' :
                                'bg-red-100 text-red-600'
                              }`}>
                                {p.status}
                              </span>
                            </td>
                            <td className="p-3 text-right font-mono font-bold text-[#10B981]">-{p.amount.toLocaleString()} ₫</td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
                {renderPagination(payouts.page, payouts.totalPages, setPage)}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

// Helper component for pagination
function renderPagination(currentPage: number, totalPages: number, setPage: (p: number) => void) {
  if (totalPages <= 1) return null;
  
  return (
    <div className="flex items-center justify-between mt-2 px-2">
      <span className="text-xs font-bold text-slate-500">
        Page {currentPage} of {totalPages}
      </span>
      <div className="flex items-center gap-1">
        <button 
          onClick={() => setPage(Math.max(1, currentPage - 1))}
          disabled={currentPage === 1}
          className="p-1.5 rounded-lg border border-slate-200 text-slate-500 hover:bg-white disabled:opacity-50 transition-colors cursor-pointer"
        >
          <span className="material-symbols-outlined text-[18px]">chevron_left</span>
        </button>
        
        <div className="flex gap-1">
          {Array.from({ length: Math.min(5, totalPages) }).map((_, i) => {
            let pageNum = i + 1;
            if (totalPages > 5) {
              if (currentPage > 3) pageNum = currentPage - 2 + i;
              if (pageNum > totalPages) pageNum = totalPages - (4 - i);
            }
            return (
              <button
                key={pageNum}
                onClick={() => setPage(pageNum)}
                className={`w-8 h-8 rounded-lg text-xs font-bold transition-colors cursor-pointer ${
                  currentPage === pageNum 
                    ? 'bg-primary text-white shadow-md shadow-primary/20' 
                    : 'text-slate-600 hover:bg-slate-200/50 border border-transparent hover:border-slate-300'
                }`}
              >
                {pageNum}
              </button>
            );
          })}
        </div>

        <button 
          onClick={() => setPage(Math.min(totalPages, currentPage + 1))}
          disabled={currentPage === totalPages}
          className="p-1.5 rounded-lg border border-slate-200 text-slate-500 hover:bg-white disabled:opacity-50 transition-colors cursor-pointer"
        >
          <span className="material-symbols-outlined text-[18px]">chevron_right</span>
        </button>
      </div>
    </div>
  );
}
