import React, { useState, useEffect, useRef } from 'react';
import { Outlet, Link, NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { ContestSidebar } from './ContestSidebar';

export interface ContestOverviewData {
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
  participantCount: number;
  problemCount: number;
  isUserRegistered: boolean;
}

export const Layout: React.FC = () => {
  const { user, cart, logout } = useApp();
  const navigate = useNavigate();
  const location = useLocation();

  const isInstructorRoute = location.pathname.startsWith('/instructor');
  const isAdminRoute = location.pathname.startsWith('/admin');
  const isProblemSolvePage = location.pathname.startsWith('/problems/');

  // Parse contestId from location pathname
  const match = location.pathname.match(/^\/contests\/(\d+)/);
  const contestId = match ? match[1] : undefined;
  const isContestPage = !!contestId;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // const [appealReasonText, setAppealReasonText] = useState('');
  // const [isSubmittingAppeal, setIsSubmittingAppeal] = useState(false);
  // const [appealError, setAppealError] = useState<string | null>(null);

  // const handleAppealSubmit = async (e: React.FormEvent) => {
  //   e.preventDefault();
  //   if (!appealReasonText.trim()) {
  //     setAppealError('Vui lòng nhập nội dung khiếu nại.');
  //     return;
  //   }
  //   setIsSubmittingAppeal(true);
  //   setAppealError(null);
  //   try {
  //     await authService.submitAppeal(appealReasonText.trim());
  //     if (updateUser) {
  //       updateUser({ lockAppeal: appealReasonText.trim() });
  //     }
  //   } catch (err: any) {
  //     setAppealError(err.message || 'Gửi khiếu nại thất bại. Vui lòng thử lại.');
  //   } finally {
  //     setIsSubmittingAppeal(false);
  //   }
  // };

  // Contest State
  const [contest, setContest] = useState<ContestOverviewData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [timeLeft, setTimeLeft] = useState<string>('--:--:--');
  const [timerLabel, setTimerLabel] = useState<string>('Ends In');
  const [password, setPassword] = useState('');
  const [registering, setRegistering] = useState(false);
  const [registrationMessage, setRegistrationMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const timeOffsetRef = useRef<number>(0);

  const fetchContest = async () => {
    if (!contestId) return;
    try {
      const response = await fetch(`http://localhost:8080/nonstopcoding/contests/${contestId}`, {
        credentials: 'include',
      });
      const data = await response.json();
      if (data && data.result) {
        setContest(data.result);
        setError(null);
        if (data.timestamp) {
          timeOffsetRef.current = new Date(data.timestamp).getTime() - Date.now();
        }
      } else {
        setError(data.message || 'Failed to fetch contest details');
      }
    } catch (err) {
      console.error('Error fetching contest:', err);
      setError('Failed to fetch contest details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isContestPage && contestId) {
      setLoading(true);
      fetchContest();
    } else {
      setContest(null);
      setLoading(false);
      setError(null);
    }
  }, [contestId, isContestPage]);

  useEffect(() => {
    if (!contest || !isContestPage) return;

    if (contest.status === 'ENDED') {
      setTimeLeft('Ended');
      setTimerLabel('Contest Ended');
      return;
    }

    const targetTime = contest.status === 'UPCOMING' ? contest.startTime : contest.endTime;
    const label = contest.status === 'UPCOMING' ? 'Begins In' : 'Ends In';
    setTimerLabel(label);

    const updateTimer = () => {
      const now = Date.now() + timeOffsetRef.current;
      const end = new Date(targetTime).getTime();
      const diff = end - now;

      if (diff <= 0) {
        fetchContest();
        setTimeLeft('Ended');
        return;
      }

      const hrs = Math.floor(diff / (1000 * 60 * 60));
      const mins = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const secs = Math.floor((diff % (1000 * 60)) / 1000);

      const pad = (n: number) => String(n).padStart(2, '0');
      setTimeLeft(`${pad(hrs)}:${pad(mins)}:${pad(secs)}`);
    };

    updateTimer();
    const timer = setInterval(updateTimer, 1000);
    return () => clearInterval(timer);
  }, [contest, isContestPage]);

  let activeTab: 'overview' | 'problems' | 'submissions' | 'ranking' = 'overview';
  if (location.pathname.includes('/problems')) {
    activeTab = 'problems';
  } else if (location.pathname.includes('/submissions')) {
    activeTab = 'submissions';
  } else if (location.pathname.includes('/ranking')) {
    activeTab = 'ranking';
  }

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!contestId) return;
    if (contest?.isPrivate && !password.trim()) {
      setRegistrationMessage({
        type: 'error',
        text: 'Please enter the contest password',
      });
      return;
    }

    setRegistering(true);
    try {
      const response = await fetch(`http://localhost:8080/nonstopcoding/contests/${contestId}/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ password: contest?.isPrivate ? password : null }),
        credentials: 'include',
      });
      const data = await response.json();
      if (response.ok && data.code === 1000) {
        setRegistrationMessage({
          type: 'success',
          text: 'Successfully registered for the contest!',
        });
        setPassword('');
        await fetchContest();
      } else {
        setRegistrationMessage({
          type: 'error',
          text: data.message || 'Registration failed',
        });
      }
    } catch (err) {
      console.error(err);
      setRegistrationMessage({
        type: 'error',
        text: 'Connection error. Please try again.',
      });
    } finally {
      setRegistering(false);
    }

    setTimeout(() => {
      setRegistrationMessage(null);
    }, 4000);
  };

  // Redirection / Protection logic
  const privateRoutes = ['/dashboard', '/instructor', '/wallet-transaction', '/payment-transaction', '/shopping-cart'];
  const isPrivateRoute = privateRoutes.some(route => location.pathname.startsWith(route));

  React.useEffect(() => {
    // If not logged in and trying to access private routes, redirect to root Home page
    if (!user && isPrivateRoute) {
      navigate('/', { replace: true });
    }
  }, [user, isPrivateRoute, navigate]);


  return (
    <div className="bg-[#f0f4f9] text-text-main font-body min-h-screen flex flex-col antialiased selection:bg-primary-light selection:text-brand-blue relative">

      {/* LOCKED ACCOUNT MODAL OVERLAY */}
      {user && user.status === 'LOCKED' && (
        <div className="fixed inset-0 bg-slate-900/80 backdrop-blur-md z-[9999] flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl border border-slate-100 shadow-2xl max-w-md w-full p-8 text-left animate-fade-in flex flex-col gap-6">
            <div className="flex flex-col items-center text-center gap-3">
              <div className="w-16 h-16 rounded-full bg-red-50 flex items-center justify-center border border-red-100 text-red-500 animate-pulse">
                <span className="material-symbols-outlined text-4xl">lock</span>
              </div>
              <h2 className="font-display font-black text-2xl text-red-600">Account Locked</h2>
              <p className="text-xs text-text-muted max-w-sm leading-relaxed">
                The account <strong>@{user.username}</strong> has been locked. Please contact our support team via Gmail to request an unlock.
              </p>
            </div>

            <div className="bg-red-50/50 rounded-2xl border border-red-100/50 p-5 space-y-4">
              <div>
                <span className="text-[10px] font-black uppercase tracking-wider text-red-500">Lock Reason</span>
                <p className="text-xs text-slate-700 font-semibold mt-1 leading-relaxed bg-white/80 border border-slate-100 p-3 rounded-xl">
                  {user.lockReason || 'Violation of platform terms of service or security guidelines.'}
                </p>
              </div>
              
              <div className="flex items-center gap-2 text-xs text-slate-600 pt-1 border-t border-red-100/30">
                <span className="material-symbols-outlined text-base text-red-500">mail</span>
                <span>Support Gmail: <a href="mailto:nonstopcoding.support@gmail.com" className="font-bold text-primary hover:underline">nonstopcoding.support@gmail.com</a></span>
              </div>
            </div>

            <button
              type="button"
              onClick={handleLogout}
              className="w-full bg-primary hover:bg-primary-hover text-white font-bold text-xs py-3 rounded-xl transition-all shadow-md flex items-center justify-center gap-1.5 cursor-pointer transform hover:-translate-y-0.5 active:translate-y-0"
            >
              <span className="material-symbols-outlined text-sm">arrow_back</span>
              Go Back
            </button>
          </div>
        </div>
      )}

      {/* Glowing Backdrop Circles */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        <div className="absolute -top-40 -left-40 w-[500px] h-[500px] bg-primary/10 rounded-full blur-[120px]"></div>
        <div className="absolute top-1/3 -right-40 w-[500px] h-[500px] bg-brand-blue/10 rounded-full blur-[120px]"></div>
        <div className="absolute -bottom-40 left-1/4 w-[600px] h-[600px] bg-brand-green/5 rounded-full blur-[150px]"></div>
      </div>

      {/* TopAppBar */}
      {!isInstructorRoute && !isAdminRoute && (
        <header className="bg-surface/90 backdrop-blur-md shadow-sm fixed top-0 z-50 w-full border-b border-gray-100/50">
          <div className="flex justify-between items-center w-full px-8 h-16 max-w-[1440px] mx-auto relative">
            {/* Brand */}
            <Link to="/" className="shrink-0 flex items-center cursor-pointer">
              <img src={`${import.meta.env.BASE_URL}LOGO.png`} alt="Nonstop Coding Logo" className="h-16 w-auto" />
            </Link>
            <nav className="hidden lg:flex gap-6 items-center absolute left-1/2 transform -translate-x-1/2 h-full">
              {user && (
                <NavLink className={({ isActive }) => `font-body text-body-md transition-colors font-medium px-2 py-1 ${isActive ? 'text-primary' : 'text-text-main hover:text-primary'}`} to="/dashboard">My Learning</NavLink>
              )}
              <NavLink className={({ isActive }) => `font-body text-body-md transition-colors font-medium px-2 py-1 ${isActive ? 'text-primary' : 'text-text-main hover:text-primary'}`} to="/courses">Courses</NavLink>
              <NavLink className={({ isActive }) => `font-body text-body-md transition-colors font-medium px-2 py-1 ${isActive ? 'text-primary' : 'text-text-main hover:text-primary'}`} to="/problems">Problems</NavLink>
              <NavLink className={({ isActive }) => `font-body text-body-md transition-colors font-medium px-2 py-1 ${isActive ? 'text-primary' : 'text-text-main hover:text-primary'}`} to="/contests">Contests</NavLink>
              <NavLink className={({ isActive }) => `font-body text-body-md transition-colors font-medium px-2 py-1 ${isActive ? 'text-primary' : 'text-text-main hover:text-primary'}`} to="/rankings">Rankings</NavLink>
            </nav>
            <div className="flex items-center gap-4">
              {/* Instructor Capsule Link */}
              {user && user.role === 'instructor' && (
                <Link to="/instructor" className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-primary-light/40 text-primary hover:bg-primary hover:text-white font-semibold text-xs md:text-sm transition-all select-none border border-primary/20 shrink-0">
                  <span className="material-symbols-outlined text-[16px] md:text-[18px] icon-fill">school</span>
                  <span>Instructor</span>
                </Link>
              )}
              {/* Admin Capsule Link */}
              {user && user.role === 'admin' && (
                <Link to="/admin" className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-primary-light/40 text-primary hover:bg-primary hover:text-white font-semibold text-xs md:text-sm transition-all select-none border border-primary/20 shrink-0">
                  <span className="material-symbols-outlined text-[16px] md:text-[18px] icon-fill">admin_panel_settings</span>
                  <span>Admin</span>
                </Link>
              )}
              <button className="p-2 rounded-full text-text-muted hover:text-primary hover:bg-surface-gray transition-all">
                <span className="material-symbols-outlined">notifications</span>
              </button>
              <Link to="/shopping-cart" className="p-2 rounded-full text-text-muted hover:text-primary hover:bg-surface-gray transition-all relative">
                <span className="material-symbols-outlined">shopping_cart</span>
                {cart.length > 0 && (
                  <span className="absolute top-1 right-0 bg-primary text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full leading-none">{cart.length}</span>
                )}
              </Link>
              {user ? (
                <div className="relative flex items-center gap-1 cursor-pointer group ml-2">
                  <img
                    alt="User Avatar"
                    className="w-8 h-8 rounded-full border-2 border-transparent group-hover:border-primary transition-all object-cover"
                    src={user?.avatar || "https://ui-avatars.com/api/?name=You&background=12284C&color=fff"}
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = 'https://ui-avatars.com/api/?name=You&background=12284C&color=fff';
                    }}
                  />
                  <span className="material-symbols-outlined text-text-muted group-hover:text-primary transition-colors">arrow_drop_down</span>

                  {/* Dropdown Menu */}
                  <div className="absolute top-full right-0 mt-2 w-48 bg-surface rounded-lg shadow-lg border border-gray-100 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 flex flex-col py-2 cursor-default text-left">
                    <Link to="/dashboard" className="px-4 py-2 text-sm text-text-main hover:bg-surface-gray hover:text-primary transition-colors flex items-center gap-2">
                      <span className="material-symbols-outlined text-[18px]">dashboard</span> My Learning
                    </Link>
                    {user && user.role === 'admin' && (
                      <Link to="/admin" className="px-4 py-2 text-sm text-text-main hover:bg-surface-gray hover:text-primary transition-colors flex items-center gap-2">
                        <span className="material-symbols-outlined text-[18px]">admin_panel_settings</span> Admin Panel
                      </Link>
                    )}
                    {user && user.role === 'instructor' && (
                      <Link to="/instructor" className="px-4 py-2 text-sm text-text-main hover:bg-surface-gray hover:text-primary transition-colors flex items-center gap-2">
                        <span className="material-symbols-outlined text-[18px]">school</span> Instructor Panel
                      </Link>
                    )}
                    {user && user.role !== 'admin' && user.role !== 'instructor' && (
                      <Link to="/apply-instructor" className="px-4 py-2 text-sm text-text-main hover:bg-surface-gray hover:text-primary transition-colors flex items-center gap-2">
                        <span className="material-symbols-outlined text-[18px]">school</span> Become Instructor
                      </Link>
                    )}
                    <a href="#" className="px-4 py-2 text-sm text-text-main hover:bg-surface-gray hover:text-primary transition-colors flex items-center gap-2">
                      <span className="material-symbols-outlined text-[18px]">person</span> Edit Profile
                    </a>
                    <Link to="/wallet-transaction" className="px-4 py-2 text-sm text-text-main hover:bg-surface-gray hover:text-primary transition-colors flex items-center gap-2">
                      <span className="material-symbols-outlined text-[18px]">account_balance_wallet</span> Wallet
                    </Link>
                    <div className="h-px bg-gray-100 my-1 w-full"></div>
                    <button onClick={handleLogout} className="px-4 py-2 text-sm text-red-600 hover:bg-red-50 hover:text-red-700 transition-colors flex items-center gap-2 w-full text-left">
                      <span className="material-symbols-outlined text-[18px]">logout</span> Logout
                    </button>
                  </div>
                </div>
              ) : (
                <Link
                  to="/login"
                  className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-gradient-to-r from-primary to-[#ff8c42] hover:from-[#d95f19] hover:to-primary text-white font-extrabold text-xs md:text-sm shadow-sm transition-all transform active:scale-95 group select-none shrink-0"
                >
                  <span className="material-symbols-outlined text-[18px] group-hover:translate-x-0.5 transition-transform">login</span>
                  <span>Login</span>
                </Link>
              )}
            </div>
          </div>
        </header>
      )}

      {/* Main content body with Outlet */}
      <main className={`relative z-10 flex-grow w-full min-w-0 ${(isInstructorRoute || isAdminRoute) ? '' : 'pt-16'}`}>
        {isContestPage ? (
          <div className="flex-grow flex flex-col md:flex-row w-full max-w-[1920px] mx-auto text-left relative z-10">
            {/* Main content column on the left (85% default, 100% for ranking) */}
            <div className={activeTab === 'ranking' ? "w-full flex flex-col bg-surface-gray min-w-0" : "w-full md:w-[85%] flex flex-col bg-surface-gray min-w-0"}>
              <Outlet context={{ contest, loading, error, fetchContest, timeLeft, timerLabel }} />
            </div>

            {/* Shared right sidebar (15%) - Hidden on ranking tab */}
            {activeTab !== 'ranking' && (
              <ContestSidebar
                contestId={contestId || ''}
                activeTab={activeTab}
                timeLeft={timeLeft}
                timerLabel={timerLabel}
                isRegistered={!!contest?.isUserRegistered}
                contestStatus={contest?.status}
              >
              {activeTab === 'overview' && !loading && contest && (
                <div className="mt-8 border-t border-gray-100 pt-6">
                  {!user ? (
                    <div className="bg-blue-50 border border-blue-200 text-blue-800 rounded-xl p-4 text-center space-y-3">
                      <span className="material-symbols-outlined text-blue-600 text-3xl mb-1">account_circle</span>
                      <p className="text-sm font-bold">Authentication Required</p>
                      <p className="text-xs text-blue-600">Please login to register for this contest.</p>
                      <button
                        onClick={() => navigate('/login')}
                        className="w-full bg-primary hover:bg-primary-hover text-white text-xs font-bold py-2 rounded-lg transition-all"
                      >
                        Go to Login
                      </button>
                    </div>
                  ) : contest.isUserRegistered ? (
                    <div className="bg-green-50 border border-green-200 text-green-800 rounded-xl p-4 text-center">
                      <span className="material-symbols-outlined text-green-600 text-3xl mb-1 icon-fill">verified_user</span>
                      <p className="text-sm font-bold">Registered</p>
                      <p className="text-xs text-green-600 mt-1">You are in this arena!</p>
                    </div>
                  ) : contest.status === 'ENDED' ? (
                    <div className="bg-red-50 border border-red-200 text-red-800 rounded-xl p-4 text-center">
                      <span className="material-symbols-outlined text-red-600 text-3xl mb-1">lock</span>
                      <p className="text-sm font-bold">Registration Closed</p>
                      <p className="text-xs text-red-600 mt-1">This contest has already ended.</p>
                    </div>
                  ) : (
                    <form onSubmit={handleRegister} className="space-y-4">
                      {contest.isPrivate && (
                        <div>
                          <label className="block text-label-md font-medium text-text-muted mb-2 tracking-wider uppercase text-center" htmlFor="contest-password">
                            Contest Password
                          </label>
                          <input
                            className="w-full bg-surface border border-gray-300 rounded-lg px-4 py-2 text-body-md focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-colors text-center"
                            id="contest-password"
                            placeholder="Enter password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                          />
                        </div>
                      )}
                      <button
                        disabled={registering}
                        className="w-full bg-primary text-white text-label-md font-label-md rounded-xl font-bold hover:bg-primary-hover transition-all duration-200 shadow-sm py-2.5 disabled:opacity-50"
                        type="submit"
                      >
                        {registering ? 'Registering...' : 'Register Now'}
                      </button>
                      {registrationMessage && (
                        <div
                          className={`text-xs font-bold p-2.5 rounded-lg text-center ${registrationMessage.type === 'success'
                              ? 'bg-green-50 text-green-700 border border-green-200'
                              : 'bg-red-50 text-red-700 border border-red-200'
                            }`}
                        >
                          {registrationMessage.text}
                        </div>
                      )}
                    </form>
                  )}
                </div>
              )}
              </ContestSidebar>
            )}
          </div>
        ) : (
          <Outlet />
        )}
      </main>

      {/* Master Footer */}
      {!isInstructorRoute && !isAdminRoute && !isProblemSolvePage && !isContestPage && (
        <footer className="bg-brand-blue text-white mt-auto shrink-0 w-full z-40 relative">
          <div className="max-w-[1440px] mx-auto px-8 py-12">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              <div className="flex flex-col gap-4">
                <div className="text-headline-md font-bold font-display">
                  <span>Nonstop</span><span className="text-primary">Coding</span>
                </div>
                <p className="text-body-md text-white/70 max-w-xs">
                  Empowering developers through continuous learning, practice, and competition in a global tech community.
                </p>
                <div className="flex gap-4">
                  <button className="w-10 h-10 rounded-full bg-white/10 hover:bg-primary transition-colors flex items-center justify-center">
                    <span className="material-symbols-outlined">language</span>
                  </button>
                  <button className="w-10 h-10 rounded-full bg-white/10 hover:bg-primary transition-colors flex items-center justify-center">
                    <span className="material-symbols-outlined">share</span>
                  </button>
                </div>
              </div>
              <div className="flex flex-col gap-4">
                <h4 className="text-body-lg font-bold font-display">Platform</h4>
                <nav className="flex flex-col gap-2">
                  <Link className="text-white/70 hover:text-primary transition-colors" to="/courses">Courses</Link>
                  <Link className="text-white/70 hover:text-primary transition-colors" to="/problems">Problems</Link>
                  <Link className="text-white/70 hover:text-primary transition-colors" to="/contests">Contests</Link>
                  <Link className="text-white/70 hover:text-primary transition-colors" to="/rankings">Leaderboard</Link>
                </nav>
              </div>
              <div className="flex flex-col gap-4">
                <h4 className="text-body-lg font-bold font-display">Support</h4>
                <nav className="flex flex-col gap-2">
                  <a className="text-white/70 hover:text-primary transition-colors" href="#">Help Center</a>
                  <a className="text-white/70 hover:text-primary transition-colors" href="#">FAQ</a>
                  <Link className="text-white/70 hover:text-primary transition-colors" to="/contact">Contact Us</Link>
                  <Link className="text-white/70 hover:text-primary transition-colors" to="/terms">Terms of Service</Link>
                </nav>
              </div>
              <div className="flex flex-col gap-4">
                <h4 className="text-body-lg font-bold font-display">Stay Updated</h4>
                <p className="text-body-md text-white/70">Subscribe to our newsletter for latest updates.</p>
                <div className="flex flex-col gap-2">
                  <input className="bg-white/10 border-white/20 rounded-lg py-2 px-4 text-white placeholder-white/40 focus:ring-primary focus:border-primary" placeholder="Enter your email" type="email" />
                  <button className="w-full bg-primary hover:bg-primary-hover py-2 rounded-lg font-bold transition-colors">Subscribe</button>
                </div>
              </div>
            </div>
            <div className="mt-12 pt-8 border-t border-white/10 flex flex-col md:flex-row justify-between items-center gap-4">
              <p className="text-sm text-white/60">© 2024 Nonstop Coding. All rights reserved.</p>
              <div className="flex gap-6">
                <Link className="text-sm text-white/60 hover:text-primary" to="/privacy">Privacy Policy</Link>
                <Link className="text-sm text-white/60 hover:text-primary" to="/cookies">Cookies Policy</Link>
              </div>
            </div>
          </div>
        </footer>
      )}
    </div>
  );
};
