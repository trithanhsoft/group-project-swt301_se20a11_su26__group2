import React from 'react';

export const PrivacyPolicy: React.FC = () => {
  return (
    <div className="max-w-[1000px] mx-auto px-margin-mobile md:px-margin-desktop py-12 md:py-20 text-left">
      <div className="mb-12 border-b border-slate-200 pb-8">
        <div className="inline-flex items-center gap-1.5 bg-brand-green-light border border-brand-green/20 px-3 py-1 rounded-full text-brand-green font-bold text-xs uppercase tracking-wider mb-4 shadow-sm">
          <span className="material-symbols-outlined text-xs">shield</span> Trust &amp; Safety
        </div>
        <h1 className="text-display-md font-display font-black text-brand-blue leading-tight mb-2">
          Privacy Policy
        </h1>
        <p className="text-sm text-text-muted">Last Updated: June 3, 2026</p>
      </div>

      <div className="flex flex-col gap-8 text-text-main leading-relaxed">
        {/* Section 1 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-brand-green font-black">1.</span> Introduction
          </h2>
          <p className="text-sm text-text-muted">
            At Nonstop Coding, we value your privacy and trust. This Privacy Policy details what information we collect when you use our educational website, coding sandbox compiler, and dashboard, and how we protect and process that information.
          </p>
        </section>

        {/* Section 2 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-brand-green font-black">2.</span> Information We Collect
          </h2>
          <p className="text-sm text-text-muted mb-3">
            To provide a fully integrated learning environment, we collect the following types of information:
          </p>
          <ul className="list-disc list-inside text-sm text-text-muted flex flex-col gap-2 pl-2">
            <li><strong>Account Profile Data:</strong> When registering, you provide your name, email address, password, role choice, and optional profile avatar. If you log in via Google OAuth, we receive authorized profile attributes (avatar, name, email) from Google.</li>
            <li><strong>Code Submissions:</strong> We store source code submissions that you run or submit for test validations. This allows us to track challenge scores, update leaderboards, and grade course exercises.</li>
            <li><strong>Transaction Logs:</strong> For premium course purchases, we record payment transaction data, reference IDs, and wallet deposit logs (credit/debit balances). We do not store full credit card credentials locally.</li>
          </ul>
        </section>

        {/* Section 3 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-brand-green font-black">3.</span> How We Use Your Information
          </h2>
          <p className="text-sm text-text-muted mb-3">
            Your personal information is used exclusively to:
          </p>
          <ul className="list-disc list-inside text-sm text-text-muted flex flex-col gap-2 pl-2">
            <li>Provide access to online courses, lesson videos, and sandboxes.</li>
            <li>Validate source code compliance and evaluate program logic securely in real-time.</li>
            <li>Build global leaderboards and contest rank standings.</li>
            <li>Send critical transactional notifications (such as course payment receipts or security alerts).</li>
          </ul>
        </section>

        {/* Section 4 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-brand-green font-black">4.</span> Code Evaluation Safety &amp; Cache
          </h2>
          <p className="text-sm text-text-muted">
            Code files submitted to our compilers run inside stateless sandbox containers. While test inputs are processed in-memory, successful and failed solutions are logged to your personal profile dashboard to help you track learning milestones. We implement structural encryption and network firewalls to ensure that your coding solutions are secure.
          </p>
        </section>

        {/* Section 5 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-brand-green font-black">5.</span> Data Sharing &amp; Third Parties
          </h2>
          <p className="text-sm text-text-muted">
            We do not sell, rent, or trade your personal profile data. We share relevant transaction information only with verified payment processors and secure cloud infrastructure hosting providers (such as Google Cloud Platform/AWS) solely to run the platform.
          </p>
        </section>

        {/* Section 6 */}
        <section className="glassmorphism rounded-2xl p-6 md:p-8 border border-slate-200/50 shadow-sm">
          <h2 className="font-display font-bold text-lg text-brand-blue mb-3 flex items-center gap-2">
            <span className="text-brand-green font-black">6.</span> Your Rights &amp; Access Controls
          </h2>
          <p className="text-sm text-text-muted">
            You have the right to view, modify, or completely delete your personal profile data at any time. You can edit your profile details from the Student Dashboard. To request permanent account deletion and removal of all source code submissions from our database, please email <a href="mailto:privacy@nonstopcoding.vn" className="text-primary hover:underline font-semibold">privacy@nonstopcoding.vn</a>.
          </p>
        </section>
      </div>
    </div>
  );
};
