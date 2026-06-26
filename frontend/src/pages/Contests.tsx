import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../context/AppContext';

interface Contest {
  id: number;
  title: string;
  description: string;
  scoringRule: string;
  startTime: string; 
  endTime: string;
  durations: number;
  status: 'UPCOMING' | 'ONGOING' | 'ENDED';
  creatorName: string;
  isPrivate: boolean;
  participantCount?: number;
  problemCount?: number;
  isUserRegistered?: boolean;
}

interface UserStats {
  avatarUrl?: string;
  displayName?: string;
  score?: number;
  rank?: number;
  totalUsers?: number;
  contestsCount?: number;
  avgAccuracy?: number;
}

export const Contests: React.FC = () => {
  const { user } = useApp();
  const timeOffsetRef = useRef<number>(0);

  // --- States for Interactivity ---
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('All');
  const [accessFilter, setAccessFilter] = useState('All');

  // --- States for Dynamic Data and Pagination ---
  const [contests, setContests] = useState<Contest[]>([]);
  const [currentPage, setCurrentPage] = useState(0); // 0-indexed for Spring Boot
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  // --- Banner & User Stats States ---
  const [bannerContest, setBannerContest] = useState<Contest | null>(null);
  const [userStats, setUserStats] = useState<UserStats | null>(null);

  // --- Fetch Contests List ---
  useEffect(() => {
    const fetchContests = async () => {
      setIsLoading(true);
      try {
        const response = await fetch(
          `http://localhost:8080/nonstopcoding/contests?search=${searchQuery}&status=${statusFilter}&access=${accessFilter}&page=${currentPage}&size=5`,
          {
            credentials: 'include',
          }
        );
        const data = await response.json();
        
        if (data && data.result) {
          setContests(data.result.content);
          setTotalPages(data.result.totalPages);
          setTotalElements(data.result.totalElements);
          if (data.timestamp) {
            timeOffsetRef.current = new Date(data.timestamp).getTime() - Date.now();
          }
        }
      } catch (error) {
        console.error("Lỗi khi fetch data contest:", error);
      } finally {
        setIsLoading(false);
      }
    };

    const delayDebounceFn = setTimeout(() => {
      void fetchContests();
    }, 400);

    return () => clearTimeout(delayDebounceFn);
  }, [searchQuery, statusFilter, accessFilter, currentPage]);

  const fetchBannerAndStats = useCallback(async () => {
    try {
      const bannerRes = await fetch('http://localhost:8080/nonstopcoding/contests/banner', {
        credentials: 'include'
      });
      const bannerData = await bannerRes.json();
      if (bannerData) {
        setBannerContest(bannerData.result);
        if (bannerData.timestamp) {
          timeOffsetRef.current = new Date(bannerData.timestamp).getTime() - Date.now();
        }
      }

      if (user) {
        const statsRes = await fetch('http://localhost:8080/nonstopcoding/contests/user-stats', {
          credentials: 'include'
        });
        const statsData = await statsRes.json();
        if (statsData && statsData.result) {
          setUserStats(statsData.result);
        }
      } else {
        setUserStats(null);
      }
    } catch (error) {
      console.error("Error fetching banner or stats:", error);
    }
  }, [user]);

  useEffect(() => {
    const timer = setTimeout(() => {
      void fetchBannerAndStats();
    }, 0);
    return () => clearTimeout(timer);
  }, [fetchBannerAndStats]);

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatusFilter(e.target.value);
    setCurrentPage(0);
  };

  const handleAccessChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setAccessFilter(e.target.value);
    setCurrentPage(0);
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const formatTime = (isoString: string) => {
    if (!isoString) return '';
    return new Date(isoString).toLocaleString('en-US', {
      month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit'
    });
  };

  // --- Banner Countdown Timer Setup ---
  const [timeLeft, setTimeLeft] = useState({
    days: '00',
    hours: '00',
    mins: '00',
    secs: '00',
    isLive: false
  });

  useEffect(() => {
    if (!bannerContest) return;

    const startTimeMs = new Date(bannerContest.startTime).getTime();

    const updateCountdown = () => {
      const now = Date.now() + timeOffsetRef.current;
      const difference = startTimeMs - now;

      if (difference <= 0) {
        setTimeLeft({
          days: '00',
          hours: '00',
          mins: '00',
          secs: '00',
          isLive: true
        });
      } else {
        const days = Math.floor(difference / (1000 * 60 * 60 * 24));
        const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const mins = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
        const secs = Math.floor((difference % (1000 * 60)) / 1000);

        setTimeLeft({
          days: String(days).padStart(2, '0'),
          hours: String(hours).padStart(2, '0'),
          mins: String(mins).padStart(2, '0'),
          secs: String(secs).padStart(2, '0'),
          isLive: false
        });
      }
    };

    updateCountdown();
    const interval = setInterval(updateCountdown, 1000);
    return () => clearInterval(interval);
  }, [bannerContest]);

  // --- Registration & Modal States ---
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalContestTitle, _setModalContestTitle] = useState('');
  const [modalTicketId, _setModalTicketId] = useState('');

  // Image fallback handler
  const handleAvatarError = (e: React.SyntheticEvent<HTMLImageElement, Event>) => {
    e.currentTarget.src = 'https://ui-avatars.com/api/?name=You&background=12284C&color=fff';
  };

  return (
    <main className="relative z-10 flex-grow w-full max-w-[1440px] mx-auto px-4 lg:px-8 py-12 flex flex-col gap-6 mt-4">
      {/* Custom Styles for Glow and Animations */}
      <style dangerouslySetInnerHTML={{ __html: `
        .ambient-shadow {
            box-shadow: 0 4px 20px rgba(18, 40, 76, 0.05);
        }
        .ambient-shadow-hover:hover {
            box-shadow: 0 8px 30px rgba(18, 40, 76, 0.1) !important;
        }
        @keyframes pulse-glow-orange {
            0%, 100% { box-shadow: 0 0 12px rgba(243, 111, 33, 0.4); }
            50% { box-shadow: 0 0 24px rgba(243, 111, 33, 0.75); }
        }
        @keyframes pulse-glow-green {
            0%, 100% { box-shadow: 0 0 8px rgba(70, 160, 64, 0.3); }
            50% { box-shadow: 0 0 18px rgba(70, 160, 64, 0.75); }
        }
        @keyframes floating {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-5px); }
        }
        .pulse-glow-orange {
            animation: pulse-glow-orange 2s infinite;
        }
        .pulse-glow-green {
            animation: pulse-glow-green 1.5s infinite;
        }
        .animate-float {
            animation: floating 3.5s ease-in-out infinite;
        }
        .glass-card {
            background: rgba(255, 255, 255, 0.8);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.5);
        }
        .fade-in {
            animation: fadeIn 0.4s ease-out forwards;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .no-scrollbar::-webkit-scrollbar {
            display: none;
        }
        .no-scrollbar {
            -ms-overflow-style: none;
            scrollbar-width: none;
        }
      `}} />

      {/* Glowing Backdrop Circles */}
      <div className="absolute inset-0 pointer-events-none z-0 overflow-hidden">
        <div className="absolute -top-40 -left-40 w-[500px] h-[500px] bg-primary/10 rounded-full blur-[120px]"></div>
        <div className="absolute top-1/3 -right-40 w-[500px] h-[500px] bg-brand-blue/10 rounded-full blur-[120px]"></div>
        <div className="absolute -bottom-40 left-1/4 w-[600px] h-[600px] bg-[#3b82f6]/5 rounded-full blur-[150px]"></div>
      </div>

      {/* Hero Promo Banner: Upcoming / Ongoing Hot Contest */}
      {bannerContest && (
        <section className="relative z-10 bg-gradient-to-br from-brand-blue via-[#173059] to-brand-blue rounded-2xl p-8 md:p-10 flex flex-col lg:flex-row items-center justify-between gap-8 text-white shadow-xl overflow-hidden border border-white/10">
          {/* Tech Graphics Backdrop */}
          <div className="absolute top-0 right-0 w-full h-full opacity-10 pointer-events-none overflow-hidden">
            <span className="material-symbols-outlined absolute -right-16 -top-16 text-[320px] text-primary/30 font-thin select-none">emoji_events</span>
            <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,rgba(243,111,33,0.15),transparent_60%)]"></div>
          </div>

          {/* Details */}
          <div className="relative z-10 flex flex-col gap-5 max-w-3xl">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="inline-flex items-center gap-1 bg-primary text-white font-extrabold text-[10px] md:text-xs uppercase tracking-widest px-3 py-1 rounded-full animate-pulse shadow-md">
                <span className="w-1.5 h-1.5 rounded-full bg-white"></span> Live Spotlight
              </span>
              <span className="text-[10px] md:text-xs text-white/70 bg-white/10 px-2.5 py-1 rounded-full font-medium">Spring Season 2026</span>
            </div>
            <div>
              <h2 className="font-display text-3xl md:text-5xl font-black mb-2 leading-tight tracking-tight bg-gradient-to-r from-white via-white to-primary-light bg-clip-text text-transparent">
                {bannerContest.title}
              </h2>
              <p className="font-body text-sm md:text-base text-white/80 max-w-2xl mt-2 leading-relaxed">
                {bannerContest.description}
              </p>
            </div>

            {/* Glassmorphism Stats Cards */}
            <div className="grid grid-cols-3 gap-3 md:gap-4 max-w-lg mt-1">
              <div className="bg-white/5 border border-white/10 backdrop-blur-md rounded-xl p-3 flex flex-col items-center justify-center text-center">
                <span className="material-symbols-outlined text-primary text-2xl mb-1 icon-fill">emoji_events</span>
                <span className="text-[10px] text-white/60 font-semibold uppercase tracking-wider">Scoring Rule</span>
                <span className="text-xs md:text-base font-bold text-white mt-0.5">{bannerContest.scoringRule || 'ICPC'}</span>
              </div>
              <div className="bg-white/5 border border-white/10 backdrop-blur-md rounded-xl p-3 flex flex-col items-center justify-center text-center">
                <span className="material-symbols-outlined text-brand-green text-2xl mb-1">timer</span>
                <span className="text-[10px] text-white/60 font-semibold uppercase tracking-wider">Duration</span>
                <span className="text-xs md:text-base font-bold text-white mt-0.5">{bannerContest.durations || 180} Mins</span>
              </div>
              <div className="bg-white/5 border border-white/10 backdrop-blur-md rounded-xl p-3 flex flex-col items-center justify-center text-center">
                <span className="material-symbols-outlined text-blue-400 text-2xl mb-1">quiz</span>
                <span className="text-[10px] text-white/60 font-semibold uppercase tracking-wider">Challenges</span>
                <span className="text-xs md:text-base font-bold text-white mt-0.5">{bannerContest.problemCount || 0} Tasks</span>
              </div>
            </div>
          </div>

          {/* Countdown & Action Box */}
          <div className="relative z-10 w-full lg:w-96 bg-white/5 border border-white/10 backdrop-blur-md rounded-2xl p-6 flex flex-col gap-5 justify-center items-center text-center shadow-2xl">
            {/* 1. Registration Competitors Count */}
            <div className="w-full flex justify-center items-center text-sm font-semibold text-white/70">
              <span className="flex items-center gap-1.5">
                <span className="material-symbols-outlined text-[18px]">groups</span>
                <span>{(bannerContest.participantCount || 0).toLocaleString()} Competitors Joined</span>
              </span>
            </div>

            {/* 2. Countdown Clock or Live Indicator */}
            <div className="w-full flex flex-col items-center gap-3">
              <h4 className="text-xs md:text-sm font-extrabold text-white/80 uppercase tracking-widest">
                {timeLeft.isLive ? 'Contest is Live' : 'Contest Begin In'}
              </h4>
              {timeLeft.isLive ? (
                <span className="text-brand-green font-bold text-lg uppercase tracking-widest flex items-center justify-center gap-1.5 animate-pulse">
                  <span className="w-2.5 h-2.5 rounded-full bg-brand-green"></span> Contest is LIVE!
                </span>
              ) : (
                <div className="flex items-center justify-center gap-2">
                  {/* Days */}
                  <div className="flex flex-col items-center justify-center bg-[#12284C] border border-white/10 rounded-2xl w-16 h-20 shadow-md">
                    <span className="text-2xl md:text-3xl font-extrabold font-display text-white">{timeLeft.days}</span>
                    <span className="text-[9px] uppercase tracking-wider text-white/60 font-semibold mt-1">Days</span>
                  </div>
                  <span className="text-xl font-bold text-white/30 animate-pulse">:</span>
                  
                  {/* Hours */}
                  <div className="flex flex-col items-center justify-center bg-[#12284C] border border-white/10 rounded-2xl w-16 h-20 shadow-md">
                    <span className="text-2xl md:text-3xl font-extrabold font-display text-white">{timeLeft.hours}</span>
                    <span className="text-[9px] uppercase tracking-wider text-white/60 font-semibold mt-1">Hours</span>
                  </div>
                  <span className="text-xl font-bold text-white/30 animate-pulse">:</span>
                  
                  {/* Mins */}
                  <div className="flex flex-col items-center justify-center bg-[#12284C] border border-white/10 rounded-2xl w-16 h-20 shadow-md">
                    <span className="text-2xl md:text-3xl font-extrabold font-display text-white">{timeLeft.mins}</span>
                    <span className="text-[9px] uppercase tracking-wider text-white/60 font-semibold mt-1">Mins</span>
                  </div>
                  <span className="text-xl font-bold text-white/30 animate-pulse">:</span>
                  
                  {/* Secs */}
                  <div className="flex flex-col items-center justify-center bg-[#12284C] border border-white/10 rounded-2xl w-16 h-20 shadow-md">
                    <span className="text-2xl md:text-3xl font-extrabold font-display text-[#f36f21]">{timeLeft.secs}</span>
                    <span className="text-[9px] uppercase tracking-wider text-white/60 font-semibold mt-1">Secs</span>
                  </div>
                </div>
              )}
            </div>

            {/* 3. Action Button: Enter Arena Now */}
            <Link 
              to={`/contests/${bannerContest.id}`} 
              className="w-full py-4 px-6 rounded-xl bg-gradient-to-r from-brand-green to-green-600 hover:from-green-600 hover:to-green-700 text-white font-extrabold text-base transition-all transform hover:-translate-y-0.5 active:translate-y-0 shadow-lg pulse-glow-green flex items-center justify-center gap-2"
            >
              <span className="material-symbols-outlined font-bold">login</span> Enter Arena Now
            </Link>
          </div>
        </section>
      )}

      {/* Combat Layout: Main List & Profile Sidebar */}
      <div className="relative z-10 grid grid-cols-1 lg:grid-cols-4 gap-6 items-start">
        
        {/* Left: Main Contests List Explorer (3 cols) */}
        <div className="lg:col-span-3 flex flex-col gap-6">
          
          {/* Page Title Arena Indicator */}
          <div className="flex flex-col gap-2 text-left">
            <div className="inline-flex items-center gap-1.5 bg-primary-light border border-primary/20 px-3.5 py-1.5 rounded-full text-primary font-extrabold text-xs uppercase tracking-wider w-max shadow-sm">
              <span className="material-symbols-outlined text-xs icon-fill">swords</span> Combat Arena
            </div>
            <h1 className="text-display-lg-mobile md:text-display-lg font-display font-black leading-none">
              <span className="bg-gradient-to-r from-[#0114a7] to-[#2563eb] bg-clip-text text-transparent">Coding </span>
              <span className="bg-gradient-to-r from-[#ff6000] to-[#ff8c42] bg-clip-text text-transparent">Contests</span>
            </h1>
            <p className="font-body text-base text-text-muted max-w-2xl leading-relaxed mt-1">
              Push your logical limits, out-code competitors in real-time, and rise up the global rankings.
            </p>
          </div>

          {/* Explorer Control Bar: Search & Tag Chips */}
          <div className="flex flex-col gap-4 bg-surface p-5 rounded-2xl border border-gray-100 shadow-md">
            {/* Row 1: Search and Filters */}
            <div className="flex flex-col md:flex-row gap-4 items-center">
              {/* Search Input */}
              <div className="relative w-full md:flex-1">
                <span className="material-symbols-outlined absolute left-3.5 top-1/2 -translate-y-1/2 text-text-muted">search</span>
                <input 
                  id="search-input" 
                  value={searchQuery}
                  onChange={handleSearchChange}
                  className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-gray-200 bg-surface-gray focus:border-primary focus:ring-2 focus:ring-primary-light outline-none transition-all font-body text-sm text-text-main placeholder-text-muted shadow-inner" 
                  placeholder="Search contests by title..." 
                  type="text" 
                />
              </div>
              
              {/* Advanced Category Selection Dropdown */}
              <div className="flex items-center gap-2 w-full md:w-auto">
                <select 
                  id="status-filter" 
                  value={statusFilter}
                  onChange={handleStatusChange}
                  className="w-full md:w-44 border border-gray-200 rounded-xl px-3 py-2.5 bg-surface font-semibold text-xs text-text-main focus:outline-none focus:ring-2 focus:ring-primary-light focus:border-primary"
                >
                  <option value="All">Status: All</option>
                  <option value="Ongoing">Ongoing</option>
                  <option value="Upcoming">Upcoming</option>
                  <option value="Ended">Ended</option>
                </select>
                <select 
                  id="access-filter" 
                  value={accessFilter}
                  onChange={handleAccessChange}
                  className="w-full md:w-40 border border-gray-200 rounded-xl px-3 py-2.5 bg-surface font-semibold text-xs text-text-main focus:outline-none focus:ring-2 focus:ring-primary-light focus:border-primary"
                >
                  <option value="All">Access: All</option>
                  <option value="Public">Public</option>
                  <option value="Lock">Private (Password)</option>
                </select>
              </div>
            </div>
          </div>

          {/* Contests Card Container */}
          <section id="contests-grid" className="flex flex-col gap-4 text-left">
            {isLoading ? (
              <div className="flex justify-center items-center py-12 bg-white rounded-2xl border border-gray-100 shadow-md">
                <span className="material-symbols-outlined text-primary text-4xl animate-spin">sync</span>
              </div>
            ) : contests.length === 0 ? (
              <div id="zero-state" className="flex flex-col items-center justify-center text-center p-12 bg-white rounded-2xl border border-gray-100 shadow-md">
                <span className="material-symbols-outlined text-text-muted text-5xl mb-2">find_in_page</span>
                <h3 className="text-base font-bold text-brand-blue">No Contests Found</h3>
                <p className="text-xs text-text-muted max-w-xs mt-1">Try relaxing your search terms or selecting another status filter.</p>
              </div>
            ) : (
              contests.map((contest) => (
                <article key={contest.id} className={`contest-card bg-surface rounded-2xl border border-gray-100 p-6 flex flex-col md:flex-row md:items-center justify-between gap-5 transition-all shadow-md ambient-shadow-hover group fade-in ${contest.status === 'ONGOING' ? 'hover:border-brand-green/60' : 'hover:border-primary/60'}`}>
                  <div className="flex-1 flex flex-col gap-3">
                    <div className="flex items-center gap-2">
                      {contest.status === 'ONGOING' ? (
                        <span className="inline-flex items-center gap-1.5 bg-brand-green text-white font-extrabold text-[10px] px-3 py-1 rounded-full uppercase tracking-wider pulse-glow-green">
                          <span className="w-1.5 h-1.5 rounded-full bg-white animate-ping"></span> Ongoing
                        </span>
                      ) : contest.status === 'UPCOMING' ? (
                        <span className="inline-flex items-center gap-1.5 bg-warning-container text-on-warning-container font-extrabold text-[10px] px-3 py-1 rounded-full uppercase tracking-wider">
                          <span className="material-symbols-outlined text-[12px] font-bold">schedule</span> Upcoming
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 bg-error/10 text-error font-extrabold text-[10px] px-3 py-1 rounded-full uppercase tracking-wider">
                          <span className="material-symbols-outlined text-[12px] font-bold">done</span> Ended
                        </span>
                      )}
                      {contest.isPrivate ? (
                        <span className="inline-flex items-center gap-1 bg-gray-100 text-gray-700 text-[10px] px-2.5 py-1 rounded-full font-bold uppercase">
                          <span className="material-symbols-outlined text-[12px]">lock</span> Private
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 bg-blue-50 text-blue-700 text-[10px] px-2.5 py-1 rounded-full font-bold uppercase">
                          <span className="material-symbols-outlined text-[12px]">public</span> Public
                        </span>
                      )}
                    </div>
                    <div>
                      <h3 className="font-display font-extrabold text-lg md:text-xl text-brand-blue group-hover:text-primary transition-colors">
                        {contest.title}
                      </h3>
                      <p className="text-sm text-text-muted mt-1 max-w-2xl">{contest.description}</p>
                      <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-text-muted font-semibold text-xs mt-1.5">
                        <div className="flex items-center gap-1">
                          <span className="material-symbols-outlined text-[16px]">calendar_today</span>
                          <span>{formatTime(contest.startTime)} – {formatTime(contest.endTime)}</span>
                        </div>
                        <span className="text-gray-300">•</span>
                        <div className="flex items-center gap-1">
                          <span className="material-symbols-outlined text-[16px]">timer</span>
                          <span>{contest.durations} Mins</span>
                        </div>
                        <span className="text-gray-300">•</span>
                        <div className="flex items-center gap-1">
                          <span className="material-symbols-outlined text-[16px]">person</span>
                          <span>Created by {contest.creatorName}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Enter Arena button accompanied by (participantCount + Competitors) */}
                  <div className="flex flex-row md:flex-col items-center md:items-end justify-between md:justify-center gap-4 border-t md:border-t-0 md:border-l border-gray-100 pt-4 md:pt-0 md:pl-6 shrink-0">
                    <div className="flex flex-col items-center md:items-end gap-1.5">
                      <Link 
                        to={`/contests/${contest.id}`} 
                        className="px-5 py-2.5 bg-brand-green hover:bg-green-600 text-white font-extrabold text-xs rounded-xl shadow-md transition-all transform hover:-translate-y-0.5 text-center min-w-[120px]"
                      >
                        Enter Arena
                      </Link>
                      <span className="text-[11px] text-text-muted font-bold">
                        ({contest.participantCount || 0} Competitors)
                      </span>
                    </div>
                  </div>
                </article>
              ))
            )}
          </section>

          {/* Pagination Section */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6 rounded-2xl shadow-sm mt-4">
              <div className="flex flex-1 justify-between sm:hidden">
                <button
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage(prev => Math.max(prev - 1, 0))}
                  className="relative inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                >
                  Previous
                </button>
                <button
                  disabled={currentPage === totalPages - 1}
                  onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages - 1))}
                  className="relative ml-3 inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                >
                  Next
                </button>
              </div>
              <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm text-gray-700">
                    Showing page <span className="font-semibold">{currentPage + 1}</span> of{' '}
                    <span className="font-semibold">{totalPages}</span> (Total{' '}
                    <span className="font-semibold">{totalElements}</span> contests)
                  </p>
                </div>
                <div>
                  <nav className="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                    <button
                      disabled={currentPage === 0}
                      onClick={() => setCurrentPage(prev => Math.max(prev - 1, 0))}
                      className="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                    >
                      <span className="sr-only">Previous</span>
                      <span className="material-symbols-outlined text-[18px]">chevron_left</span>
                    </button>
                    {Array.from({ length: totalPages }).map((_, idx) => (
                      <button
                        key={idx}
                        onClick={() => setCurrentPage(idx)}
                        aria-current={currentPage === idx ? 'page' : undefined}
                        className={`relative inline-flex items-center px-4 py-2 text-sm font-semibold focus:z-20 ${
                          currentPage === idx
                            ? 'z-10 bg-primary text-white focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary'
                            : 'text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:outline-offset-0'
                        }`}
                      >
                        {idx + 1}
                      </button>
                    ))}
                    <button
                      disabled={currentPage === totalPages - 1}
                      onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages - 1))}
                      className="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                    >
                      <span className="sr-only">Next</span>
                      <span className="material-symbols-outlined text-[18px]">chevron_right</span>
                    </button>
                  </nav>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right: Sidebar Combat Profile Widget (1 col) */}
        <aside className="lg:col-span-1 flex flex-col gap-6 w-full lg:sticky lg:top-20 text-left">
          
          {user ? (
            /* User Combat Profile Widget */
            <div className="bg-surface rounded-2xl p-5 border border-gray-100 shadow-md flex flex-col gap-5">
              <div className="flex items-center gap-3">
                <img 
                  alt="User Avatar" 
                  className="w-11 h-11 rounded-full border-2 border-primary object-cover" 
                  onError={handleAvatarError} 
                  src={userStats?.avatarUrl || user.avatar || "https://ui-avatars.com/api/?name=You&background=12284C&color=fff"} 
                />
                <div>
                  <h4 className="text-sm font-bold text-brand-blue">{userStats?.displayName || user.name}</h4>
                </div>
              </div>
              
              <div className="h-px bg-gray-100"></div>
              
              {/* Battle Stats Grid */}
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-[#f8f9fa] rounded-xl p-3 border border-gray-50 flex flex-col">
                  <span className="text-[9px] uppercase tracking-wider text-text-muted font-bold">Contest Score</span>
                  <span className="text-lg font-black text-brand-blue mt-0.5">{userStats?.score?.toLocaleString() || '0'}</span>
                  <span className="text-[9px] text-[#46A040] font-bold flex items-center mt-1">
                    <span className="material-symbols-outlined text-[10px] font-extrabold">trending_up</span> Active
                  </span>
                </div>
                <div className="bg-[#f8f9fa] rounded-xl p-3 border border-gray-50 flex flex-col">
                  <span className="text-[9px] uppercase tracking-wider text-text-muted font-bold">Global Rank</span>
                  <span className="text-lg font-black text-brand-blue mt-0.5">#{userStats?.rank || 'N/A'}</span>
                  <span className="text-[9px] text-[#46A040] font-bold flex items-center mt-1">
                    Top {(((userStats?.rank || 1) / (userStats?.totalUsers || 1)) * 100).toFixed(1)}% global
                  </span>
                </div>
                <div className="bg-[#f8f9fa] rounded-xl p-3 border border-gray-50 flex flex-col">
                  <span className="text-[9px] uppercase tracking-wider text-text-muted font-bold">Contests</span>
                  <span className="text-lg font-black text-brand-blue mt-0.5">{userStats?.contestsCount || 0}</span>
                  <span className="text-[9px] text-text-muted font-medium mt-1">Attended events</span>
                </div>
                <div className="bg-[#f8f9fa] rounded-xl p-3 border border-gray-50 flex flex-col">
                  <span className="text-[9px] uppercase tracking-wider text-text-muted font-bold">Avg. Accuracy</span>
                  <span className="text-lg font-black text-[#46A040] mt-0.5">{userStats?.avgAccuracy || 0}%</span>
                  <span className="text-[9px] text-text-muted font-medium mt-1">Submission rate</span>
                </div>
              </div>
            </div>
          ) : (
            /* Guest Widget */
            <div className="bg-surface rounded-2xl p-6 border border-gray-100 shadow-md flex flex-col gap-4 text-center items-center">
              <span className="material-symbols-outlined text-primary text-4xl">account_circle</span>
              <h4 className="text-sm font-bold text-brand-blue">Combat Profile</h4>
              <p className="text-xs text-text-muted">Sign in to view your global ranking, scores, and battle statistics.</p>
              <Link to="/login" className="w-full py-2.5 bg-primary hover:bg-primary-hover text-white font-bold text-xs rounded-xl shadow-md transition-colors text-center">
                Log In
              </Link>
            </div>
          )}
        </aside>
      </div>

      {/* Registration Success Modal Overlay */}
      {isModalOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-brand-blue/60 backdrop-blur-md transition-all duration-300">
          <div className="bg-surface rounded-2xl p-8 max-w-sm w-full mx-4 border border-white/20 shadow-2xl relative text-center flex flex-col items-center gap-4 transition-all duration-300 scale-100">
            <div className="w-16 h-16 rounded-full bg-brand-green/10 text-brand-green flex items-center justify-center mb-2 pulse-glow-green">
              <span className="material-symbols-outlined text-4xl">check_circle</span>
            </div>
            <h3 className="text-xl font-bold text-brand-blue">Registration Successful!</h3>
            <p className="text-sm text-text-muted">
              You are successfully locked in for <strong className="text-brand-blue">{modalContestTitle}</strong>!
            </p>
            
            {/* Premium Ticket Detail */}
            <div className="w-full bg-[#f8f9fa] border border-dashed border-gray-300 rounded-xl p-4 my-2 flex flex-col gap-2 relative overflow-hidden text-left">
              {/* Ticket cutouts */}
              <div className="absolute left-0 top-1/2 -translate-y-1/2 -translate-x-2.5 w-5 h-5 rounded-full bg-brand-blue/5 border-r border-gray-300"></div>
              <div className="absolute right-0 top-1/2 -translate-y-1/2 translate-x-2.5 w-5 h-5 rounded-full bg-brand-blue/5 border-l border-gray-300"></div>
              
              <div className="flex justify-between items-center text-[10px] font-bold text-text-muted">
                <span>ENTRY PASS</span>
                <span className="text-primary font-extrabold">#NS-2026</span>
              </div>
              <div className="h-px bg-gray-200 border-dashed my-1"></div>
              <div className="flex justify-between items-center text-xs font-bold text-brand-blue">
                <span>TICKET ID:</span>
                <span className="font-mono text-sm tracking-wider text-primary">{modalTicketId}</span>
              </div>
            </div>
            
            <button 
              onClick={() => setIsModalOpen(false)} 
              className="w-full py-2.5 bg-brand-blue hover:bg-brand-blue-light text-white font-bold rounded-lg transition-colors shadow-md text-sm"
            >
              Awesome, Let's Go!
            </button>
          </div>
        </div>
      )}
    </main>
  );
};
