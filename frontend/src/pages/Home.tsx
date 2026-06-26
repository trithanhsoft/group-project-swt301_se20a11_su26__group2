import React, { useState, useRef } from 'react';
import { Link } from 'react-router-dom';

const codeSnippets: Record<string, string> = {
  cpp: `vector&lt;<span class="text-pink-400">int</span>&gt; <span class="text-blue-400">twoSum</span>(vector&lt;<span class="text-pink-400">int</span>&gt;&amp; nums, <span class="text-pink-400">int</span> target) {
    <span class="text-slate-500">// O(N) Hash Table approach</span>
    unordered_map&lt;<span class="text-pink-400">int</span>, <span class="text-pink-400">int</span>&gt; seen;
    <span class="text-pink-400">for</span> (<span class="text-pink-400">int</span> i = <span class="text-purple-400">0</span>; i &lt; nums.size(); i++) {
        <span class="text-pink-400">int</span> diff = target - nums[i];
        <span class="text-pink-400">if</span> (seen.count(diff)) {
            <span class="text-pink-400">return</span> {seen[diff], i};
        }
        seen[nums[i]] = i;
    }
    <span class="text-pink-400">return</span> {};
}`,
  py: `<span class="text-pink-400">def</span> <span class="text-blue-400">two_sum</span>(nums, target):
    <span class="text-slate-500"># O(N) linear scan using dictionary</span>
    seen = {}
    <span class="text-pink-400">for</span> i, num <span class="text-pink-400">in</span> <span class="text-yellow-400">enumerate</span>(nums):
        diff = target - num
        <span class="text-pink-400">if</span> diff <span class="text-pink-400">in</span> seen:
            <span class="text-pink-400">return</span> [seen[diff], i]
        seen[num] = i
    <span class="text-pink-400">return</span> []`,
  js: `<span class="text-pink-400">function</span> <span class="text-blue-400">twoSum</span>(nums, target) {
    <span class="text-slate-500">// Time Complexity: O(N)</span>
    <span class="text-pink-400">const</span> map = <span class="text-pink-400">new</span> <span class="text-yellow-400">Map</span>();
    <span class="text-pink-400">for</span> (<span class="text-pink-400">let</span> i = <span class="text-purple-400">0</span>; i &lt; nums.length; i++) {
        <span class="text-pink-400">const</span> diff = target - nums[i];
        <span class="text-pink-400">if</span> (map.<span class="text-yellow-400">has</span>(diff)) {
            <span class="text-pink-400">return</span> [map.<span class="text-yellow-400">get</span>(diff), i];
        }
        map.<span class="text-yellow-400">set</span>(nums[i], i);
    }
}`,
};

const tabFilenames: Record<string, string> = {
  cpp: 'two_sum.cpp',
  py: 'two_sum.py',
  js: 'two_sum.js',
};

