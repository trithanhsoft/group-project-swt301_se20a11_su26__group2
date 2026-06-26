import React from 'react';

export const CookiesPolicy: React.FC = () => {
  return (
    <div className="max-w-[1000px] mx-auto px-margin-mobile md:px-margin-desktop py-12 md:py-20 text-left">
      <div className="mb-12 border-b border-slate-200 pb-8">
        <div className="inline-flex items-center gap-1.5 bg-[#fce2d3] border border-primary/20 px-3 py-1 rounded-full text-primary font-bold text-xs uppercase tracking-wider mb-4 shadow-sm">
          <span className="material-symbols-outlined text-xs">cookie</span> User Experience
        </div>
        <h1 className="text-display-md font-display font-black text-brand-blue leading-tight mb-2">
          Cookies Policy
        </h1>
        <p className="text-sm text-text-muted">Last Updated: June 3, 2026</p>
      </div>

      <div className="flex flex-col gap-8 text-text-main leading-relaxed">
        {/* Section 1 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">1.</span> What Are Cookies?
          </h2>
          <p className="text-sm text-text-muted">
            Cookies are small text files stored on your web browser or device when you visit websites. They are widely used to make web applications function correctly, optimize user experiences, and provide administrative analytics.
          </p>
        </section>

        {/* Section 2 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">2.</span> How We Use Cookies
          </h2>
          <p className="text-sm text-text-muted mb-3">
            Nonstop Coding uses cookies and local storage tokens to recognize you when you navigate our platform. Specifically:
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-4">
            {/* Type 1 */}
            <div className="p-4 bg-surface rounded-xl border border-slate-100 shadow-sm">
              <span className="material-symbols-outlined text-primary mb-2 text-2xl">vpn_key</span>
              <h4 className="font-display font-bold text-sm text-brand-blue mb-1">Essential Authentication</h4>
              <p className="text-xs text-text-muted">Used to verify your logged-in status, keeping you authenticated as you browse from courses to sandboxes.</p>
            </div>
            {/* Type 2 */}
            <div className="p-4 bg-surface rounded-xl border border-slate-100 shadow-sm">
              <span className="material-symbols-outlined text-brand-green mb-2 text-2xl">settings</span>
              <h4 className="font-display font-bold text-sm text-brand-blue mb-1">Functional Preferences</h4>
              <p className="text-xs text-text-muted">Remembers your editor preferences, code sizing, and language selection (C++, Python, JS) in the sandbox.</p>
            </div>
            {/* Type 3 */}
            <div className="p-4 bg-surface rounded-xl border border-slate-100 shadow-sm">
              <span className="material-symbols-outlined text-blue-500 mb-2 text-2xl">analytics</span>
              <h4 className="font-display font-bold text-sm text-brand-blue mb-1">Traffic Analytics</h4>
              <p className="text-xs text-text-muted">Aggregates anonymous visitor data to analyze layout efficiency, average lesson duration, and site stability.</p>
            </div>
          </div>
        </section>

        {/* Section 3 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">3.</span> Local Browser Storage
          </h2>
          <p className="text-sm text-text-muted">
            In addition to cookies, we utilize HTML5 Local Storage to temporarily cache incomplete code solutions in your sandbox. This ensures that you do not lose progress if your internet connection gets disconnected unexpectedly.
          </p>
        </section>

        {/* Section 4 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">4.</span> Controlling Your Cookie Settings
          </h2>
          <p className="text-sm text-text-muted">
            Most web browsers automatically accept cookies, but you can configure your browser settings to reject or delete them. Please note that blocking essential authentication cookies will prevent you from logging in, checking out of your shopping cart, or using the online compiler sandbox.
          </p>
        </section>
      </div>
    </div>
  );
};

