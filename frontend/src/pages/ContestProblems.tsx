import React, { useState, useEffect } from 'react';
import { useParams, Link, useOutletContext } from 'react-router-dom';
import type { ContestOverviewData } from '../components/Layout';

interface ContestProblem {
  problemId: number;
  title: string;
  orderIndex: number;
  difficulty: string;
  totalSubmission: number;
  totalAccepted: number;
  status: 'SOLVED' | 'FAILED' | 'UNATTEMPTED';
}

export const ContestProblems: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const contestId = id || '1';

  const { contest, loading: contestLoading } = useOutletContext<{
    contest: ContestOverviewData | null;
    loading: boolean;
  }>();

  const [problems, setProblems] = useState<ContestProblem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!contest || !contest.isUserRegistered) {
      setLoading(false);
      return;
    }

    const fetchProblems = async () => {
      setLoading(true);
      try {
        const response = await fetch(`http://localhost:8080/nonstopcoding/contests/${contestId}/problems`, {
          credentials: 'include',
        });
        const data = await response.json();
        if (data && data.result) {
          setProblems(data.result);
          setError(null);
        } else {
          setError(data.message || 'Failed to fetch problems');
        }
      } catch (err) {
        console.error('Error fetching problems:', err);
        setError('Failed to fetch problems');
      } finally {
        setLoading(false);
      }
    };

    fetchProblems();
  }, [contest, contestId]);


  const getProblemCode = (orderIndex: number) => {
    return String.fromCharCode(65 + orderIndex);
  };

  if (contestLoading) {
    return (
      <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow animate-pulse">
        <div className="max-w-[1280px] mx-auto bg-white rounded-xl shadow-sm h-64"></div>
      </main>
    );
  }

  // Lock screen if not registered
  if (contest && !contest.isUserRegistered) {
    return (
      <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow flex items-center justify-center">
        <div className="max-w-md w-full bg-white/80 backdrop-blur-md rounded-2xl p-8 text-center shadow-xl border border-white/50 space-y-4">
          <div className="w-16 h-16 rounded-full bg-primary-light/40 text-primary flex items-center justify-center border border-primary/20 mx-auto">
            <span className="material-symbols-outlined text-4xl">lock</span>
          </div>
          <h3 className="font-display font-black text-xl text-brand-blue">Arena Locked</h3>
          <p className="font-body text-sm text-text-muted">
            You must register for this contest first to view and solve the problems. Use the registration panel on the sidebar.
          </p>
        </div>
      </main>
    );
  }

  return (
    <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow">
      <div className="max-w-[1280px] mx-auto">
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          
          <div className="p-6 border-b border-gray-200 bg-white flex justify-between items-center">
            <h1 className="text-2xl font-headline font-semibold text-text-main">Problems</h1>
          </div>

          {loading ? (
            <div className="p-12 text-center">
              <span className="material-symbols-outlined text-primary text-4xl animate-spin">sync</span>
            </div>
          ) : error ? (
            <div className="p-12 text-center text-red-500 font-bold">
              {error}
            </div>
          ) : problems.length === 0 ? (
            <div className="p-12 text-center text-text-muted">
              No problems assigned to this contest yet.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-surface-gray border-b border-gray-200 text-text-main font-label text-sm">
                    <th className="p-4 w-16 text-center">Status</th>
                    <th className="p-4 w-16">#</th>
                    <th className="p-4">Title</th>
                    <th className="p-4 w-32 text-right">Solved&nbsp;</th>
                  </tr>
                </thead>
                <tbody className="text-base font-body divide-y divide-gray-200">
                  {problems.map((cp) => (
                    <tr key={cp.problemId} className="hover:bg-surface-gray/50 transition-colors">
                      <td className="p-4 text-center">
                        {cp.status === 'SOLVED' && (
                          <span className="material-symbols-outlined text-brand-green icon-fill text-[20px] cursor-default select-none" title="Solved">
                            check_circle
                          </span>
                        )}
                        {cp.status === 'FAILED' && (
                          <span className="material-symbols-outlined text-red-500 icon-fill text-[20px] cursor-default select-none" title="Failed">
                            cancel
                          </span>
                        )}
                      </td>
                      <td className="p-4 font-semibold text-text-main">
                        {getProblemCode(cp.orderIndex)}
                      </td>
                      <td className="p-4">
                        <Link className="text-primary hover:underline font-bold" to={`/contests/${contestId}/problems/${cp.problemId}`}>
                          {cp.title}
                        </Link>
                      </td>
                      <td className="p-4 text-right text-text-muted">
                        {cp.totalAccepted.toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </main>
  );
};
