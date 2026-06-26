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
    <aside className={className || "w-full md:w-[15%] min-w-[280px] bg-white border-l border-gray-200 flex flex-col relative sticky top-16 h-[calc(100vh-64px)] z-20 shrink-0"}>
      <div className="flex-grow overflow-y-auto py-8 px-6">
        {/* Contest Header / Timer */}
        <div className="mb-10 text-center">
          <h2 className="text-xl font-black text-brand-blue mb-2 tracking-tight">Contest #{contestId}</h2>
          <div className="bg-surface-gray rounded-lg p-4 shadow-sm border border-gray-200">
            <p className="text-xs font-label text-text-muted uppercase tracking-wider mb-1">{timerLabel}</p>
            <div className="font-display text-2xl font-bold text-primary tabular-nums tracking-tight">{timeLeft}</div>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="space-y-2 font-label text-sm">
          <Link
            className={`flex items-center gap-3 py-3 px-4 rounded-lg transition-all ${
              activeTab === 'overview'
                ? 'text-primary font-bold border-l-4 border-primary bg-primary-light/30 shadow-sm translate-x-1'
                : 'text-text-muted font-medium hover:bg-surface-gray'
            }`}
            to={`/contests/${contestId}`}
          >
            <span className={`material-symbols-outlined ${activeTab === 'overview' ? 'icon-fill' : ''}`}>dashboard</span>
            Overview
          </Link>

          {/* Upcoming notice: registered nhưng chưa bắt đầu */}
          {isRegistered && contestStatus === 'UPCOMING' && (
            <div className="mt-4 p-3 bg-amber-50 border border-amber-200 rounded-lg text-center">
              <span className="material-symbols-outlined text-amber-500 text-2xl mb-1 block">schedule</span>
              <p className="text-xs font-bold text-amber-700">Contest hasn't started</p>
              <p className="text-xs text-amber-600 mt-0.5">Problems will be available when contest begins.</p>
            </div>
          )}

          {canAccessContestContent && (
            <>
              <Link
                className={`flex items-center gap-3 py-3 px-4 rounded-lg transition-all ${
                  activeTab === 'problems'
                    ? 'text-primary font-bold border-l-4 border-primary bg-primary-light/30 shadow-sm translate-x-1'
                    : 'text-text-muted font-medium hover:bg-surface-gray'
                }`}
                to={`/contests/${contestId}/problems`}
              >
                <span className={`material-symbols-outlined ${activeTab === 'problems' ? 'icon-fill' : ''}`}>extension</span>
                Problems
              </Link>
              <Link
                className={`flex items-center gap-3 py-3 px-4 rounded-lg transition-all ${
                  activeTab === 'submissions'
                    ? 'text-primary font-bold border-l-4 border-primary bg-primary-light/30 shadow-sm translate-x-1'
                    : 'text-text-muted font-medium hover:bg-surface-gray'
                }`}
                to={`/contests/${contestId}/submissions`}
              >
                <span className={`material-symbols-outlined ${activeTab === 'submissions' ? 'icon-fill' : ''}`}>list_alt</span>
                Submissions
              </Link>
              <Link
                className={`flex items-center gap-3 py-3 px-4 rounded-lg transition-all ${
                  activeTab === 'ranking'
                    ? 'text-primary font-bold border-l-4 border-primary bg-primary-light/30 shadow-sm translate-x-1'
                    : 'text-text-muted font-medium hover:bg-surface-gray'
                }`}
                to={`/contests/${contestId}/ranking`}
              >
                <span className={`material-symbols-outlined ${activeTab === 'ranking' ? 'icon-fill' : ''}`}>leaderboard</span>
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
