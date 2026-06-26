import React from 'react';

export const TermsOfService: React.FC = () => {
  return (
    <div className="max-w-[1000px] mx-auto px-margin-mobile md:px-margin-desktop py-12 md:py-20 text-left">
      <div className="mb-12 border-b border-slate-200 pb-8">
        <div className="inline-flex items-center gap-1.5 bg-blue-100 border border-blue-200 px-3 py-1 rounded-full text-blue-700 font-bold text-xs uppercase tracking-wider mb-4 shadow-sm">
          <span className="material-symbols-outlined text-xs">gavel</span> Legal Terms
        </div>
        <h1 className="text-display-md font-display font-black text-brand-blue leading-tight mb-2">
          Terms of Service
        </h1>
        <p className="text-sm text-text-muted">Last Updated: June 3, 2026</p>
      </div>

      <div className="flex flex-col gap-8 text-text-main leading-relaxed">
        {/* Section 1 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">1.</span> Acceptance of Terms
          </h2>
          <p className="text-sm text-text-muted">
            Welcome to Nonstop Coding. By accessing or using our website, courses, online sandbox compilers, and coding contest environments (collectively, the "Service"), you agree to comply with and be bound by these Terms of Service. If you do not agree to these terms, please do not use the Service.
          </p>
        </section>

        {/* Section 2 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">2.</span> Account Registration &amp; Security
          </h2>
          <p className="text-sm text-text-muted mb-3">
            To unlock our interactive coding sandboxes, participate in global leaderboards, and enroll in course modules, you must create a user account. You agree to:
          </p>
          <ul className="list-disc list-inside text-sm text-text-muted flex flex-col gap-2 pl-2">
            <li>Provide accurate, current, and complete credentials during registration.</li>
            <li>Maintain the security of your password and accept all risks of unauthorized access to your account.</li>
            <li>Promptly notify our support team if you discover any security breaches related to the Service.</li>
          </ul>
        </section>

        {/* Section 3 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">3.</span> Course Enrollment &amp; Payments
          </h2>
          <p className="text-sm text-text-muted mb-3">
            Our platform offers both free algorithmic practice suites and premium, structured technical programs.
          </p>
          <ul className="list-disc list-inside text-sm text-text-muted flex flex-col gap-2 pl-2">
            <li><strong>Tuition Fees:</strong> Enrollment prices for premium courses are billed in Vietnamese Dong (₫). Fees are clearly specified on the course information pages.</li>
            <li><strong>Refund Policy:</strong> Since all curriculum content, coding sandbox challenges, and certificates are available immediately upon purchase, refunds are only issued under special billing anomalies, within 3 days of transaction, and before completing more than 10% of the lessons.</li>
          </ul>
        </section>

        {/* Section 4 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">4.</span> Sandbox Compiler &amp; Fair Play Rules
          </h2>
          <p className="text-sm text-text-muted mb-3">
            Our online compilers compile, test, and run code (Python, C++, JS) inside safe sandboxes. Misusing these tools disrupts the ecosystem for all learners.
          </p>
          <ul className="list-disc list-inside text-sm text-text-muted flex flex-col gap-2 pl-2">
            <li><strong>No System Abuse:</strong> Do not attempt to bypass sandbox limitations, execute malicious commands, write infinite loops that consume resources, or initiate DDoS attacks.</li>
            <li><strong>Academic Integrity:</strong> Plagiarism or copying external code during ranked contests or certified module completions is strictly forbidden. We actively monitor submission logs; violations will result in score resets and permanent leaderboard bans.</li>
          </ul>
        </section>

        {/* Section 5 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">5.</span> Intellectual Property Rights
          </h2>
          <p className="text-sm text-text-muted">
            All course videos, curriculum graphics, coding challenge questions, templates, online compiler architecture, and logo graphics are the proprietary intellectual property of Nonstop Coding. You are granted a limited, personal, non-transferable license to access and view our resources strictly for private educational purposes.
          </p>
        </section>

        {/* Section 6 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-primary font-black">6.</span> Termination of Account
          </h2>
          <p className="text-sm text-text-muted">
            We reserve the right to suspend or terminate your user access immediately, without prior notice or liability, if you violate these Terms of Service, commit code fraud during contests, or engage in behavior that harms other community members.
          </p>
        </section>
      </div>
    </div>
  );
};
