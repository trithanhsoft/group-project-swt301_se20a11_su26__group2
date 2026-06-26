import React, { useState } from 'react';

export const ContactUs: React.FC = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: 'general',
    message: '',
  });
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    // Simulate API request
    setTimeout(() => {
      setLoading(false);
      setIsSubmitted(true);
      setFormData({
        name: '',
        email: '',
        subject: 'general',
        message: '',
      });
      // Hide success notification after 5s
      setTimeout(() => setIsSubmitted(false), 5000);
    }, 1200);
  };

  return (
    <div className="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop py-12 md:py-20">
      {/* Page Header */}
      <div className="text-center mb-12 md:mb-16">
        <div className="inline-flex items-center gap-1.5 bg-[#fce2d3] border border-primary/20 px-3 py-1 rounded-full text-primary font-bold text-xs uppercase tracking-wider mb-4 shadow-sm">
          <span className="material-symbols-outlined text-xs">support_agent</span> Get in Touch
        </div>
        <h1 className="text-display-md font-display font-black text-brand-blue leading-tight mb-4">
          Contact <span className="bg-gradient-to-r from-primary to-[#ff8c42] bg-clip-text text-transparent">Our Support</span> Team
        </h1>
        <p className="text-body-lg text-text-muted max-w-xl mx-auto">
          Have a question about our programming courses, platform features, or licensing? Reach out to us. We usually reply within 24 hours.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-start">
        {/* Contact Info Cards */}
        <div className="lg:col-span-5 flex flex-col gap-6">
          {/* Card 1: Email Support */}
          <div className="glassmorphism rounded-2xl p-6 border border-slate-200/60 shadow-lg relative group transition-all duration-300 hover:shadow-xl">
            <div className="flex gap-4">
              <div className="w-12 h-12 rounded-xl bg-primary-light flex items-center justify-center text-primary shrink-0 border border-primary/10">
                <span className="material-symbols-outlined text-2xl">mail</span>
              </div>
              <div className="flex flex-col">
                <h3 className="font-display font-bold text-base text-brand-blue">Email Support</h3>
                <p className="text-xs text-text-muted mt-1 mb-2">For general inquiries, account questions, and payment support.</p>
                <a href="mailto:support@nonstopcoding.vn" className="text-sm font-bold text-primary hover:underline">
                  support@nonstopcoding.vn
                </a>
              </div>
            </div>
          </div>

          {/* Card 2: Tech Hotline */}
          <div className="glassmorphism rounded-2xl p-6 border border-slate-200/60 shadow-lg relative group transition-all duration-300 hover:shadow-xl">
            <div className="flex gap-4">
              <div className="w-12 h-12 rounded-xl bg-brand-green-light flex items-center justify-center text-brand-green shrink-0 border border-brand-green/10">
                <span className="material-symbols-outlined text-2xl">phone_in_talk</span>
              </div>
              <div className="flex flex-col">
                <h3 className="font-display font-bold text-base text-brand-blue">Phone Hotline</h3>
                <p className="text-xs text-text-muted mt-1 mb-2">Call us directly for urgent system accessibility issues.</p>
                <a href="tel:+84123456789" className="text-sm font-bold text-brand-green hover:underline">
                  +84 (123) 456-789
                </a>
              </div>
            </div>
          </div>

          {/* Card 3: Main Office */}
          <div className="glassmorphism rounded-2xl p-6 border border-slate-200/60 shadow-lg relative group transition-all duration-300 hover:shadow-xl">
            <div className="flex gap-4">
              <div className="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center text-blue-600 shrink-0 border border-blue-200/30">
                <span className="material-symbols-outlined text-2xl">location_on</span>
              </div>
              <div className="flex flex-col">
                <h3 className="font-display font-bold text-base text-brand-blue">Campus Office</h3>
                <p className="text-xs text-text-muted mt-1 mb-2">FPT University Da Nang Campus,</p>
                <p className="text-sm font-semibold text-text-main">
                  Khu đô thị FPT City, Ngũ Hành Sơn, Đà Nẵng, Vietnam
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Contact Form */}
        <div className="lg:col-span-7">
          <div className="glassmorphism rounded-3xl p-6 md:p-8 shadow-xl border border-white/50 relative overflow-hidden">
            <div className="absolute -top-40 -right-40 w-80 h-80 bg-primary/5 rounded-full blur-[100px] pointer-events-none"></div>
            
            <h2 className="font-display font-bold text-xl text-brand-blue mb-6">Send Us a Message</h2>

            {isSubmitted && (
              <div className="mb-6 p-4 bg-brand-green-light border border-brand-green/20 rounded-xl text-brand-green flex items-center gap-3 animate-fade-in">
                <span className="material-symbols-outlined icon-fill">check_circle</span>
                <div className="text-xs font-semibold">
                  Thank you! Your message has been sent successfully. We will contact you soon.
                </div>
              </div>
            )}

            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5 text-left">
                  <label htmlFor="name" className="text-xs font-bold text-text-main uppercase tracking-wider">Your Name</label>
                  <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                    placeholder="Enter your name"
                    className="bg-white border border-slate-200 rounded-xl py-3 px-4 text-sm text-text-main placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                  />
                </div>
                <div className="flex flex-col gap-1.5 text-left">
                  <label htmlFor="email" className="text-xs font-bold text-text-main uppercase tracking-wider">Email Address</label>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    placeholder="name@example.com"
                    className="bg-white border border-slate-200 rounded-xl py-3 px-4 text-sm text-text-main placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                  />
                </div>
              </div>

              <div className="flex flex-col gap-1.5 text-left">
                <label htmlFor="subject" className="text-xs font-bold text-text-main uppercase tracking-wider">Subject</label>
                <select
                  id="subject"
                  name="subject"
                  value={formData.subject}
                  onChange={handleChange}
                  className="bg-white border border-slate-200 rounded-xl py-3 px-4 text-sm text-text-main focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all cursor-pointer"
                >
                  <option value="general">General Inquiry</option>
                  <option value="billing">Course Billing &amp; Refunds</option>
                  <option value="technical">Technical Bug or Compiler Error</option>
                  <option value="partnership">Business Partnership</option>
                </select>
              </div>

              <div className="flex flex-col gap-1.5 text-left">
                <label htmlFor="message" className="text-xs font-bold text-text-main uppercase tracking-wider">Message</label>
                <textarea
                  id="message"
                  name="message"
                  value={formData.message}
                  onChange={handleChange}
                  required
                  rows={5}
                  placeholder="How can we help you? Please describe in detail..."
                  className="bg-white border border-slate-200 rounded-xl py-3 px-4 text-sm text-text-main placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all resize-none"
                ></textarea>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full mt-2 inline-flex items-center justify-center gap-2 bg-gradient-to-r from-primary to-[#ff8c42] hover:from-[#d95f19] hover:to-primary text-white font-bold text-sm uppercase tracking-wide py-3.5 rounded-xl shadow-md hover:shadow-lg transition-all duration-300 transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed group"
              >
                {loading ? (
                  <>
                    Sending message...
                    <span className="material-symbols-outlined text-[18px] animate-spin">sync</span>
                  </>
                ) : (
                  <>
                    Submit Message
                    <span className="material-symbols-outlined text-[18px] group-hover:translate-x-0.5 transition-transform">send</span>
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};
