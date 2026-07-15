import React from 'react';
import { Link } from 'react-router-dom';

interface ContestSidebarProps {
  contestId: string;
  activeTab: 'overview' | 'problems' | 'submissions' | 'ranking';
  timeLeft: string;
  timerLabel: string;
  isRegistered: boolean;
  contestStatus?: string; // 'UPCOMING' | 'ONGOING' | 'ENDED'
  children?: React.ReactNode;
  className?: string;
}


export const ContestSidebar: React.FC<ContestSidebarProps> = ({ contestId, activeTab, timeLeft, timerLabel, isRegistered, contestStatus, children, className }) => {
  // Chỉ hiển thị Problems/Submissions/Ranking khi contest đã bắt đầu (ONGOING hoặc ENDED)
  const canAccessContestContent = isRegistered && contestStatus !== 'UPCOMING';

  return (
    <aside className={className || "w-full md:w-[12%] min-w-[190px] bg-white border-l border-gray-200 flex flex-col relative sticky top-16 h-[calc(100vh-64px)] z-20 shrink-0 shadow-lg"}>
      <div className="flex-grow py-5 px-3 flex flex-col overflow-y-auto">
        {/* Contest Header / Timer */}
        <div className="mb-6 text-center">
          <h2 className="text-sm font-black text-brand-blue mb-1.5 tracking-tight">Contest #{contestId}</h2>
          <div className="bg-slate-50 rounded-lg p-2.5 shadow-sm border border-gray-200">
            <p className="text-[10px] font-semibold text-text-muted uppercase tracking-wider mb-0.5">{timerLabel}</p>
            <div className="font-display text-base font-bold text-primary tabular-nums tracking-tight">{timeLeft}</div>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="space-y-1 font-semibold text-xs flex-grow">
          <Link
            className={`w-full flex items-center gap-2 py-2 px-2.5 rounded-lg transition-all border-none text-left cursor-pointer ${
              activeTab === 'overview'
                ? 'text-primary font-bold border-l-4 border-primary bg-primary-light/30 shadow-sm translate-x-0.5'
                : 'text-text-muted font-medium hover:bg-slate-50 bg-transparent'
            }`}
            to={`/contests/${contestId}`}
          >
            <span className={`material-symbols-outlined text-[16px] ${activeTab === 'overview' ? 'icon-fill font-black' : ''}`}>dashboard</span>
            Overview
          </Link>

          {/* Upcoming notice: registered nhưng chưa bắt đầu */}
          {isRegistered && contestStatus === 'UPCOMING' && (
            <div className="mt-4 p-2 bg-amber-50 border border-amber-200 rounded-lg text-center">
              <span className="material-symbols-outlined text-amber-500 text-lg mb-0.5 block">schedule</span>
              <p className="text-[10px] font-bold text-amber-700">Contest hasn't started</p>
              <p className="text-[9px] text-amber-600 mt-0.5">Available when it begins.</p>
            </div>
          )}

          {canAccessContestContent && (
            <>
              <Link
                className={`w-full flex items-center gap-2 py-2 px-2.5 rounded-lg transition-all border-none text-left cursor-pointer ${
                  activeTab === 'problems'
                    ? 'text-primary font-bold border-l-4 border-primary bg-primary-light/30 shadow-sm translate-x-0.5'
                    : 'text-text-muted font-medium hover:bg-slate-50 bg-transparent'
                }`}
                to={`/contests/${contestId}/problems`}
              >
                <span className={`material-symbols-outlined text-[16px] ${activeTab === 'problems' ? 'icon-fill font-black' : ''}`}>extension</span>
                Problems
              </Link>
              <Link
                className={`w-full flex items-center gap-2 py-2 px-2.5 rounded-lg transition-all border-none text-left cursor-pointer ${
                  activeTab === 'submissions'
                    ? 'text-primary font-bold border-l-4 border-primary bg-primary-light/30 shadow-sm translate-x-0.5'
                    : 'text-text-muted font-medium hover:bg-slate-50 bg-transparent'
                }`}
                to={`/contests/${contestId}/submissions`}
              >
                <span className={`material-symbols-outlined text-[16px] ${activeTab === 'submissions' ? 'icon-fill font-black' : ''}`}>list_alt</span>
                Submissions
              </Link>
              <Link
                className={`w-full flex items-center gap-2 py-2 px-2.5 rounded-lg transition-all border-none text-left cursor-pointer ${
                  activeTab === 'ranking'
                    ? 'text-primary font-bold border-l-4 border-primary bg-primary-light/30 shadow-sm translate-x-0.5'
                    : 'text-text-muted font-medium hover:bg-slate-50 bg-transparent'
                }`}
                to={`/contests/${contestId}/ranking`}
              >
                <span className={`material-symbols-outlined text-[16px] ${activeTab === 'ranking' ? 'icon-fill font-black' : ''}`}>leaderboard</span>
                Rankings
              </Link>
            </>
          )}
        </nav>

        {children}
      </div>
    </aside>
  );
};
