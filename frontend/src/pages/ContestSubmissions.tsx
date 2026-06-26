import React, { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import type { ContestOverviewData } from '../components/Layout';

interface Submission {
  id: number;
  submittedAt: string;
  username: string;
  problemLabel: string;
  problemId: number;
  problemTitle: string;
  status: string;
  lang: string;
  runtime: string;
  memory: string;
  statusClass: string;
}

export const ContestSubmissions: React.FC = () => {
  const { contest, loading } = useOutletContext<{
    contest: ContestOverviewData | null;
    loading: boolean;
  }>();

  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [loadingSubmissions, setLoadingSubmissions] = useState(true);
  const [errorSubmissions, setErrorSubmissions] = useState<string | null>(null);

  useEffect(() => {
    if (!contest) return;

    const fetchSubmissions = async () => {
      setLoadingSubmissions(true);
      setErrorSubmissions(null);
      try {
        const response = await fetch(`http://localhost:8080/nonstopcoding/contests/${contest.id}/submissions`, {
          credentials: 'include',
        });
        const data = await response.json();
        if (data && data.result) {
          setSubmissions(data.result);
        } else {
          setErrorSubmissions(data.message || 'Failed to fetch submissions');
        }
      } catch (err) {
        console.error('Error fetching contest submissions:', err);
        setErrorSubmissions('Failed to fetch submissions');
      } finally {
        setLoadingSubmissions(false);
      }
    };

    void fetchSubmissions();
  }, [contest]);

  if (loading) {
    return (
      <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow animate-pulse">
        <div className="max-w-[1280px] mx-auto bg-white rounded-xl shadow-sm h-64"></div>
      </main>
    );
  }

  if (contest && !contest.isUserRegistered) {
    return (
      <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow flex items-center justify-center">
        <div className="max-w-md w-full bg-white/80 backdrop-blur-md rounded-2xl p-8 text-center shadow-xl border border-white/50 space-y-4">
          <div className="w-16 h-16 rounded-full bg-primary-light/40 text-primary flex items-center justify-center border border-primary/20 mx-auto">
            <span className="material-symbols-outlined text-4xl">lock</span>
          </div>
          <h3 className="font-display font-black text-xl text-brand-blue">Arena Locked</h3>
          <p className="font-body text-sm text-text-muted">
            You must register for this contest first to view the submissions. Use the registration panel on the right sidebar.
          </p>
        </div>
      </main>
    );
  }

  return (
    <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow">
      <div className="max-w-[1280px] mx-auto">
        <h1 className="font-headline text-headline-lg text-text-main mb-8">Submissions</h1>
        
        {/* Table Container */}
        <div className="bg-surface rounded-xl shadow-sm border border-gray-200 overflow-hidden bg-white">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-surface-gray text-text-main font-label text-label-md border-b border-gray-200 uppercase tracking-wider text-xs">
                  <th className="px-6 py-4 whitespace-nowrap text-text-main font-semibold">When</th>
                  <th className="px-6 py-4 whitespace-nowrap text-text-main font-semibold">User</th>
                  <th className="px-6 py-4 whitespace-nowrap text-center text-text-main font-semibold">Problem</th>
                  <th className="px-6 py-4 whitespace-nowrap text-text-main font-semibold">Status</th>
                  <th className="px-6 py-4 whitespace-nowrap text-right text-text-main font-semibold">Time</th>
                  <th className="px-6 py-4 whitespace-nowrap text-right text-text-main font-semibold">Memory</th>
                  <th className="px-6 py-4 whitespace-nowrap text-text-main font-semibold">Language</th>
                </tr>
              </thead>
              <tbody className="font-body text-body-md text-text-main divide-y divide-gray-200">
                {loadingSubmissions ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-8 text-center text-text-muted">
                      Loading submissions...
                    </td>
                  </tr>
                ) : errorSubmissions ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-8 text-center text-red-500 font-semibold">
                      {errorSubmissions}
                    </td>
                  </tr>
                ) : submissions.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-8 text-center text-text-muted">
                      No submissions yet.
                    </td>
                  </tr>
                ) : (
                  submissions.map((sub) => (
                    <tr key={sub.id} className="hover:bg-surface-gray/50 transition-colors">
                      <td className="px-6 py-4 text-text-muted text-sm whitespace-nowrap">{sub.submittedAt}</td>
                      <td className="px-6 py-4 font-medium text-text-main">@{sub.username}</td>
                      <td className="px-6 py-4 text-center">
                        <span className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-surface-gray text-text-main font-semibold">
                          {sub.problemLabel}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full font-label text-sm font-semibold ${
                          sub.status === 'Accepted'
                            ? 'bg-brand-green/10 text-brand-green'
                            : 'bg-primary-light text-primary'
                        }`}>
                          <span className="material-symbols-outlined text-[18px]">
                            {sub.status === 'Accepted' ? 'check_circle' : 'cancel'}
                          </span>
                          {sub.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right text-text-muted">{sub.runtime}</td>
                      <td className="px-6 py-4 text-right text-text-muted">{sub.memory}</td>
                      <td className="px-6 py-4">
                        <span className="inline-flex items-center px-2 py-1 rounded-md bg-surface-gray text-text-muted text-xs font-medium">
                          {sub.lang}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Submissions count summary */}
          <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between bg-white">
            <span className="text-sm text-text-muted">
              Showing {submissions.length} submissions
            </span>
          </div>
        </div>
      </div>
    </main>
  );
};
