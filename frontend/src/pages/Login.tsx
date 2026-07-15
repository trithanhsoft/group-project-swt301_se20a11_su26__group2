import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import { useApp } from '../context/AppContext';

export const Login: React.FC = () => {
  const { login, googleLogin } = useApp();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const user = await login(username, password);
      if (user.role === 'admin') {
        navigate('/admin');
      } else if (user.role === 'instructor') {
        navigate('/instructor');
      } else {
        navigate('/dashboard');
      }
    } catch (err: any) {
      setError(err.message || 'Login failed. Please check your details.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="min-h-screen flex items-center justify-center p-gutter md:p-margin-desktop text-text-main bg-[#f0f4f9] relative overflow-hidden"
      style={{ fontFamily: "'Inter', sans-serif" }}
    >
      {/* Back to Home Button */}
      <Link 
        to="/" 
        className="absolute top-6 left-6 md:top-8 md:left-8 flex items-center gap-2 text-text-muted hover:text-primary transition-colors font-semibold z-20 bg-white/50 backdrop-blur-sm px-4 py-2 rounded-full shadow-sm hover:shadow"
      >
        <span className="material-symbols-outlined text-[20px]">arrow_back</span>
        Back to Home
      </Link>

      {/* Glowing Backdrop Circles matching Layout.tsx */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        <div className="absolute -top-40 -left-40 w-[500px] h-[500px] bg-primary/10 rounded-full blur-[120px]"></div>
        <div className="absolute top-1/3 -right-40 w-[500px] h-[500px] bg-brand-blue/10 rounded-full blur-[120px]"></div>
        <div className="absolute -bottom-40 left-1/4 w-[600px] h-[600px] bg-brand-green/5 rounded-full blur-[150px]"></div>
      </div>

      <main className="w-full max-w-md relative z-10">
        {/* Logo Header */}
        <div className="text-center mb-10 flex flex-col items-center">
          <Link to="/">
            <img src={`${import.meta.env.BASE_URL}LOGO.png`} alt="Nonstop Coding Logo" className="h-20 w-auto mb-2 drop-shadow-sm" />
          </Link>
          <p className="font-body text-text-muted mt-2 font-medium">Welcome back! Please enter your details.</p>
        </div>

        <div className="bg-surface rounded-2xl shadow-sm border border-gray-100 p-8 md:p-10 transition-all duration-300 hover:shadow-md">
          {error && (
            <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-lg mb-6 text-red-700 text-sm font-medium">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Username Input */}
            <div>
              <label className="block font-medium text-text-main text-sm mb-1.5" htmlFor="username">Username</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                  <span className="material-symbols-outlined text-text-muted text-lg">person</span>
                </div>
                <input
                  className="block w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl bg-white text-text-main placeholder-gray-400 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                  id="username"
                  name="username"
                  placeholder="Enter your username"
                  required
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>
            </div>

            {/* Password Input */}
            <div>
              <label className="block font-medium text-text-main text-sm mb-1.5" htmlFor="password">Password</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                  <span className="material-symbols-outlined text-text-muted text-lg">lock</span>
                </div>
                <input
                  className="block w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl bg-white text-text-main placeholder-gray-400 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all shadow-sm"
                  id="password"
                  name="password"
                  placeholder="Enter your password"
                  required
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
            </div>

            {/* Remember & Forgot */}
            <div className="flex items-center justify-between pt-1">
              <div className="flex items-center">
                <input
                  className="h-4 w-4 text-primary focus:ring-primary border-gray-300 rounded cursor-pointer"
                  id="remember-me"
                  name="remember-me"
                  type="checkbox"
                />
                <label className="ml-2 block text-sm text-text-muted cursor-pointer select-none" htmlFor="remember-me">
                  Remember me
                </label>
              </div>
              <div className="text-sm">
                <a className="font-medium text-primary hover:text-primary-hover transition-colors" href="#">
                  Forgot password?
                </a>
              </div>
            </div>

            {/* Submit Button */}
            <button
              className="w-full flex justify-center py-2.5 px-4 border border-transparent rounded-xl shadow-sm text-sm font-bold text-white bg-primary hover:bg-primary-hover transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary items-center disabled:opacity-70 disabled:cursor-not-allowed mt-2"
              type="submit"
              disabled={loading}
            >
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="material-symbols-outlined animate-spin text-lg">autorenew</span>
                  Logging In...
                </span>
              ) : 'Log In'}
            </button>
          </form>

          <div className="mt-8">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-3 bg-surface text-text-muted text-xs uppercase tracking-wider font-semibold">Or continue with</span>
              </div>
            </div>

            <div className="mt-6 flex flex-col gap-3 items-center w-full">
              <div className="w-full flex justify-center [&>div]:w-full drop-shadow-sm hover:drop-shadow transition-all">
                <GoogleLogin
                  onSuccess={async (credentialResponse) => {
                    if (credentialResponse.credential) {
                      setLoading(true);
                      setError(null);
                      try {
                        const user = await googleLogin(credentialResponse.credential);
                        if (user.role === 'admin') {
                          navigate('/admin');
                        } else if (user.role === 'instructor') {
                          navigate('/instructor');
                        } else {
                          navigate('/dashboard');
                        }
                      } catch (err: any) {
                        setError(err.message || 'Đăng nhập Google thất bại');
                      } finally {
                        setLoading(false);
                      }
                    }
                  }}
                  onError={() => {
                    setError('Đăng nhập Google thất bại');
                  }}
                  useOneTap
                  theme="outline"
                  size="large"
                  text="continue_with"
                  width="100%"
                  shape="rectangular"
                />
              </div>
            </div>
          </div>
        </div>

        {/* Register Link */}
        <p className="mt-8 text-center text-sm text-text-muted">
          Don't have an account?{' '}
          <Link className="font-bold text-primary hover:text-primary-hover transition-colors underline decoration-2 underline-offset-4 decoration-primary/30 hover:decoration-primary" to="/register">Register now</Link>
        </p>
      </main>
    </div>
  );
};
