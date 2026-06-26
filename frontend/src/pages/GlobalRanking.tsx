import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { rankingService } from '../services/rankingService';
import type { RankingUser, UserRankStats } from '../services/rankingService';





export const GlobalRanking: React.FC = () => {
  const { user } = useApp();
  const [activeLeague, setActiveLeague] = useState<'all' | 'weekly' | 'monthly'>('all');
  const [podiumTransitioning, setPodiumTransitioning] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState<string>('');
  
  const [rankings, setRankings] = useState<RankingUser[]>([]);
  const [userStats, setUserStats] = useState<UserRankStats | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const itemsPerPage = 17; // 3 podium users + 17 table users = 20 users total per page

  useEffect(() => {
    const loadRankings = async () => {
      try {
        setLoading(true);
        const data = await rankingService.fetchGlobalRankings(activeLeague);
        setRankings(data);
        const stats = await rankingService.fetchUserRankStats(activeLeague);
        setUserStats(stats);
      } catch (err) {
        console.error("Error loading rankings:", err);
      } finally {
        setLoading(false);
      }
    };
    loadRankings();
  }, [activeLeague]);

  const handleLeagueSwitch = (league: 'all' | 'weekly' | 'monthly') => {
    if (league === activeLeague) return;
    setPodiumTransitioning(true);
    setTimeout(() => {
      setActiveLeague(league);
    }, 150);
    setTimeout(() => {
      setPodiumTransitioning(false);
    }, 300);
  };

  const filteredUsers = rankings.filter((u) =>
    u.name.toLowerCase().includes(searchQuery.toLowerCase().trim())
  );

  const tableUsers = filteredUsers.filter(u => u.rank > 3);

  const totalPages = Math.max(1, Math.ceil(tableUsers.length / itemsPerPage));

  useEffect(() => {
    setCurrentPage(1);
  }, [activeLeague, searchQuery]);

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [tableUsers.length, totalPages, currentPage]);

  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedUsers = tableUsers.slice(startIndex, startIndex + itemsPerPage);

  const p1 = rankings[0] ? {
    name: rankings[0].name,
    points: rankings[0].points.toLocaleString() + " pts",
    avatar: rankings[0].avatar
  } : {
    name: "TBD",
    points: "0 pts",
    avatar: ""
  };

  const p2 = rankings[1] ? {
    name: rankings[1].name,
    points: rankings[1].points.toLocaleString() + " pts",
    avatar: rankings[1].avatar
  } : {
    name: "TBD",
    points: "0 pts",
    avatar: ""
  };

  const p3 = rankings[2] ? {
    name: rankings[2].name,
    points: rankings[2].points.toLocaleString() + " pts",
    avatar: rankings[2].avatar
  } : {
    name: "TBD",
    points: "0 pts",
    avatar: ""
  };

  return (
    <>
      <style>{`
        .material-symbols-outlined {
            font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
        }
        .icon-fill {
            font-variation-settings: 'FILL' 1;
        }
        
        /* Premium Custom Animations & Keyframes */
        @keyframes float-slow {
            0%, 100% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-10px) rotate(0.5deg); }
        }
        @keyframes float-medium {
            0%, 100% { transform: translateY(0px); }
            50% { transform: translateY(-6px); }
        }
        @keyframes crown-bounce {
            0%, 100% { transform: translateY(0px) rotate(-5deg); }
            50% { transform: translateY(-4px) rotate(5deg); }
        }
        @keyframes pulse-glow {
            0%, 100% { box-shadow: 0 0 15px rgba(243, 111, 33, 0.3), inset 0 0 10px rgba(243, 111, 33, 0.05); border-color: rgba(243, 111, 33, 0.4); }
            50% { box-shadow: 0 0 25px rgba(243, 111, 33, 0.6), inset 0 0 15px rgba(243, 111, 33, 0.2); border-color: rgba(243, 111, 33, 0.8); }
        }
        @keyframes pulse-glow-green {
            0%, 100% { box-shadow: 0 0 15px rgba(34, 197, 94, 0.3), inset 0 0 10px rgba(34, 197, 94, 0.05); border-color: rgba(34, 197, 94, 0.4); }
            50% { box-shadow: 0 0 25px rgba(34, 197, 94, 0.6), inset 0 0 15px rgba(34, 197, 94, 0.2); border-color: rgba(34, 197, 94, 0.8); }
        }
        @keyframes pulse-glow-blue {
            0%, 100% { box-shadow: 0 0 15px rgba(59, 130, 246, 0.3), inset 0 0 10px rgba(59, 130, 246, 0.05); border-color: rgba(59, 130, 246, 0.4); }
            50% { box-shadow: 0 0 25px rgba(59, 130, 246, 0.6), inset 0 0 15px rgba(59, 130, 246, 0.2); border-color: rgba(59, 130, 246, 0.8); }
        }
        @keyframes shine {
            0% { left: -100%; }
            100% { left: 100%; }
        }

        .animate-float-slow {
            animation: float-slow 6s ease-in-out infinite;
        }
        .animate-float-medium {
            animation: float-medium 4s ease-in-out infinite;
        }
        .animate-crown {
            animation: crown-bounce 3s ease-in-out infinite;
        }
        .animate-pulse-glow {
            animation: pulse-glow 3s infinite;
        }
        .animate-pulse-glow-green {
            animation: pulse-glow-green 3s infinite;
        }
        .animate-pulse-glow-blue {
            animation: pulse-glow-blue 3s infinite;
        }
        
        .shine-effect {
            position: relative;
            overflow: hidden;
        }
        .shine-effect::after {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 50%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
            transform: skewX(-20deg);
            animation: shine 4s infinite;
        }

        .glassmorphism {
            background: rgba(255, 255, 255, 0.85);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.5);
        }

        .orange-border {
            border: 4px solid #E05E1A;
            box-shadow: 0 0 25px rgba(243, 111, 33, 0.45);
        }
        .blue-border {
            border: 4px solid #3b82f6;
            box-shadow: 0 0 25px rgba(59, 130, 246, 0.45);
        }
        .green-border {
            border: 4px solid #22c55e;
            box-shadow: 0 0 25px rgba(34, 197, 94, 0.45);
        }

        /* Highlight current user in leaderboard */
        .current-user-row {
            background-color: rgba(243, 111, 33, 0.08) !important;
        }
        .current-user-row td {
            font-weight: 600;
        }
        .current-user-row td:first-child {
            border-left: 4px solid #F36F21 !important;
        }
        #leaderboard-list tr td:first-child {
            border-left: 4px solid transparent;
            transition: border-color 0.2s ease;
        }
        
        /* Smooth scale transition for ranking rows */
        .ranking-row {
            transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .ranking-row:hover {
            transform: scale(1.01) translateY(-2px);
        }
        
        /* Hide scrollbar for Chrome, Safari and Opera */
        .no-scrollbar::-webkit-scrollbar {
            display: none;
        }
        /* Hide scrollbar for IE, Edge and Firefox */
        .no-scrollbar {
            -ms-overflow-style: none;  /* IE and Edge */
            scrollbar-width: none;  /* Firefox */
        }
      `}</style>

      {/* Glowing Backdrop Circles */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        <div className="absolute -top-40 -left-40 w-[500px] h-[500px] bg-[#F36F21]/10 rounded-full blur-[120px]"></div>
        <div className="absolute top-1/3 -right-40 w-[500px] h-[500px] bg-[#12284C]/10 rounded-full blur-[120px]"></div>
        <div className="absolute -bottom-40 left-1/4 w-[600px] h-[600px] bg-[#3b82f6]/5 rounded-full blur-[150px]"></div>
      </div>

      {/* Main Layout Container */}
      <div className="flex-grow flex flex-col w-full max-w-[1440px] mx-auto px-4 lg:px-8 pt-4 pb-8 md:pt-6 md:pb-12 z-10">
        {/* Header Text */}
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-6">
          <div>
            {/* Decorative Badge */}
            <div className="inline-flex items-center gap-1.5 bg-[#fce2d3] border border-primary/20 px-3 py-1 rounded-full text-primary font-bold text-xs uppercase tracking-wider mb-3 shadow-sm">
              <span className="material-symbols-outlined text-xs icon-fill">military_tech</span> Season 2026 Active
            </div>
            <h1 className="text-display-lg-mobile md:text-display-lg font-display font-black leading-tight">
              <span className="bg-gradient-to-r from-[#0114a7] to-[#2563eb] bg-clip-text text-transparent">Global</span>{' '}
              <span className="bg-gradient-to-r from-[#ff6000] to-[#ff8c42] bg-clip-text text-transparent">Rankings</span>
            </h1>
            <p className="text-body-md text-text-muted mt-2 max-w-lg">See who's leading the charts in coding mastery.</p>
          </div>

          {/* Filter Tab System and Search Bar */}
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
            {/* Search bar */}
            <div className="relative min-w-[240px]">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-text-muted text-[20px]">search</span>
              <input
                id="rank-search"
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search coders..."
                className="w-full pl-10 pr-4 py-2 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary text-sm bg-white/70 backdrop-blur-sm transition-all shadow-sm"
              />
            </div>

            {/* Main Filter buttons */}
            <div className="flex items-center gap-2 bg-white/70 backdrop-blur-sm p-1.5 rounded-xl border border-gray-200 shadow-sm w-fit">
              <button
                id="tab-all-time"
                onClick={() => handleLeagueSwitch('all')}
                className={`px-4 py-1.5 rounded-lg text-xs font-bold tracking-wide uppercase transition-all duration-200 ${
                  activeLeague === 'all'
                    ? 'bg-brand-blue text-white shadow-sm'
                    : 'text-text-muted hover:text-primary'
                }`}
              >
                All Time
              </button>
              <button
                id="tab-weekly"
                onClick={() => handleLeagueSwitch('weekly')}
                className={`px-4 py-1.5 rounded-lg text-xs font-bold tracking-wide uppercase transition-all duration-200 ${
                  activeLeague === 'weekly'
                    ? 'bg-brand-blue text-white shadow-sm'
                    : 'text-text-muted hover:text-primary'
                }`}
              >
                Weekly
              </button>
              <button
                id="tab-monthly"
                onClick={() => handleLeagueSwitch('monthly')}
                className={`px-4 py-1.5 rounded-lg text-xs font-bold tracking-wide uppercase transition-all duration-200 ${
                  activeLeague === 'monthly'
                    ? 'bg-brand-blue text-white shadow-sm'
                    : 'text-text-muted hover:text-primary'
                }`}
              >
                Monthly
              </button>
            </div>
          </div>
        </div>

        {/* Podium Section with Luxury Glassmorphic Design */}
        <div
          id="podium-wrapper"
          className={`grid grid-cols-1 md:grid-cols-3 gap-6 items-end justify-center mb-16 mt-4 w-full transition-all duration-300 ${
            podiumTransitioning ? 'opacity-50 translate-y-1.5' : 'opacity-100 translate-y-0'
          }`}
        >
          {/* Rank 2: Green (Appears on left on desktop) */}
          <div className="flex flex-col items-center order-2 md:order-1 animate-float-medium" style={{ animationDelay: '0.5s' }}>
            {/* Floating Medal Icon for Top 2 (Green) */}
            <div className="text-brand-green animate-crown mb-2 relative z-10 flex flex-col items-center" style={{ animationDelay: '0.3s' }}>
              <span className="material-symbols-outlined text-[38px] icon-fill text-[#22c55e] drop-shadow-[0_4px_10px_rgba(34,197,94,0.3)]">military_tech</span>
              <div className="h-1 w-6 bg-[#22c55e]/30 rounded-full blur-xs opacity-50 mt-1"></div>
            </div>
            <div className="w-full glassmorphism rounded-2xl p-7 shadow-xl border-t-[6px] border-[#22c55e] relative overflow-hidden transition-all duration-300 hover:shadow-2xl hover:scale-[1.025] animate-pulse-glow-green">
              {/* Decorative background rank */}
              <div className="absolute -right-6 -bottom-6 text-[100px] font-black font-display text-[#22c55e]/5 pointer-events-none select-none">2</div>

              {/* Profile Image & Badge */}
              <div className="flex flex-col items-center text-center">
                <div className="relative mb-4">
                  <img
                    src={p2.avatar}
                    alt="Rank 2 Avatar"
                    className="w-20 h-20 rounded-full object-cover green-border"
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = `https://ui-avatars.com/api/?name=${encodeURIComponent(p2.name)}&background=22c55e&color=fff`;
                    }}
                  />
                  <div className="absolute -bottom-2 left-1/2 -translate-x-1/2 bg-[#22c55e] text-white w-7 h-7 rounded-full flex items-center justify-center font-bold text-sm border-2 border-white shadow-md">2</div>
                </div>

                <h3 id="p2-name" className="font-display font-bold text-lg text-[#22c55e]">{p2.name}</h3>

                {/* Stats breakdown */}
                <div className="w-full mt-6 pt-4 border-t border-gray-100 text-center">
                  <span className="block text-caption text-text-muted font-semibold uppercase">Score</span>
                  <span id="p2-points" className="font-display font-black text-[#22c55e] text-lg">{p2.points}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Rank 1: Orange (Center piece, tallest, most prominent) */}
          <div className="flex flex-col items-center order-1 md:order-2 animate-float-slow">
            {/* Floating Animated Trophy Cup instead of King Bed */}
            <div className="text-primary animate-crown mb-2 relative z-10 flex flex-col items-center">
              <span className="material-symbols-outlined text-[52px] icon-fill text-[#F36F21] drop-shadow-[0_4px_12px_rgba(243,111,33,0.5)]">trophy</span>
              <div className="h-1.5 w-8 bg-[#F36F21]/40 rounded-full blur-xs opacity-60 mt-1"></div>
            </div>

            <div className="w-full glassmorphism rounded-2xl p-8 shadow-2xl border-t-[8px] border-[#F36F21] relative overflow-hidden transition-all duration-300 hover:shadow-primary/20 hover:scale-[1.03] shine-effect animate-pulse-glow">
              {/* Glowing Aura */}
              <div className="absolute -top-12 -left-12 w-28 h-28 bg-[#F36F21]/20 rounded-full blur-2xl"></div>
              <div className="absolute -right-6 -bottom-6 text-[120px] font-black font-display text-[#F36F21]/10 pointer-events-none select-none">1</div>

              <div className="flex flex-col items-center text-center">
                {/* Profile Image */}
                <div className="relative mb-5">
                  <img
                    src={p1.avatar}
                    alt="Rank 1 Avatar"
                    className="w-24 h-24 rounded-full object-cover orange-border shadow-lg"
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = `https://ui-avatars.com/api/?name=${encodeURIComponent(p1.name)}&background=F36F21&color=fff`;
                    }}
                  />
                  <div className="absolute -bottom-2.5 left-1/2 -translate-x-1/2 bg-[#F36F21] text-white w-9 h-9 rounded-full flex items-center justify-center font-bold text-lg border-2 border-white shadow-md">1</div>
                </div>

                <h3 id="p1-name" className="font-display font-black text-xl text-[#F36F21] tracking-tight">{p1.name}</h3>
                {/* Stats breakdown */}
                <div className="w-full mt-6 pt-5 border-t border-[#F36F21]/10 text-center">
                  <span className="block text-caption text-text-muted font-semibold uppercase">Score</span>
                  <span id="p1-points" className="font-display font-black text-[#F36F21] text-xl">{p1.points}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Rank 3: Blue (Least prominent of the top three) */}
          <div className="flex flex-col items-center order-3 animate-float-medium" style={{ animationDelay: '1s' }}>
            {/* Floating Badge for Top 3 (Blue) */}
            <div className="text-brand-blue animate-crown mb-2 relative z-10 flex flex-col items-center" style={{ animationDelay: '0.6s' }}>
              <span className="material-symbols-outlined text-[30px] icon-fill text-[#3b82f6] drop-shadow-[0_4px_8px_rgba(59,130,246,0.3)]">workspace_premium</span>
              <div className="h-1 w-5 bg-[#3b82f6]/30 rounded-full blur-xs opacity-40 mt-1"></div>
            </div>

            <div className="w-full glassmorphism rounded-2xl p-5 shadow-md border-t-[5px] border-[#3b82f6] relative overflow-hidden transition-all duration-300 hover:shadow-lg hover:scale-100 animate-pulse-glow-blue">
              {/* Decorative background rank */}
              <div className="absolute -right-6 -bottom-6 text-[100px] font-black font-display text-[#3b82f6]/5 pointer-events-none select-none">3</div>

              {/* Profile Image & Badge */}
              <div className="flex flex-col items-center text-center">
                <div className="relative mb-4">
                  <img
                    src={p3.avatar}
                    alt="Rank 3 Avatar"
                    className="w-16 h-16 rounded-full object-cover blue-border"
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = `https://ui-avatars.com/api/?name=${encodeURIComponent(p3.name)}&background=3b82f6&color=fff`;
                    }}
                  />
                  <div className="absolute -bottom-2 left-1/2 -translate-x-1/2 bg-[#3b82f6] text-white w-7 h-7 rounded-full flex items-center justify-center font-bold text-xs border-2 border-white shadow-md">3</div>
                </div>

                <h3 id="p3-name" className="font-display font-bold text-md text-[#3b82f6]">{p3.name}</h3>
                {/* Stats breakdown */}
                <div className="w-full mt-6 pt-4 border-t border-gray-100 text-center">
                  <span className="block text-caption text-text-muted font-semibold uppercase">Score</span>
                  <span id="p3-points" className="font-display font-black text-[#3b82f6] text-md">{p3.points}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Current User Rank Section: Simplified, Premium & Ultra-Clean */}
        <div className="w-full max-w-5xl mx-auto mb-10 px-0 sm:px-4">
          <div className="bg-gradient-to-r from-brand-blue to-[#1a386b] rounded-2xl shadow-xl overflow-hidden relative border border-primary/20 animate-pulse-glow">
            <div className="p-5 md:p-6 flex flex-col md:flex-row items-center justify-between gap-6 relative z-10">
              <div className="flex items-center gap-4 w-full md:w-auto">
                {/* User Avatar */}
                <div className="relative shrink-0">
                  <img
                    src={user?.avatar || "https://lh3.googleusercontent.com/aida-public/AHOXywsx8o4BvW3D2tXv5_X0a_xZ0x_xZ0x_xZ0x_xZ0x_xZ0x_xZ0x_xZ0x_xZ0"}
                    alt="Your Avatar"
                    className="w-12 h-12 md:w-16 md:h-16 rounded-full border-2 border-primary object-cover"
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = 'https://ui-avatars.com/api/?name=You&background=F36F21&color=fff';
                    }}
                  />
                </div>

                <div className="flex flex-col">
                  <span className="text-[10px] text-primary font-black uppercase tracking-wider">Your Stats</span>
                  <h2 className="font-display font-extrabold text-white text-lg md:text-xl tracking-tight leading-tight">
                    {user?.name ? `${user.name}` : "Guest"}
                  </h2>
                  {/* Simplified horizontal badges for Rank and Score */}
                  <div className="flex items-center gap-2 mt-1.5">
                    <span className="bg-primary/20 text-primary border border-primary/30 px-2 py-0.5 rounded-lg text-xs font-black">
                      Rank #{userStats && userStats.rank > 0 ? userStats.rank : '--'}
                    </span>
                    <span className="bg-white/10 text-white border border-white/10 px-2 py-0.5 rounded-lg text-xs font-bold">
                      Score: {userStats ? userStats.points.toLocaleString() : '0'} pts
                    </span>
                  </div>
                </div>
              </div>

              {/* Gamified Progression & Motivation Bar */}
              {user ? (
                <div className="flex-grow max-w-sm w-full">
                  {!userStats ? (
                    <div className="text-white/70 text-xs">Loading your stats...</div>
                  ) : userStats.rank === 1 ? (
                    <>
                      <div className="flex justify-between items-center text-xs text-white/80 font-bold mb-1.5">
                        <span className="flex items-center gap-1">
                          <span className="material-symbols-outlined text-[14px] text-primary icon-fill">military_tech</span> Leading the leaderboard!
                        </span>
                        <span className="text-primary font-black">Top Rank</span>
                      </div>
                      <div className="w-full h-2 bg-white/15 rounded-full overflow-hidden p-0.5">
                        <div className="h-full bg-gradient-to-r from-[#ff6000] to-[#ff8c42] rounded-full shadow-[0_0_8px_rgba(243,111,33,0.5)]" style={{ width: '100%' }}></div>
                      </div>
                      <p className="text-[11px] text-white/60 mt-1.5 italic">
                        You are Rank #1 on the leaderboard! Keep up the great work.
                      </p>
                    </>
                  ) : userStats.rank > 1 ? (
                    <>
                      <div className="flex justify-between items-center text-xs text-white/80 font-bold mb-1.5">
                        <span className="flex items-center gap-1">
                          <span className="material-symbols-outlined text-[14px] text-primary icon-fill">arrow_circle_up</span> Surpass the next rank
                        </span>
                        <span className="text-primary font-black">{userStats.pointsToNextRank.toLocaleString()} pts to go</span>
                      </div>
                      {/* Custom sleek progress bar */}
                      <div className="w-full h-2 bg-white/15 rounded-full overflow-hidden p-0.5">
                        <div 
                          className="h-full bg-gradient-to-r from-[#ff6000] to-[#ff8c42] rounded-full shadow-[0_0_8px_rgba(243,111,33,0.5)] transition-all duration-500" 
                          style={{ width: `${userStats.points + userStats.pointsToNextRank > 0 ? Math.max(10, Math.min(90, (userStats.points / (userStats.points + userStats.pointsToNextRank)) * 100)) : 10}%` }}
                        ></div>
                      </div>
                      <p className="text-[11px] text-white/60 mt-1.5 italic">
                        Need {userStats.pointsToNextRank.toLocaleString()} pts to surpass #{userStats.rank - 1} {userStats.nextRankUserName}! Solve more problems to rank up.
                      </p>
                    </>
                  ) : (
                    <>
                      <div className="flex justify-between items-center text-xs text-white/80 font-bold mb-1.5">
                        <span className="flex items-center gap-1">
                          <span className="material-symbols-outlined text-[14px] text-primary icon-fill">pending</span> Not ranked yet
                        </span>
                        <span className="text-white/60 font-black">Unranked</span>
                      </div>
                      <div className="w-full h-2 bg-white/15 rounded-full overflow-hidden p-0.5">
                        <div className="h-full bg-gray-500" style={{ width: '0%' }}></div>
                      </div>
                      <p className="text-[11px] text-white/60 mt-1.5 italic">
                        You don't have a score on the leaderboard yet. Solve practice problems to start competing!
                      </p>
                    </>
                  )}
                </div>
              ) : (
                <div className="flex-grow max-w-sm w-full text-white/70 text-xs font-medium">
                  Log in to view your position on the leaderboard and track your learning progress!
                </div>
              )}

              {/* Action Button */}
              <div className="shrink-0 w-full md:w-auto text-center">
                {user ? (
                  <Link
                    to="/problems"
                    className="inline-flex items-center justify-center gap-2 w-full md:w-auto bg-primary hover:bg-primary-hover text-white font-bold text-xs tracking-wide uppercase px-5 py-2.5 rounded-xl transition-all duration-300 shadow-md shadow-primary/20 group"
                  >
                    Practice Now!
                    <span className="material-symbols-outlined text-xs group-hover:translate-x-0.5 transition-transform">swords</span>
                  </Link>
                ) : (
                  <Link
                    to="/login"
                    className="inline-flex items-center justify-center gap-2 w-full md:w-auto bg-primary hover:bg-primary-hover text-white font-bold text-xs tracking-wide uppercase px-5 py-2.5 rounded-xl transition-all duration-300 shadow-md shadow-primary/20 group"
                  >
                    Log In
                    <span className="material-symbols-outlined text-xs group-hover:translate-x-0.5 transition-transform">login</span>
                  </Link>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Leaderboard Table Section for Rank 4+ */}
        <div className="bg-surface rounded-xl shadow-[0_4px_20px_rgba(18,40,76,0.05)] border border-gray-200 overflow-hidden mb-12 max-w-5xl mx-auto w-full">
          {/* Live Counter in Header */}
          <div className="px-6 py-4 bg-surface-gray border-b border-gray-200 flex items-center justify-between">
            <span className="text-sm font-bold text-brand-blue">Global Competitors</span>
            <span id="result-count" className="bg-brand-blue/5 text-brand-blue font-bold px-3 py-1 rounded-full text-xs">
              Showing {tableUsers.length} Player{tableUsers.length !== 1 ? 's' : ''}
            </span>
          </div>

          {loading ? (
            <div className="flex flex-col items-center justify-center py-16 text-primary">
              <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary mb-4"></div>
              <p className="text-body-md text-text-muted">Loading competitors...</p>
            </div>
          ) : tableUsers.length === 0 ? (
            <div className="text-center py-16 text-text-muted">
              No competitors found.
            </div>
          ) : (
            <>
              <div className="overflow-x-auto no-scrollbar">
                <table className="w-full text-left border-collapse min-w-[600px]">
                  <thead>
                    <tr className="bg-surface-gray text-text-muted font-label text-label-md border-b border-gray-200">
                      <th className="p-4 font-semibold w-20 text-center">Rank</th>
                      <th className="p-4 font-semibold">User</th>
                      <th className="p-4 font-semibold text-right pr-8">Score</th>
                    </tr>
                  </thead>
                  <tbody id="leaderboard-list" className="text-body-md font-body text-text-main divide-y divide-gray-100">
                    {paginatedUsers.map((u) => {
                      const isCurrentUser = user && (String(u.userId) === String(user.id) || u.name === user.name);
                      return (
                        <tr
                          key={u.name}
                          className={`hover:bg-surface-gray/50 transition-colors group cursor-pointer ranking-row ${isCurrentUser ? 'current-user-row' : ''}`}
                          data-name={u.name}
                        >
                          <td className="p-4 text-center font-bold text-text-muted group-hover:text-text-main text-lg w-20">
                            <div className="flex flex-col items-center justify-center">
                              <span>{u.rank}</span>
                              <span className="text-[10px] text-slate-400 font-bold flex items-center gap-0.5">▬</span>
                            </div>
                          </td>
                          <td className="p-4">
                            <div className="flex items-center gap-4">
                              <img
                                src={u.avatar}
                                alt="User Avatar"
                                className="w-12 h-12 rounded-full object-cover border border-outline-variant/50"
                                onError={(e) => {
                                  (e.target as HTMLImageElement).src = `https://ui-avatars.com/api/?name=${encodeURIComponent(u.name)}&background=cbd5e1&color=64748b`;
                                }}
                              />
                              <div className="flex flex-col">
                                <div className="flex items-center gap-2">
                                  <span className="font-bold text-text-main group-hover:text-primary transition-colors">{u.name}</span>
                                  {isCurrentUser && (
                                    <span className="bg-primary/20 text-primary border border-primary/30 px-1.5 py-0.5 rounded text-[10px] font-black uppercase">
                                      You
                                    </span>
                                  )}
                                </div>
                              </div>
                            </div>
                          </td>
                          <td className="p-4 text-right pr-8 font-bold text-brand-blue text-lg">
                            {u.points.toLocaleString()} <span className="text-xs text-text-muted font-normal">pts</span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="px-6 py-4 border-t border-gray-200 bg-surface flex items-center justify-between">
                  <span className="text-sm text-text-muted hidden sm:inline">
                    Showing {4 + startIndex} to {4 + startIndex + paginatedUsers.length - 1} of {rankings.length} users
                  </span>
                  <div className="flex items-center gap-1 sm:ml-auto">
                    <button
                      onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                      disabled={currentPage === 1}
                      className="w-8 h-8 flex items-center justify-center rounded border border-outline-variant/50 text-text-muted hover:bg-surface-gray disabled:opacity-50"
                    >
                      <span className="material-symbols-outlined text-sm">chevron_left</span>
                    </button>
                    
                    {/* Dynamic Page Buttons */}
                    {Array.from({ length: totalPages }, (_, idx) => idx + 1)
                      .filter(page => page === 1 || page === totalPages || Math.abs(page - currentPage) <= 2)
                      .map((pageNum, idx, arr) => {
                        const showDots = idx > 0 && pageNum - arr[idx - 1] > 1;
                        return (
                          <React.Fragment key={pageNum}>
                            {showDots && <span className="w-8 h-8 flex items-center justify-center text-text-muted">...</span>}
                            <button
                              onClick={() => setCurrentPage(pageNum)}
                              className={`w-8 h-8 flex items-center justify-center rounded font-medium text-sm transition-colors ${
                                currentPage === pageNum
                                  ? 'bg-primary text-white font-bold'
                                  : 'border border-outline-variant/50 text-text-muted hover:bg-surface-gray hover:text-primary transition-colors text-sm font-medium'
                              }`}
                            >
                              {pageNum}
                            </button>
                          </React.Fragment>
                        );
                      })}

                    <button
                      onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
                      disabled={currentPage === totalPages}
                      className="w-8 h-8 flex items-center justify-center rounded border border-outline-variant/50 text-text-muted hover:bg-surface-gray disabled:opacity-50"
                    >
                      <span className="material-symbols-outlined text-sm">chevron_right</span>
                    </button>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </>
  );
};
