import React, { useState, useMemo, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { problemService } from '../services/problemService';
import type { ProblemListItem } from '../services/problemService';
import { useApp } from '../context/AppContext';

export const Problems: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useApp();

  // State controls for filtering and searching
  const [problems, setProblems] = useState<ProblemListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [difficultyFilter, setDifficultyFilter] = useState('all');
  const [topicFilter, setTopicFilter] = useState('all');
  const [sortBy, setSortBy] = useState('recent');
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    problemService.fetchProblems()
      .then(data => {
        setProblems(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  const solvedCount = useMemo(() => problems.filter(p => p.isSolved).length, [problems]);
  const totalCount = problems.length;

  // Toggle helper for popular tags
  const toggleTopic = (topic: string) => {
    if (topicFilter === topic) {
      setTopicFilter('all');
    } else {
      setTopicFilter(topic);
    }
  };

  // Helper matching logic for topics
  const matchesTopic = (problemTags: string[], topic: string) => {
    if (topic === 'all') return true;
    const normalized = topic.toLowerCase();
    return problemTags.some(t => {
      const tag = t.toLowerCase();
      if (normalized === 'arrays' && (tag === 'array' || tag === 'arrays')) return true;
      if (normalized === 'hashmap' && (tag === 'hash table' || tag === 'hash map')) return true;
      if (normalized === 'dp' && tag === 'dynamic programming') return true;
      if (normalized === 'twopointers' && tag === 'two pointers') return true;
      if (normalized === 'math' && tag === 'math') return true;
      if (normalized === 'string' && tag === 'string') return true;
      return tag === normalized;
    });
  };

  // Dynamically filtered and sorted problem set
  const filteredAndSortedProblems = useMemo(() => {
    let result = [...problems];

    // Search query
    if (searchQuery.trim() !== '') {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        p =>
          p.title.toLowerCase().includes(q) ||
          p.tags.some(t => t.toLowerCase().includes(q))
      );
    }

    // Status filter
    if (statusFilter !== 'all') {
      if (statusFilter === 'solved') {
        result = result.filter(p => p.isSolved);
      } else if (statusFilter === 'unsolved') {
        result = result.filter(p => !p.isSolved);
      }
    }

    // Difficulty filter
    if (difficultyFilter !== 'all') {
      result = result.filter(
        p => p.difficulty.toLowerCase() === difficultyFilter.toLowerCase()
      );
    }

    // Topic/category filter
    if (topicFilter !== 'all') {
      result = result.filter(p => matchesTopic(p.tags, topicFilter));
    }

    // Sort order
    if (sortBy === 'success_desc') {
      result.sort((a, b) => {
        const rateA = a.totalSubmission > 0 ? (a.totalAccepted / a.totalSubmission) : 0;
        const rateB = b.totalSubmission > 0 ? (b.totalAccepted / b.totalSubmission) : 0;
        return rateB - rateA;
      });
    } else if (sortBy === 'submissions_desc') {
      result.sort((a, b) => {
        return b.totalSubmission - a.totalSubmission;
      });
    } else {
      // 'recent' -> default by ID/number ascending
      result.sort((a, b) => a.id - b.id);
    }

    return result;
  }, [problems, searchQuery, statusFilter, difficultyFilter, topicFilter, sortBy]);

  const itemsPerPage = 10;
  const totalPages = Math.max(1, Math.ceil(filteredAndSortedProblems.length / itemsPerPage));

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [filteredAndSortedProblems.length, totalPages, currentPage]);

  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedProblems = useMemo(() => {
    return filteredAndSortedProblems.slice(startIndex, startIndex + itemsPerPage);
  }, [filteredAndSortedProblems, startIndex]);

  if (loading) {
    return (
      <div className="relative z-10 flex-grow w-full max-w-[1440px] mx-auto px-4 lg:px-8 py-xxl pt-8 text-center">
        <div className="flex flex-col items-center justify-center min-h-[400px]">
          <svg className="animate-spin h-10 w-10 text-primary mb-4" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
          <span className="text-text-muted font-semibold">Loading practice arena...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="relative z-10 flex-grow w-full max-w-[1440px] mx-auto px-4 lg:px-8 py-xxl pt-8 text-center">
        <div className="flex flex-col items-center justify-center min-h-[400px] text-red-600">
          <span className="material-symbols-outlined text-[48px] mb-2">error</span>
          <span className="font-bold mb-2">Error Loading Problems</span>
          <span className="text-sm text-text-muted">{error}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="relative z-10 flex-grow w-full max-w-[1440px] mx-auto px-4 lg:px-8 py-xxl pt-8 text-left">
      {/* Page Header */}
      <header className="mb-xl flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          {/* Decorative Badge */}
          <div className="inline-flex items-center gap-1.5 bg-[#fce2d3] border border-primary/20 px-3 py-1 rounded-full text-primary font-bold text-xs uppercase tracking-wider mb-3 shadow-sm relative z-10">
            <span className="material-symbols-outlined text-xs icon-fill">military_tech</span> Practice Arena
          </div>
          <h1 className="text-display-lg-mobile md:text-display-lg font-display font-black leading-tight relative z-10">
            <span className="bg-gradient-to-r from-[#0114a7] to-[#2563eb] bg-clip-text text-transparent">Practice</span>{' '}
            <span className="bg-gradient-to-r from-[#ff6000] to-[#ff8c42] bg-clip-text text-transparent">Problems</span>
          </h1>
          <p className="font-body-lg text-body-lg text-text-muted max-w-2xl mt-1">
            Enhance your algorithmic skills and prepare for technical interviews with our curated list of problems.
          </p>
        </div>
        <div className="flex items-center gap-2 bg-surface p-4 rounded-xl shadow-sm border border-outline-variant/50 relative z-10">
          <div className="flex flex-col items-center px-4 border-r border-outline-variant/30">
            <span className="text-caption font-bold text-text-muted uppercase">Solved</span>
            <span className="text-headline-md font-bold text-brand-green">{solvedCount}</span>
          </div>
          <div className="flex flex-col items-center px-4">
            <span className="text-caption font-bold text-text-muted uppercase">Total</span>
            <span className="text-headline-md font-bold text-text-main">{totalCount}</span>
          </div>
        </div>
      </header>

      {/* Search & Filter Section */}
      <div className="bg-surface p-4 rounded-xl shadow-sm border border-sky-100/70 mb-6 flex flex-col gap-3 relative overflow-hidden">
        <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-primary via-sky-400 to-brand-blue"></div>

        <div className="flex flex-col lg:flex-row gap-3 items-center justify-between mt-1">
          {/* Search Bar */}
          <form
            onSubmit={(e) => e.preventDefault()}
            className="relative flex-grow w-full lg:max-w-xl flex items-center shadow-sm hover:shadow transition-shadow rounded-lg border border-sky-100 bg-surface group focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/10"
          >
            <span className="material-symbols-outlined absolute left-3 text-text-muted group-focus-within:text-primary text-[20px]">
              search
            </span>
            <input
              type="text"
              placeholder="Search problems, topics..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-24 py-2 border-none focus:ring-0 text-sm text-brand-blue font-medium outline-none rounded-lg"
            />
            <button
              type="submit"
              className="absolute right-1 px-4 py-1.5 bg-gradient-to-r from-primary to-orange-500 hover:from-primary-hover hover:to-orange-600 text-white font-bold text-xs rounded-md transition-all shadow-sm active:scale-95"
            >
              Search
            </button>
          </form>

          {/* Filters Top */}
          <div className="flex flex-wrap items-center gap-2 w-full lg:w-auto">
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="pl-3 pr-9 py-2 border border-gray-200 hover:border-sky-300 rounded-lg text-sm text-brand-blue font-semibold bg-gray-50 focus:bg-surface focus:ring-1 focus:ring-sky-100 focus:border-sky-400 transition-all cursor-pointer outline-none"
            >
              <option value="all">Status: All</option>
              <option value="solved">Solved</option>
              <option value="unsolved">Unsolved</option>
            </select>

            <select
              value={difficultyFilter}
              onChange={(e) => setDifficultyFilter(e.target.value)}
              className="pl-3 pr-9 py-2 border border-gray-200 hover:border-orange-300 rounded-lg text-sm text-brand-blue font-semibold bg-gray-50 focus:bg-surface focus:ring-1 focus:ring-orange-100 focus:border-primary transition-all cursor-pointer outline-none"
            >
              <option value="all">Difficulty: All</option>
              <option value="easy">Easy</option>
              <option value="medium">Medium</option>
              <option value="hard">Hard</option>
            </select>
          </div>
        </div>

        {/* Popular Topic Tags & Filters Bottom */}
        <div className="flex flex-wrap lg:flex-nowrap items-center justify-between gap-3 border-t border-gray-100 pt-3">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xs text-brand-blue font-semibold mr-1 flex items-center gap-1">
              <span className="material-symbols-outlined text-primary text-[14px]">local_fire_department</span>
              Popular:
            </span>
            <button
              onClick={() => toggleTopic('arrays')}
              className={`px-2.5 py-1 rounded-md text-xs font-semibold transition-all border ${
                topicFilter === 'arrays'
                  ? 'bg-primary text-white border-primary'
                  : 'bg-primary-light/20 hover:bg-primary hover:text-white border-primary/20 text-primary'
              }`}
            >
              Arrays
            </button>
            <button
              onClick={() => toggleTopic('hashmap')}
              className={`px-2.5 py-1 rounded-md text-xs font-semibold transition-all border ${
                topicFilter === 'hashmap'
                  ? 'bg-sky-500 text-white border-sky-500'
                  : 'bg-sky-50 hover:bg-sky-500 hover:text-white border-sky-200 text-sky-600'
              }`}
            >
              Hash Map
            </button>
            <button
              onClick={() => toggleTopic('dp')}
              className={`px-2.5 py-1 rounded-md text-xs font-semibold transition-all border ${
                topicFilter === 'dp'
                  ? 'bg-brand-blue text-white border-brand-blue'
                  : 'bg-brand-blue/5 hover:bg-brand-blue hover:text-white border-brand-blue/10 text-brand-blue'
              }`}
            >
              Dynamic Programming
            </button>
            <button
              onClick={() => toggleTopic('twopointers')}
              className={`px-2.5 py-1 rounded-md text-xs font-semibold transition-all border ${
                topicFilter === 'twopointers'
                  ? 'bg-sky-500 text-white border-sky-500'
                  : 'bg-sky-50 hover:bg-sky-500 hover:text-white border-sky-200 text-sky-600'
              }`}
            >
              Two Pointers
            </button>
            <button
              onClick={() => toggleTopic('math')}
              className={`px-2.5 py-1 rounded-md text-xs font-semibold transition-all border ${
                topicFilter === 'math'
                  ? 'bg-primary text-white border-primary'
                  : 'bg-primary-light/20 hover:bg-primary hover:text-white border-primary/20 text-primary'
              }`}
            >
              Math
            </button>
          </div>

          {/* Filters Bottom */}
          <div className="flex flex-wrap items-center gap-2 shrink-0">
            <select
              value={topicFilter}
              onChange={(e) => setTopicFilter(e.target.value)}
              className="pl-3 pr-9 py-2 border border-gray-200 hover:border-sky-300 rounded-lg text-sm text-brand-blue font-semibold bg-gray-50 focus:bg-surface focus:ring-1 focus:ring-sky-100 focus:border-sky-400 transition-all cursor-pointer outline-none"
            >
              <option value="all">Topics: All</option>
              <option value="arrays">Arrays</option>
              <option value="hashmap">Hash Map</option>
              <option value="string">String</option>
              <option value="dp">Dynamic Programming</option>
              <option value="twopointers">Two Pointers</option>
              <option value="math">Math</option>
            </select>

            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="pl-3 pr-9 py-2 border border-gray-200 hover:border-brand-blue/30 rounded-lg text-sm text-brand-blue font-semibold bg-gray-50 focus:bg-surface focus:ring-1 focus:ring-blue-100 focus:border-brand-blue transition-all cursor-pointer outline-none"
            >
              <option value="recent">Sort: Newest</option>
              <option value="success_desc">Success Rate</option>
              <option value="submissions_desc">Total Submissions</option>
            </select>
          </div>
        </div>
      </div>

      {/* Problems Table */}
      <div className="bg-surface rounded-xl shadow-sm border border-outline-variant/50 overflow-hidden mb-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-gray border-b border-outline-variant/50 text-text-muted font-label-md text-label-md">
                <th className="p-4 pl-6 font-semibold w-12 text-center">Status</th>
                <th className="p-4 font-semibold min-w-[300px]">Title</th>
                <th className="p-4 font-semibold w-32">Difficulty</th>
                <th className="p-4 font-semibold w-24 text-right">Score</th>
                <th className="p-4 font-semibold w-32 text-right">Acceptance</th>
                <th className="p-4 font-semibold w-40 text-right pr-6">Submissions</th>
              </tr>
            </thead>
            <tbody className="text-body-md font-body-md text-text-main divide-y divide-outline-variant/30">
              {paginatedProblems.map((prob) => (
                <tr
                  key={prob.id}
                  onClick={() => {
                    if (!user) {
                      navigate('/login');
                    } else {
                      navigate(`/problems/${prob.id}`);
                    }
                  }}
                  className="hover:bg-surface-gray/50 transition-colors group cursor-pointer"
                >
                  <td className="p-4 pl-6 text-center">
                    {prob.status === 'solved' && (
                      <span className="material-symbols-outlined text-[20px] text-brand-green">
                        check_circle
                      </span>
                    )}
                    {prob.status === 'attempted' && (
                      <span className="material-symbols-outlined text-[20px] text-red-500">
                        cancel
                      </span>
                    )}
                  </td>
                  <td className="p-4">
                    <Link
                      to={!user ? '/login' : `/problems/${prob.id}`}
                      onClick={(e) => e.stopPropagation()}
                      className="font-bold text-text-main group-hover:text-primary transition-colors"
                    >
                      {prob.title}
                    </Link>
                    <div className="text-xs text-text-muted mt-1 flex gap-2">
                      {prob.tags.map((tag) => (
                        <span
                          key={tag}
                          className="bg-surface-gray px-1.5 py-0.5 rounded text-text-muted border border-outline-variant/50"
                        >
                          {tag}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="p-4">
                    {prob.difficulty === 'Easy' && <span className="text-brand-green font-medium">Easy</span>}
                    {prob.difficulty === 'Medium' && <span className="text-orange-500 font-medium">Medium</span>}
                    {prob.difficulty === 'Hard' && <span className="text-red-600 font-medium">Hard</span>}
                  </td>
                  <td className="p-4 text-right font-mono text-sm">{prob.score}</td>
                  <td className="p-4 text-right">{prob.totalSubmission > 0 ? (prob.totalAccepted * 100 / prob.totalSubmission).toFixed(1) + '%' : '0.0%'}</td>
                  <td className="p-4 text-right pr-6 font-mono text-sm">{prob.totalSubmission}</td>
                </tr>
              ))}
              {filteredAndSortedProblems.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-text-muted">
                    No problems match your filter selection.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="p-6 border-t border-outline-variant/50 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-4 text-sm text-text-muted">
            <span>
              Showing {filteredAndSortedProblems.length > 0 ? startIndex + 1 : 0} to{' '}
              {Math.min(startIndex + itemsPerPage, filteredAndSortedProblems.length)} of {filteredAndSortedProblems.length} problems
            </span>
            <span className="text-xs font-semibold text-text-muted bg-surface-gray border border-outline-variant/50 rounded px-2.5 py-1">
              10 / page
            </span>
          </div>
          
          {totalPages > 1 && (
            <div className="flex gap-1 items-center">
              <button
                onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
                className="w-8 h-8 rounded border border-outline-variant/50 flex items-center justify-center text-text-muted hover:bg-surface-gray disabled:opacity-50"
              >
                <span className="material-symbols-outlined text-[18px]">chevron_left</span>
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
                        className={`w-8 h-8 rounded flex items-center justify-center text-sm ${
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
                className="w-8 h-8 rounded border border-outline-variant/50 flex items-center justify-center text-text-muted hover:bg-surface-gray disabled:opacity-50"
              >
                <span className="material-symbols-outlined text-[18px]">chevron_right</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
