import React from 'react';
import { useOutletContext } from 'react-router-dom';
import type { ContestOverviewData } from '../components/Layout';

export const ContestOverview: React.FC = () => {
  const { contest, loading, error } = useOutletContext<{
    contest: ContestOverviewData | null;
    loading: boolean;
    error: string | null;
  }>();

  const formatDate = (dateString?: string) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };


  if (loading) {
    return (
      <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow animate-pulse">
        <div className="max-w-[1280px] mx-auto flex flex-col gap-8">
          <div className="h-64 bg-gray-200 rounded-xl"></div>
          <div className="h-48 bg-gray-200 rounded-xl"></div>
        </div>
      </main>
    );
  }

  if (error || !contest) {
    return (
      <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow flex items-center justify-center">
        <div className="text-center p-8 bg-white rounded-xl shadow-md border border-gray-200">
          <span className="material-symbols-outlined text-red-550 text-5xl mb-2">error</span>
          <h3 className="text-lg font-bold text-brand-blue">Error Loading Contest</h3>
          <p className="text-sm text-text-muted mt-1">{error || 'Contest not found'}</p>
        </div>
      </main>
    );
  }

  return (
    <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow">
      <div className="max-w-[1280px] mx-auto flex flex-col gap-8">
        {/* Details Card */}
        <section className="bg-surface rounded-xl ambient-shadow p-8">
          <h2 className="text-headline-md font-headline-md text-text-main mb-6 pb-4 border-b border-gray-200 flex items-center gap-2">
            <span className="material-symbols-outlined text-text-muted">info</span> Contest Overview
            <span className={`ml-auto text-white text-label-md font-label-md px-3 py-1 rounded-full ${
              contest.status === 'ONGOING' ? 'bg-brand-green' : contest.status === 'UPCOMING' ? 'bg-primary' : 'bg-gray-400'
            }`}>
              {contest.status}
            </span>
          </h2>
          <div className="grid grid-cols-1 gap-8 mt-6 md:flex">
            <div className="md:col-span-1" style={{ flex: '0 0 70%', maxWidth: '70%' }}>
              <div className="mb-4">
                <h3 className="text-label-md font-label-md text-text-muted uppercase tracking-wider mb-2">Contest Title</h3>
                <p className="text-headline-sm font-headline-sm text-text-main font-bold italic">
                  {contest.title}
                </p>
              </div>
            </div>
            <div className="md:col-span-1" style={{ flex: '0 0 30%', maxWidth: '30%' }}>
              <h3 className="text-label-md font-label-md text-text-muted uppercase tracking-wider mb-2">Schedule</h3>
              <ul className="text-body-md font-body-md text-text-main space-y-1">
                <li><strong>Start:</strong> {formatDate(contest.startTime)}</li>
                <li><strong>End:</strong> {formatDate(contest.endTime)}</li>
                <li><strong>Duration:</strong> {contest.durations} Minutes</li>
              </ul>
            </div>
          </div>
        </section>

        {/* Description Card */}
        <section className="bg-surface rounded-xl ambient-shadow p-8">
          <h2 className="text-headline-md font-headline-md text-text-main mb-6 pb-4 border-b border-gray-200 flex items-center gap-2">
            <span className="material-symbols-outlined text-text-muted">description</span>Contest Description
          </h2>
          <div className="mt-6">
            <p className="text-body-md font-body-md text-text-main">
              {contest.description || 'No description provided.'}
            </p>
          </div>
        </section>

        {/* Scoring System Card */}
        <section className="bg-surface rounded-xl ambient-shadow p-8">
          <h2 className="text-headline-md font-headline-md text-text-main mb-6 pb-4 border-b border-gray-200 flex items-center gap-2">
            <span className="material-symbols-outlined text-text-muted">score</span>Scoring System
          </h2>
          <div className="mt-6">
            <div className="bg-surface-gray p-6 rounded-lg border border-gray-200">
              <h4 className="text-label-md font-label-md text-text-muted uppercase tracking-wider mb-2">Scoring Rule</h4>
              <p className="text-body-md font-body-md text-text-main font-bold flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-[20px]">emoji_events</span>
                <span>{contest.scoringRule}</span>
              </p>
            </div>
          </div>
        </section>

        {/* Rules & Prohibitions Card */}
        <section className="bg-surface rounded-xl ambient-shadow p-8">
          <h2 className="text-headline-md font-headline-md text-text-main mb-6 pb-4 border-b border-gray-200 flex items-center gap-2">
            <span className="material-symbols-outlined text-text-muted">gavel</span>Rules & Prohibitions
          </h2>
          <div className="mt-6">
            <p className="text-body-md font-body-md text-text-main text-text-muted italic">
              —
            </p>
          </div>
        </section>

        {/* Supported Languages Card */}
        <section className="bg-surface rounded-xl ambient-shadow p-8">
          <h2 className="text-headline-md font-headline-md text-text-main mb-6 pb-4 border-b border-gray-200 flex items-center gap-2">
            <span className="material-symbols-outlined text-text-muted">translate</span>Supported Languages
          </h2>
          <div className="mt-6">
            <p className="text-body-md font-body-md text-text-main text-text-muted italic">
              —
            </p>
          </div>
        </section>
      </div>
    </main>
  );
};
