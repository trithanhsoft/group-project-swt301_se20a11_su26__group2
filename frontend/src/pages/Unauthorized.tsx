import React from 'react';
import { Link } from 'react-router-dom';

export const Unauthorized: React.FC = () => {
  return (
    <div
      className="min-h-[70vh] flex items-center justify-center p-6 text-text-main relative overflow-hidden"
      style={{ fontFamily: "'Inter', sans-serif" }}
    >
      {/* Subtle background glow */}
      <div className="absolute inset-0 pointer-events-none z-0 overflow-hidden">
        <div className="absolute top-1/4 left-1/4 w-[400px] h-[400px] bg-red-500/5 rounded-full blur-[100px]"></div>
      </div>

      <div className="bg-surface/90 backdrop-blur-md rounded-2xl border border-red-150/40 p-8 md:p-12 text-center shadow-lg max-w-md w-full relative z-10 hover:shadow-xl transition-all duration-300">
        {/* Animated Lock Icon */}
        <div className="w-20 h-20 bg-red-50 text-red-500 rounded-full flex items-center justify-center mx-auto mb-6 border border-red-100 shadow-inner transition-transform duration-500 hover:rotate-12">
          <span className="material-symbols-outlined text-4xl">lock</span>
        </div>
        
        <h3 className="font-display font-black text-2xl text-brand-blue mb-3">Access Denied</h3>
        <p className="font-body text-sm text-text-muted mb-8 leading-relaxed">
          Access denied. Your current account does not have sufficient permissions to access this area.
        </p>

        <div className="flex flex-col gap-3">
          <Link
            to="/"
            className="flex items-center justify-center gap-2 w-full py-3 px-4 rounded-xl text-sm font-bold text-white bg-primary hover:bg-primary-hover shadow-md hover:shadow transition-all active:scale-95"
          >
            <span className="material-symbols-outlined text-lg">home</span>
            <span>Quay lại Trang chủ</span>
          </Link>
          <Link
            to="/login"
            className="flex items-center justify-center gap-2 w-full py-3 px-4 rounded-xl text-sm font-bold text-primary hover:bg-primary-light/20 border border-primary/20 transition-all active:scale-95"
          >
            <span className="material-symbols-outlined text-lg">logout</span>
            <span>Đăng nhập tài khoản khác</span>
          </Link>
        </div>
      </div>
    </div>
  );
};