export const Home: React.FC = () => {
  const [activeLang, setActiveLang] = useState<string>('js');
  const [terminalHtml, setTerminalHtml] = useState<string>(
    '<span class="text-slate-500">&gt; Click "Run Code" to compile and execute this challenge...</span>'
  );
  const [isRunning, setIsRunning] = useState(false);
  const terminalRef = useRef<HTMLDivElement>(null);

  const selectLanguage = (lang: string) => {
    setActiveLang(lang);
    setTerminalHtml(
      `<span class="text-slate-500">&gt; Sandbox loaded. Language switched to ${lang.toUpperCase()}. Click "Run Code" to compile...</span>`
    );
  };

  const runSandboxCode = () => {
    if (isRunning) return;
    setIsRunning(true);

    setTerminalHtml(
      '<span class="text-brand-green">&gt; initializing virtual compiler...</span><br><span class="text-slate-500">&gt; loading dataset test cases...</span>'
    );

    setTimeout(() => {
      setTerminalHtml(
        (prev) =>
          prev + '<br><span class="text-slate-300">&gt; compiling code structure... OK.</span>'
      );
    }, 600);

    setTimeout(() => {
      setTerminalHtml(
        (prev) => prev + '<br><span class="text-slate-300">&gt; executing tests...</span>'
      );
    }, 1200);

    setTimeout(() => {
      setTerminalHtml(
        (prev) =>
          prev +
          '<br><span class="text-brand-green font-bold">✔ Test Case 1 Passed!</span> (Input: nums = [2,7,11,15], target = 9)'
      );
    }, 1700);

    setTimeout(() => {
      setTerminalHtml(
        (prev) =>
          prev +
          '<br><span class="text-brand-green font-bold">✔ Test Case 2 Passed!</span> (Input: nums = [3,2,4], target = 6)'
      );
    }, 2100);

    setTimeout(() => {
      setTerminalHtml(
        (prev) =>
          prev +
          '<br><br><span class="text-amber-400 font-extrabold flex items-center gap-1">🏆 Challenge Solved successfully!</span>' +
          '<span class="text-brand-green font-black">All test cases passed. LC Point rewards: +50 LC. Rank Up progress updated!</span>'
      );
      setIsRunning(false);
    }, 2600);
  };

  return (
    <>
      {/* Section 1: Hero Folder & Mock IDE Preview */}
      <section className="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop py-12 md:py-20 grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
        {/* Hero Intro */}
        <div className="lg:col-span-6 flex flex-col items-start text-left">
          {/* Brand Logo in Hero */}
          <img src={`${import.meta.env.BASE_URL}LOGO.png`} alt="Nonstop Coding Brand Logo" className="h-[110px] w-auto mb-4 animate-float-medium" />

          {/* Promotion Tag */}
          <div className="inline-flex items-center gap-1.5 bg-brand-green-light border border-brand-green/20 px-3 py-1 rounded-full text-brand-green font-bold text-xs uppercase tracking-wider mb-4 shadow-sm">
            <span className="material-symbols-outlined text-xs icon-fill">sports_esports</span> Gamified Learning Arena
          </div>

          <h1 className="text-display-lg-mobile md:text-display-lg font-display font-black leading-tight mb-4">
            <span className="bg-gradient-to-r from-[#0114a7] to-[#2563eb] bg-clip-text text-transparent">Level Up</span> <br />
            <span className="bg-gradient-to-r from-[#ff6000] to-[#ff8c42] bg-clip-text text-transparent">Your Coding Skills</span>
          </h1>

          <p className="text-body-lg text-text-muted mb-8 max-w-lg leading-relaxed">
            Learn tech courses, solve 1200+ algorithm challenges, and participate in real-time developer contests. The ultimate all-in-one gamified coding suite.
          </p>

          {/* CTAs */}
          <div className="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
            <Link to="/courses" className="inline-flex items-center justify-center gap-2 bg-gradient-to-r from-[#ff6000] to-[#ff8c42] hover:from-[#d95f19] hover:to-[#ff6000] text-white font-bold text-sm uppercase tracking-wide px-8 py-4 rounded-xl shadow-lg shadow-primary/20 hover:shadow-primary/40 transition-all duration-300 transform active:scale-95 group">
              Explore Courses
              <span className="material-symbols-outlined text-base group-hover:translate-x-0.5 transition-transform">local_library</span>
            </Link>
            <Link to="/problems" className="inline-flex items-center justify-center gap-2 bg-white hover:bg-surface-gray border border-slate-200 text-brand-blue font-bold text-sm uppercase tracking-wide px-8 py-4 rounded-xl shadow-md hover:shadow-lg transition-all duration-300 transform active:scale-95 group">
              Solve Problems
              <span className="material-symbols-outlined text-base text-brand-green group-hover:translate-x-0.5 transition-transform">code</span>
            </Link>
          </div>
        </div>

        {/* Interactive Mock IDE Sandbox */}
        <div className="lg:col-span-6 animate-float-slow">
          <div className="w-full bg-slate-900 rounded-2xl shadow-2xl border border-slate-800 overflow-hidden relative">
            {/* Mac buttons bar */}
            <div className="bg-slate-950 px-4 py-3 flex items-center justify-between border-b border-slate-900">
              <div className="flex gap-2">
                <span className="w-3 h-3 rounded-full bg-red-500 inline-block"></span>
                <span className="w-3 h-3 rounded-full bg-yellow-500 inline-block"></span>
                <span className="w-3 h-3 rounded-full bg-green-500 inline-block"></span>
              </div>
              <div className="text-xs text-slate-400 font-mono">{tabFilenames[activeLang]} — Interactive Sandbox</div>
              <span className="material-symbols-outlined text-slate-500 text-sm">settings</span>
            </div>

            {/* IDE Tabs */}
            <div className="bg-slate-900/60 px-4 py-1.5 flex gap-2 border-b border-slate-950/40 text-xs font-mono">
              <button
                onClick={() => selectLanguage('cpp')}
                className={
                  activeLang === 'cpp'
                    ? 'px-3 py-1 rounded bg-slate-850 text-brand-green font-bold border-b border-brand-green'
                    : 'px-3 py-1 rounded text-slate-400 hover:text-white transition-colors'
                }
              >
                two_sum.cpp
              </button>
              <button
                onClick={() => selectLanguage('py')}
                className={
                  activeLang === 'py'
                    ? 'px-3 py-1 rounded bg-slate-850 text-brand-green font-bold border-b border-brand-green'
                    : 'px-3 py-1 rounded text-slate-400 hover:text-white transition-colors'
                }
              >
                two_sum.py
              </button>
              <button
                onClick={() => selectLanguage('js')}
                className={
                  activeLang === 'js'
                    ? 'px-3 py-1 rounded bg-slate-850 text-brand-green font-bold border-b border-brand-green'
                    : 'px-3 py-1 rounded text-slate-400 hover:text-white transition-colors'
                }
              >
                two_sum.js
              </button>
            </div>

            {/* IDE Code Editor */}
            <div className="p-6 h-72 font-mono text-base overflow-hidden bg-slate-950 text-slate-300 hide-scrollbar">
              <pre
                className="text-left leading-relaxed"
                dangerouslySetInnerHTML={{ __html: codeSnippets[activeLang] }}
              />
            </div>

            {/* Run buttons & Terminal */}
            <div className="bg-slate-950 p-4 border-t border-slate-900">
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs text-slate-500 font-mono">Terminal Output</span>
                <button
                  onClick={runSandboxCode}
                  disabled={isRunning}
                  className="inline-flex items-center gap-1.5 bg-brand-green hover:bg-brand-green-hover text-white px-4 py-1.5 rounded-lg text-xs font-bold transition-all transform active:scale-95 shadow-md"
                >
                  {isRunning ? (
                    <>
                      Compiling... <span className="material-symbols-outlined text-[14px] animate-spin">sync</span>
                    </>
                  ) : (
                    <>
                      Run Code <span className="material-symbols-outlined text-[14px] icon-fill">play_arrow</span>
                    </>
                  )}
                </button>
              </div>
              <div
                ref={terminalRef}
                className="bg-slate-900 rounded-xl p-3 h-24 font-mono text-xs text-slate-400 overflow-y-auto text-left border border-slate-800 scrollbar-thin"
                dangerouslySetInnerHTML={{ __html: terminalHtml }}
              />
            </div>
          </div>
        </div>
      </section>

      {/* Section 2: Platform Strengths */}
      <section className="bg-surface/50 backdrop-blur-md border-y border-slate-200/50 py-16 md:py-24">
        <div className="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop text-center">

          <div className="mb-16">
            <div className="inline-flex items-center gap-1 bg-[#fce2d3] border border-primary/20 px-3 py-1 rounded-full text-primary font-bold text-xs uppercase tracking-wider mb-3">
              Core Capabilities
            </div>
            <h2 className="text-headline-lg md:text-4xl font-display font-black text-brand-blue">
              Our Platform <span className="bg-gradient-to-r from-primary to-[#ff8c42] bg-clip-text text-transparent">Strengths</span>
            </h2>
            <p className="text-body-md text-text-muted mt-2 max-w-lg mx-auto">Discover the game-changing tools designed to make coding interactive, competitive, and fun.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Strength 1: Gamified */}
            <div className="glassmorphism rounded-2xl p-8 text-left border border-slate-200/60 shadow-lg relative group transition-all duration-300 hover:shadow-xl hover:-translate-y-2">
              <div className="absolute -right-4 -top-4 w-12 h-12 bg-primary/10 rounded-full blur-xl"></div>
              <div className="w-12 h-12 rounded-2xl bg-primary-light flex items-center justify-center text-primary mb-6 shadow-sm border border-primary/10">
                <span className="material-symbols-outlined text-[26px]">military_tech</span>
              </div>
              <h3 className="font-display font-bold text-lg text-brand-blue mb-2">Gamified Coder Arena</h3>
              <p className="text-body-md text-text-muted leading-relaxed">
                Earn points for solving challenges, maintain daily streaks, obtain special coder titles, and climb the ranks from Novice to Grandmaster.
              </p>
            </div>

            {/* Strength 2: Online compiler */}
            <div className="glassmorphism rounded-2xl p-8 text-left border border-slate-200/60 shadow-lg relative group transition-all duration-300 hover:shadow-xl hover:-translate-y-2">
              <div className="absolute -right-4 -top-4 w-12 h-12 bg-brand-green/10 rounded-full blur-xl"></div>
              <div className="w-12 h-12 rounded-2xl bg-brand-green-light flex items-center justify-center text-brand-green mb-6 shadow-sm border border-brand-green/10">
                <span className="material-symbols-outlined text-[26px]">code_blocks</span>
              </div>
              <h3 className="font-display font-bold text-lg text-brand-blue mb-2">Browser-Based Sandbox</h3>
              <p className="text-body-md text-text-muted leading-relaxed">
                Write, compile, and submit code in Python, C++, and JavaScript directly in your browser with real-time test-case validations.
              </p>
            </div>

            {/* Strength 3: Curriculum */}
            <div className="glassmorphism rounded-2xl p-8 text-left border border-slate-200/60 shadow-lg relative group transition-all duration-300 hover:shadow-xl hover:-translate-y-2">
              <div className="absolute -right-4 -top-4 w-12 h-12 bg-blue-400/10 rounded-full blur-xl"></div>
              <div className="w-12 h-12 rounded-2xl bg-blue-100 flex items-center justify-center text-blue-600 mb-6 shadow-sm border border-blue-200/30">
                <span className="material-symbols-outlined text-[26px]">menu_book</span>
              </div>
              <h3 className="font-display font-bold text-lg text-brand-blue mb-2">Comprehensive Syllabus</h3>
              <p className="text-body-md text-text-muted leading-relaxed">
                Follow curated courses in Data Structures &amp; Algorithms, React Native, SQL Databases, and Web development, tailored from novice to pro levels.
              </p>
            </div>
          </div>

        </div>
      </section>

      {/* Section 3: Benefits to Users */}
      <section className="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop py-16 md:py-24">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">

          {/* Graphic Illustration mockup */}
          <div className="lg:col-span-5 order-2 lg:order-1 animate-float-medium">
            <div className="glassmorphism rounded-2xl p-6 shadow-xl border border-white/50 relative overflow-hidden text-left">
              <div className="absolute -top-10 -left-10 w-28 h-28 bg-brand-green/20 rounded-full blur-2xl"></div>

              <h4 className="font-display font-extrabold text-brand-blue text-base flex items-center gap-2 mb-4">
                <span className="material-symbols-outlined text-brand-green icon-fill text-[18px]">verified</span>
                Verified Achievements
              </h4>

              {/* Certificate Mock */}
              <div className="bg-gradient-to-r from-brand-blue to-brand-blue-light rounded-xl p-4 text-white relative shadow-md mb-4">
                <div className="text-[9px] uppercase tracking-wider text-primary font-black mb-1">Nonstop Certified</div>
                <h5 className="text-sm font-bold tracking-wide">Data Structures &amp; Algorithms</h5>
                <p className="text-[10px] text-white/60 mt-1">Awarded to: Nguyen Van A</p>
                <span className="material-symbols-outlined absolute right-4 bottom-4 text-primary text-3xl opacity-80 icon-fill">stars</span>
              </div>

              {/* Reward Stats list */}
              <div className="flex flex-col gap-3">
                <div className="flex items-center justify-between p-2 rounded-lg bg-surface-gray border border-gray-100">
                  <span className="text-xs font-bold text-text-main flex items-center gap-1.5"><span className="material-symbols-outlined text-sm text-amber-500 icon-fill">emoji_events</span> Interprovincial Contest #5</span>
                  <span className="text-xs font-black text-brand-green">+250 LC</span>
                </div>
                <div className="flex items-center justify-between p-2 rounded-lg bg-surface-gray border border-gray-100">
                  <span className="text-xs font-bold text-text-main flex items-center gap-1.5"><span className="material-symbols-outlined text-sm text-primary icon-fill">workspace_premium</span> Daily Coder Streak: 15 Days</span>
                  <span className="text-xs font-black text-brand-green">+50 LC</span>
                </div>
              </div>
            </div>
          </div>

          {/* Benefits Text */}
          <div className="lg:col-span-7 order-1 lg:order-2 flex flex-col items-start text-left">
            <div className="inline-flex items-center gap-1 bg-brand-green-light border border-brand-green/20 px-3 py-1 rounded-full text-brand-green font-bold text-xs uppercase tracking-wider mb-3">
              Unlock Value
            </div>
            <h2 className="text-headline-lg md:text-4xl font-display font-black text-brand-blue leading-tight mb-4">
              Propel Your <span className="bg-gradient-to-r from-brand-green to-[#5cb85c] bg-clip-text text-transparent">Tech Career</span> Forward
            </h2>
            <p className="text-body-md text-text-muted mb-8 leading-relaxed">
              Designed by software engineers for aspiring developers. We provide direct benefits to help you master algorithms, build full-stack projects, and land high-paying software jobs.
            </p>

            {/* Benefit Rows */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 w-full">
              <div className="flex gap-3">
                <span className="material-symbols-outlined text-brand-green text-[22px] shrink-0 mt-0.5 icon-fill">check_circle</span>
                <div>
                  <h4 className="font-display font-bold text-sm text-brand-blue">Interview Ready</h4>
                  <p className="text-xs text-text-muted mt-1 leading-relaxed">Master 150+ popular coding interview patterns asked in top-tier companies.</p>
                </div>
              </div>
              <div className="flex gap-3">
                <span className="material-symbols-outlined text-brand-green text-[22px] shrink-0 mt-0.5 icon-fill">check_circle</span>
                <div>
                  <h4 className="font-display font-bold text-sm text-brand-blue">Gamified Retention</h4>
                  <p className="text-xs text-text-muted mt-1 leading-relaxed">Coding shouldn't be dry. Build habits with fun daily streaks and real-time leagues.</p>
                </div>
              </div>
              <div className="flex gap-3">
                <span className="material-symbols-outlined text-brand-green text-[22px] shrink-0 mt-0.5 icon-fill">check_circle</span>
                <div>
                  <h4 className="font-display font-bold text-sm text-brand-blue">E-Portfolio &amp; Badges</h4>
                  <p className="text-xs text-text-muted mt-1 leading-relaxed">Showcase your profile ranks, coding achievements, and credentials directly to hiring partners.</p>
                </div>
              </div>
              <div className="flex gap-3">
                <span className="material-symbols-outlined text-brand-green text-[22px] shrink-0 mt-0.5 icon-fill">check_circle</span>
                <div>
                  <h4 className="font-display font-bold text-sm text-brand-blue">Mentorship &amp; Forums</h4>
                  <p className="text-xs text-text-muted mt-1 leading-relaxed">Get unstuck in minutes with our active developers forum and integrated course Q&amp;As.</p>
                </div>
              </div>
            </div>
          </div>

        </div>
      </section>

      {/* Section 4: Featured Courses */}
      <section className="bg-surface/50 backdrop-blur-md border-y border-slate-200/50 py-16 md:py-24">
        <div className="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop text-center">

          <div className="mb-16">
            <div className="inline-flex items-center gap-1 bg-blue-100 border border-blue-200 px-3 py-1 rounded-full text-blue-700 font-bold text-xs uppercase tracking-wider mb-3">
              Premium Syllabus
            </div>
            <h2 className="text-headline-lg md:text-4xl font-display font-black text-brand-blue">
              Featured <span className="bg-gradient-to-r from-[#0114a7] to-[#2563eb] bg-clip-text text-transparent">Learning Programs</span>
            </h2>
            <p className="text-body-md text-text-muted mt-2 max-w-lg mx-auto">Upgrade your skills with our highly rated, professional-grade technical courses.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-left">
            {/* Course 1: DSA */}
            <div className="glassmorphism rounded-2xl overflow-hidden border border-slate-200/50 shadow-lg flex flex-col h-full hover:shadow-xl hover:scale-[1.01] transition-all duration-300">
              <div className="h-44 bg-gradient-to-br from-primary/10 to-primary-light/50 relative overflow-hidden flex items-center justify-center p-4">
                <span className="material-symbols-outlined text-primary text-[72px] opacity-75">mediation</span>
                <div className="absolute top-3 left-3 bg-primary text-white text-[10px] font-extrabold uppercase px-2 py-0.5 rounded shadow-sm">Bestseller</div>
              </div>

              <div className="p-6 flex flex-col flex-grow">
                <div className="flex items-center gap-1 text-amber-500 mb-2">
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="text-xs text-text-muted font-bold ml-1">4.9 (4,235)</span>
                </div>
                <h4 className="font-display font-bold text-lg text-brand-blue mb-2 line-clamp-2">Data Structures &amp; Algorithms Mastery</h4>
                <p className="text-xs text-text-muted mb-4 leading-relaxed line-clamp-3">Master trees, graphs, sorting, searching, and dynamic programming to crack coding interviews at Google, Meta, and FAANG.</p>

                <div className="flex items-center justify-between text-xs text-text-muted py-2 border-y border-gray-100 mb-4 mt-auto">
                  <span>📚 48 Lessons</span>
                  <span>👥 14.5K Students</span>
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex flex-col">
                    <span className="text-caption text-text-muted uppercase font-bold tracking-wide">Course Price</span>
                    <span className="font-display font-black text-brand-blue text-lg">799,000 ₫</span>
                  </div>
                  <Link to="/courses" className="bg-primary hover:bg-primary-hover text-white font-bold text-xs uppercase tracking-wide px-4 py-2.5 rounded-lg shadow-md transition-all active:scale-95">Enroll now</Link>
                </div>
              </div>
            </div>

            {/* Course 2: React FullStack */}
            <div className="glassmorphism rounded-2xl overflow-hidden border border-slate-200/50 shadow-lg flex flex-col h-full hover:shadow-xl hover:scale-[1.01] transition-all duration-300">
              <div className="h-44 bg-gradient-to-br from-blue-400/10 to-blue-200/30 relative overflow-hidden flex items-center justify-center p-4">
                <span className="material-symbols-outlined text-blue-500 text-[72px] opacity-75">javascript</span>
                <div className="absolute top-3 left-3 bg-[#3498db] text-white text-[10px] font-extrabold uppercase px-2 py-0.5 rounded shadow-sm">Popular</div>
              </div>

              <div className="p-6 flex flex-col flex-grow">
                <div className="flex items-center gap-1 text-amber-500 mb-2">
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star_half</span>
                  <span className="text-xs text-text-muted font-bold ml-1">4.8 (2,810)</span>
                </div>
                <h4 className="font-display font-bold text-lg text-brand-blue mb-2 line-clamp-2">Mastering Full-Stack Web Development</h4>
                <p className="text-xs text-text-muted mb-4 leading-relaxed line-clamp-3">Build modern SaaS products, API integrations, and database architectures using React, Node.js, Express, and Tailwind CSS.</p>

                <div className="flex items-center justify-between text-xs text-text-muted py-2 border-y border-gray-100 mb-4 mt-auto">
                  <span>📚 65 Lessons</span>
                  <span>👥 9.2K Students</span>
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex flex-col">
                    <span className="text-caption text-text-muted uppercase font-bold tracking-wide">Course Price</span>
                    <span className="font-display font-black text-brand-blue text-lg">950,000 ₫</span>
                  </div>
                  <Link to="/courses" className="bg-primary hover:bg-primary-hover text-white font-bold text-xs uppercase tracking-wide px-4 py-2.5 rounded-lg shadow-md transition-all active:scale-95">Enroll now</Link>
                </div>
              </div>
            </div>

            {/* Course 3: Python Beginners */}
            <div className="glassmorphism rounded-2xl overflow-hidden border border-slate-200/50 shadow-lg flex flex-col h-full hover:shadow-xl hover:scale-[1.01] transition-all duration-300">
              <div className="h-44 bg-gradient-to-br from-brand-green/10 to-brand-green-light/65 relative overflow-hidden flex items-center justify-center p-4">
                <span className="material-symbols-outlined text-brand-green text-[72px] opacity-75">terminal</span>
                <div className="absolute top-3 left-3 bg-brand-green text-white text-[10px] font-extrabold uppercase px-2 py-0.5 rounded shadow-sm">Starter</div>
              </div>

              <div className="p-6 flex flex-col flex-grow">
                <div className="flex items-center gap-1 text-amber-500 mb-2">
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="material-symbols-outlined text-[16px] icon-fill">star</span>
                  <span className="text-xs text-text-muted font-bold ml-1">5.0 (1,495)</span>
                </div>
                <h4 className="font-display font-bold text-lg text-brand-blue mb-2 line-clamp-2">Python Fundamentals &amp; Automation</h4>
                <p className="text-xs text-text-muted mb-4 leading-relaxed line-clamp-3">The ideal introduction to writing clean, reliable code. Automate office tasks, parse datasets, and build scripts from scratch.</p>

                <div className="flex items-center justify-between text-xs text-text-muted py-2 border-y border-gray-100 mb-4 mt-auto">
                  <span>📚 32 Lessons</span>
                  <span>👥 5.4K Students</span>
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex flex-col">
                    <span className="text-caption text-text-muted uppercase font-bold tracking-wide">Course Price</span>
                    <span className="font-display font-black text-brand-blue text-lg">450,000 ₫</span>
                  </div>
                  <Link to="/courses" className="bg-primary hover:bg-primary-hover text-white font-bold text-xs uppercase tracking-wide px-4 py-2.5 rounded-lg shadow-md transition-all active:scale-95">Enroll now</Link>
                </div>
              </div>
            </div>
          </div>

          {/* View all link */}
          <div className="mt-12">
            <Link to="/courses" className="inline-flex items-center gap-1 text-sm font-bold text-primary hover:text-primary-hover transition-colors group">
              View all premium courses
              <span className="material-symbols-outlined text-sm group-hover:translate-x-0.5 transition-transform">arrow_forward</span>
            </Link>
          </div>

        </div>
      </section>

      {/* Section 5: Trust & Stats Count */}
      <section className="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop py-16 md:py-24 text-center">
        <div className="glassmorphism rounded-3xl p-8 md:p-12 shadow-xl border border-white/50 relative overflow-hidden">
          <div className="absolute -top-40 -left-40 w-96 h-96 bg-brand-green/5 rounded-full blur-[100px]"></div>

          <div className="grid grid-cols-2 lg:grid-cols-4 gap-8 divide-y divide-gray-100 lg:divide-y-0 lg:divide-x divide-slate-200/50">
            <div className="flex flex-col items-center p-4">
              <span className="text-3xl md:text-4xl font-display font-black text-transparent bg-clip-text bg-gradient-to-r from-[#0114a7] to-[#2563eb]">50,000+</span>
              <span className="text-xs md:text-sm font-bold text-text-muted uppercase tracking-wider mt-2">Active Students</span>
            </div>
            <div className="flex flex-col items-center p-4 pt-8 lg:pt-4">
              <span className="text-3xl md:text-4xl font-display font-black text-transparent bg-clip-text bg-gradient-to-r from-[#ff6000] to-[#ff8c42]">1,200+</span>
              <span className="text-xs md:text-sm font-bold text-text-muted uppercase tracking-wider mt-2">Coding Problems</span>
            </div>
            <div className="flex flex-col items-center p-4 pt-8 lg:pt-4">
              <span className="text-3xl md:text-4xl font-display font-black text-transparent bg-clip-text bg-gradient-to-r from-[#46A040] to-[#5cb85c]">98%</span>
              <span className="text-xs md:text-sm font-bold text-text-muted uppercase tracking-wider mt-2">Career Placements</span>
            </div>
            <div className="flex flex-col items-center p-4 pt-8 lg:pt-4">
              <span className="text-3xl md:text-4xl font-display font-black text-transparent bg-clip-text bg-gradient-to-r from-[#0114a7] to-[#3b82f6]">150+</span>
              <span className="text-xs md:text-sm font-bold text-text-muted uppercase tracking-wider mt-2">Weekly Contests</span>
            </div>
          </div>
        </div>
      </section>

      {/* Section 6: Testimonials */}
      <section className="bg-surface/50 backdrop-blur-md border-y border-slate-200/50 py-16 md:py-24">
        <div className="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop text-center">

          <div className="mb-16">
            <div className="inline-flex items-center gap-1 bg-brand-green-light border border-brand-green/20 px-3 py-1 rounded-full text-brand-green font-bold text-xs uppercase tracking-wider mb-3">
              Student Stories
            </div>
            <h2 className="text-headline-lg md:text-4xl font-display font-black text-brand-blue">
              What Our <span className="bg-gradient-to-r from-[#46A040] to-[#5cb85c] bg-clip-text text-transparent">Developers Say</span>
            </h2>
            <p className="text-body-md text-text-muted mt-2 max-w-lg mx-auto">Real success stories from developers who cracked technical interviews using our sandbox.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-left">
            {/* Card 1 */}
            <div className="glassmorphism rounded-2xl p-6 shadow-md border border-slate-200/40 relative">
              <span className="material-symbols-outlined text-4xl text-brand-green/20 absolute right-4 top-4">format_quote</span>
              <p className="text-sm text-text-main italic leading-relaxed mb-6">
                "The interactive compiler and immediate test-case run saved me hours of config. Mastering recursion and graphs here was a game changer. I just landed a Software Engineer role at VNG!"
              </p>
              <div className="flex items-center gap-3">
                <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80" alt="Student avatar" className="w-10 h-10 rounded-full object-cover" />
                <div>
                  <h4 className="font-display font-bold text-sm text-brand-blue">Linh Tran</h4>
                  <span className="text-[10px] text-text-muted block">SWE @ VNG Corporation</span>
                </div>
              </div>
            </div>

            {/* Card 2 */}
            <div className="glassmorphism rounded-2xl p-6 shadow-md border border-slate-200/40 relative">
              <span className="material-symbols-outlined text-4xl text-brand-green/20 absolute right-4 top-4">format_quote</span>
              <p className="text-sm text-text-main italic leading-relaxed mb-6">
                "I used to find algorithmic problems incredibly intimidating. Nonstop Coding's gamified leaderboards and streaks kept me practicing daily. The community forum is also extremely supportive!"
              </p>
              <div className="flex items-center gap-3">
                <img src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80" alt="Student avatar" className="w-10 h-10 rounded-full object-cover" />
                <div>
                  <h4 className="font-display font-bold text-sm text-brand-blue">Minh Nguyen</h4>
                  <span className="text-[10px] text-text-muted block">Junior Frontend Dev</span>
                </div>
              </div>
            </div>

            {/* Card 3 */}
            <div className="glassmorphism rounded-2xl p-6 shadow-md border border-slate-200/40 relative">
              <span className="material-symbols-outlined text-4xl text-brand-green/20 absolute right-4 top-4">format_quote</span>
              <p className="text-sm text-text-main italic leading-relaxed mb-6">
                "The structured course syllabus was incredibly easy to follow. The balance between video guides and direct code solving on the sandbox was perfect. Highly recommend the DSA module!"
              </p>
              <div className="flex items-center gap-3">
                <img src="https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=150&q=80" alt="Student avatar" className="w-10 h-10 rounded-full object-cover" />
                <div>
                  <h4 className="font-display font-bold text-sm text-brand-blue">Huy Pham</h4>
                  <span className="text-[10px] text-text-muted block">Data Analyst @ FPT</span>
                </div>
              </div>
            </div>
          </div>

        </div>
      </section>

      {/* Section 7: FAQ Accordion */}
      <section className="max-w-[800px] mx-auto px-margin-mobile md:px-margin-desktop py-16 md:py-24 text-center">
        <div className="mb-12">
          <h2 className="text-headline-lg font-display font-black text-brand-blue">Frequently Asked Questions</h2>
          <p className="text-body-md text-text-muted mt-2">Have questions about our platform? We've got answers.</p>
        </div>

        <div className="flex flex-col gap-4 text-left">
          {/* FAQ 1 */}
          <div className="glassmorphism rounded-xl border border-slate-200/50 overflow-hidden shadow-sm">
            <details className="group" open>
              <summary className="flex justify-between items-center p-5 font-display font-bold text-sm text-brand-blue cursor-pointer list-none select-none">
                Is Nonstop Coding free to use?
                <span className="material-symbols-outlined text-slate-500 group-open:rotate-180 transition-transform">expand_more</span>
              </summary>
              <div className="px-5 pb-5 text-xs text-text-muted leading-relaxed">
                Yes! You can register and access all 1200+ practice challenges and participate in weekly ranked contests absolutely free of charge. Premium, structured full-syllabus courses require a one-time tuition purchase.
              </div>
            </details>
          </div>

          {/* FAQ 2 */}
          <div className="glassmorphism rounded-xl border border-slate-200/50 overflow-hidden shadow-sm">
            <details className="group">
              <summary className="flex justify-between items-center p-5 font-display font-bold text-sm text-brand-blue cursor-pointer list-none select-none">
                Do I need to install any compilation toolkits?
                <span className="material-symbols-outlined text-slate-500 group-open:rotate-180 transition-transform">expand_more</span>
              </summary>
              <div className="px-5 pb-5 text-xs text-text-muted leading-relaxed">
                No installation required! The platform has an integrated online sandbox compiler. You can write, test, and submit solutions directly inside your standard web browser.
              </div>
            </details>
          </div>

          {/* FAQ 3 */}
          <div className="glassmorphism rounded-xl border border-slate-200/50 overflow-hidden shadow-sm">
            <details className="group">
              <summary className="flex justify-between items-center p-5 font-display font-bold text-sm text-brand-blue cursor-pointer list-none select-none">
                Can I showcase my certificates on LinkedIn?
                <span className="material-symbols-outlined text-slate-500 group-open:rotate-180 transition-transform">expand_more</span>
              </summary>
              <div className="px-5 pb-5 text-xs text-text-muted leading-relaxed">
                Absolutely. Once you successfully complete a premium course syllabus and its practice exams, you receive a dynamic certificate of competency which you can directly post on LinkedIn or include in your resume.
              </div>
            </details>
          </div>
        </div>
      </section>

      {/* Section 8: Final Sign Up CTA */}
      <section className="max-w-[1280px] mx-auto px-margin-mobile md:px-margin-desktop pb-16 md:pb-24">
        <div className="bg-gradient-to-r from-brand-blue to-[#1a386b] rounded-3xl shadow-2xl p-8 md:p-16 text-center border border-primary/20 relative overflow-hidden animate-pulse-glow-orange">
          <div className="absolute inset-0 bg-gradient-to-r from-primary/5 via-white/5 to-transparent pointer-events-none"></div>

          <div className="relative z-10 max-w-2xl mx-auto flex flex-col items-center">
            <div className="text-primary text-xs font-black uppercase tracking-widest mb-3">Instant Access</div>
            <h2 className="font-display font-black text-white text-3xl md:text-5xl leading-tight mb-4">
              Ready to Start Your <br />
              <span className="bg-gradient-to-r from-primary to-[#ff8c42] bg-clip-text text-transparent">Nonstop Journey?</span>
            </h2>
            <p className="text-sm md:text-base text-white/70 mb-8 max-w-md leading-relaxed">
              Create a free account, test your algorithm skills on our sandbox, and join a global developer community.
            </p>

            <Link to="/register" className="inline-flex items-center justify-center gap-2 bg-gradient-to-r from-[#ff6000] to-[#ff8c42] hover:from-[#d95f19] hover:to-[#ff6000] text-white font-bold text-sm uppercase tracking-wide px-8 py-4 rounded-xl shadow-lg shadow-primary/25 hover:shadow-primary/45 transition-all duration-300 transform active:scale-95 group">
              Create Free Account
              <span className="material-symbols-outlined text-base group-hover:translate-x-0.5 transition-transform">person_add</span>
            </Link>
          </div>
        </div>
      </section>
    </>
  );
};

export default Home;
