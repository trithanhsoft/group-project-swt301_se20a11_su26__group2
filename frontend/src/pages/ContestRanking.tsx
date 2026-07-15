import React, { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import type { ContestOverviewData } from '../components/Layout';
import { ContestSidebar } from '../components/ContestSidebar';

interface SubmissionDetail {
  time?: string;
  penalty: number;
  status: 'first_solve' | 'accepted' | 'failed' | 'unattempted';
}

interface Team {
  rank: number;
  name: string;
  displayName?: string;
  affiliation: string;
  solved: number;
  totalAttempts: number;
  totalTime: string;
  totalPenalty: number;
  submissions: Record<string, SubmissionDetail>;
}



interface ContestProblem {
  problemId: number;
  title: string;
  orderIndex: number;
  difficulty: string;
  totalSubmission: number;
  totalAccepted: number;
  status: 'SOLVED' | 'FAILED' | 'UNATTEMPTED';
}

// Helper to convert time "H:MM:SS" into minutes
const timeToMinutes = (timeStr?: string): number => {
  if (!timeStr) return 0;
  const parts = timeStr.split(':').map(Number);
  const hrs = parts[0] || 0;
  const mins = parts[1] || 0;
  const secs = parts[2] || 0;
  return hrs * 60 + mins + secs / 60;
};



// 10 Distinct Premium Colors for the Top 10 Teams
const TEAM_COLORS = [
  '#3b82f6', // Rank 1: Blue
  '#10b981', // Rank 2: Emerald Green
  '#f59e0b', // Rank 3: Amber Orange
  '#8b5cf6', // Rank 4: Purple
  '#ec4899', // Rank 5: Pink
  '#06b6d4', // Rank 6: Cyan
  '#ef4444', // Rank 7: Rose Red
  '#14b8a6', // Rank 8: Teal
  '#6366f1', // Rank 9: Indigo
  '#f97316'  // Rank 10: Orange
];

const W = 720; // width of inner chart
const H = 260; // height of inner chart
const paddingLeft = 45;
const paddingTop = 20;

// Convert minutes and solves to SVG viewbox coordinates
const getSvgCoords = (minutes: number, solves: number, duration: number, problemCount: number) => {
  const maxMins = duration || 240;
  const maxProblems = problemCount || 10;
  const svgX = paddingLeft + (minutes / maxMins) * W;
  const svgY = (paddingTop + H) - (solves / maxProblems) * H;
  return { x: svgX, y: svgY };
};

// Generate the Step-After SVG Path string for a team over the contest duration
const getStepPathString = (team: Team, duration: number, problemCount: number) => {
  const maxMins = duration || 240;
  const solves: { time: number; problem: string }[] = [];
  Object.keys(team.submissions).forEach((key) => {
    const sub = team.submissions[key];
    if (sub.status === 'accepted' || sub.status === 'first_solve') {
      solves.push({
        time: timeToMinutes(sub.time),
        problem: key
      });
    }
  });
  solves.sort((a, b) => a.time - b.time);

  const start = getSvgCoords(0, 0, maxMins, problemCount);
  if (solves.length === 0) {
    const end = getSvgCoords(maxMins, 0, maxMins, problemCount);
    return {
      pathStr: `M ${start.x} ${start.y} L ${end.x} ${end.y}`,
      solves: []
    };
  }

  let pathStr = `M ${start.x} ${start.y}`;
  solves.forEach((solve, index) => {
    const p1 = getSvgCoords(solve.time, index, maxMins, problemCount);
    const p2 = getSvgCoords(solve.time, index + 1, maxMins, problemCount);
    pathStr += ` L ${p1.x} ${p1.y} L ${p2.x} ${p2.y}`;
  });

  const end = getSvgCoords(maxMins, solves.length, maxMins, problemCount);
  pathStr += ` L ${end.x} ${end.y}`;
  return { pathStr, solves };
};

// Convert minutes back to "Hh Mm" format for formatting tooltips
const formatMinutes = (m: number): string => {
  const hrs = Math.floor(m / 60);
  const mins = Math.floor(m % 60);
  return `${hrs}h ${mins}m`;
};

export const ContestRanking: React.FC = () => {
  const { contest, loading, timeLeft, timerLabel } = useOutletContext<{
    contest: ContestOverviewData | null;
    loading: boolean;
    timeLeft: string;
    timerLabel: string;
  }>();
  const [hoveredTeam, setHoveredTeam] = useState<string | null>(null);
  const [activeTooltip, setActiveTooltip] = useState<{
    x: number;
    y: number;
    teamName: string;
    solvedCount: number;
    timeStr: string;
    problem: string;
  } | null>(null);

  const [visibleTeams, setVisibleTeams] = useState<Record<string, boolean>>({});

  const [teams, setTeams] = useState<Team[]>([]);
  const [problems, setProblems] = useState<ContestProblem[]>([]);
  const [loadingData, setLoadingData] = useState(true);
  const [errorData, setErrorData] = useState<string | null>(null);

  useEffect(() => {
    if (!contest || !contest.isUserRegistered) {
      setLoadingData(false);
      return;
    }

    let eventSource: EventSource | null = null;

    const fetchData = async () => {
      setLoadingData(true);
      setErrorData(null);
      try {
        const [sbRes, probsRes] = await Promise.all([
          fetch(`http://localhost:8080/nonstopcoding/api/v1/contests/${contest.id}/scoreboard`, { credentials: 'include' }),
          fetch(`http://localhost:8080/nonstopcoding/contests/${contest.id}/problems`, { credentials: 'include' })
        ]);

        if (!sbRes.ok || !probsRes.ok) {
          throw new Error('Failed to fetch contest ranking data');
        }

        const sbData = await sbRes.json();
        const probsData = await probsRes.json();

        if (sbData && sbData.result && probsData && probsData.result) {
          setTeams(sbData.result.rows || []);
          setProblems(probsData.result);
        } else {
          throw new Error('Invalid response structure from backend');
        }

        // Đăng ký lắng nghe realtime qua Server-Sent Events (SSE)
        eventSource = new EventSource(`http://localhost:8080/nonstopcoding/api/v1/contests/${contest.id}/scoreboard/stream`, { withCredentials: true });
        
        eventSource.addEventListener('scoreboard-update', (event: any) => {
          try {
            const parsed = JSON.parse(event.data);
            if (parsed && parsed.rows) {
              setTeams(parsed.rows);
            }
          } catch (err) {
            console.error('Error parsing SSE scoreboard update:', err);
          }
        });

        eventSource.onerror = (err) => {
          console.error('EventSource error:', err);
        };

      } catch (err: any) {
        console.error('Error fetching ranking data:', err);
        setErrorData(err.message || 'Failed to load rankings');
      } finally {
        setLoadingData(false);
      }
    };

    void fetchData();

    return () => {
      if (eventSource) {
        eventSource.close();
      }
    };
  }, [contest]);

  useEffect(() => {
    if (teams.length > 0) {
      setVisibleTeams((prev) => {
        const hasVisible = Object.values(prev).some(v => v);
        if (hasVisible) return prev;
        const next: Record<string, boolean> = {};
        teams.slice(0, 10).forEach((t) => {
          next[t.name] = true;
        });
        return next;
      });
    }
  }, [teams]);


  // Scoreboard individual submission cell styling mapper
  const renderSubmissionCell = (sub: SubmissionDetail, key: string) => {
    if (!sub || sub.status === 'unattempted') {
      return <td key={key} className="p-1 text-center border border-white text-xs bg-gray-50/50"></td>;
    }

    if (sub.status === 'failed') {
      return (
        <td key={key} className="p-1 text-center border border-white text-xs bg-primary text-white font-medium">
          --
          <br />
          <span className="text-[10px] font-normal text-white/80">(-{sub.penalty})</span>
        </td>
      );
    }

    const penaltyStr = sub.penalty > 0 ? `(-${sub.penalty})` : '';

    if (sub.status === 'first_solve') {
      return (
        <td key={key} className="p-1 text-center border border-white text-xs bg-brand-blue text-white font-medium">
          {sub.time}
          <br />
          <span className="text-[10px] font-normal text-white/80">{penaltyStr}</span>
        </td>
      );
    }

    // Accepted
    return (
      <td key={key} className="p-1 text-center border border-white text-xs bg-brand-green text-white font-medium">
        {sub.time}
        <br />
        <span className="text-[10px] font-normal text-white/80">{penaltyStr}</span>
      </td>
    );
  };

  if (loading || loadingData) {
    return (
      <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow animate-pulse">
        <div className="max-w-[1280px] mx-auto bg-white rounded-xl shadow-sm h-64 flex items-center justify-center font-semibold text-text-muted">
          Loading rankings...
        </div>
      </main>
    );
  }

  if (errorData) {
    return (
      <main className="w-full px-4 sm:px-8 py-8 md:py-12 bg-surface-gray flex-grow flex items-center justify-center">
        <div className="max-w-md w-full bg-white rounded-xl p-8 text-center shadow-sm border border-red-200">
          <h3 className="font-display font-black text-xl text-red-650">Error Loading Rankings</h3>
          <p className="font-body text-sm text-text-muted mt-2">{errorData}</p>
        </div>
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
            You must register for this contest first to view the rankings. Use the registration panel on the sidebar.
          </p>
        </div>
      </main>
    );
  }

  const contestDuration = contest?.durations || 240;
  const problemCount = problems.length || 10;

  return (
    <>
      <main className="w-full pl-4 sm:pl-8 py-8 md:py-12 pr-4 sm:pr-8 bg-surface-gray flex-grow min-w-0">
        <div className="w-full flex flex-col gap-4">
          
          <div className="flex flex-col md:flex-row w-full gap-0 items-stretch">
            {/* Top Section: Interactive Custom SVG Chart & Legends */}
            <div className="flex-grow min-w-0">
              <section className="bg-white rounded-xl p-6 shadow-sm border border-gray-200 flex flex-col gap-4 relative overflow-hidden group">
            <div className="absolute -top-24 -right-24 w-64 h-64 bg-primary/5 rounded-full blur-3xl pointer-events-none"></div>
            
            <div className="flex flex-col md:flex-row md:items-center justify-between pb-2 border-b border-gray-150">
              <div>
                <h2 className="font-headline text-headline-md text-text-main relative z-10 flex items-center gap-2 font-bold text-xl">
                  <span className="material-symbols-outlined text-primary">monitoring</span> Top 10 Teams Progress
                </h2>
                <p className="text-body-sm text-text-muted mt-1">Real-time stepwise progression of problems solved over the contest duration.</p>
              </div>
              <div className="text-xs text-text-muted mt-2 md:mt-0 italic flex items-center gap-1.5">
                <span className="w-2.5 h-2.5 rounded-full bg-primary inline-block animate-ping"></span> Live scoreboard feed
              </div>
            </div>

            {teams.length === 0 ? (
              <div className="p-12 text-center text-text-muted font-medium bg-slate-50 rounded-lg border border-dashed border-gray-300">
                <span className="material-symbols-outlined text-4xl text-gray-400 mb-2">emoji_events</span>
                <p>No submissions recorded for this contest yet.</p>
                <p className="text-xs text-text-muted/80 mt-1">Be the first to submit a solution!</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 items-stretch mt-2">
                
                {/* Interactive Vector Step Chart (Col-span 3) */}
                <div className="lg:col-span-3 bg-white rounded-lg border border-gray-150 p-4 relative flex items-center justify-center">
                  <svg className="w-full h-auto max-w-[1100px]" viewBox="0 0 800 310" width="100%">
                    {/* Vertical Hour Grid Lines */}
                    {Array.from({ length: Math.ceil(contestDuration / 60) + 1 }).map((_, i) => {
                      const minutes = i * 60;
                      if (minutes > contestDuration) return null;
                      const coordsStart = getSvgCoords(minutes, 0, contestDuration, problemCount);
                      const coordsEnd = getSvgCoords(minutes, problemCount, contestDuration, problemCount);
                      return (
                        <g key={i}>
                          <line
                            x1={coordsStart.x}
                            y1={coordsStart.y}
                            x2={coordsEnd.x}
                            y2={coordsEnd.y}
                            stroke="#e2e8f0"
                            strokeWidth={1}
                            strokeDasharray="4 4"
                          />
                          <text
                            x={coordsStart.x}
                            y={coordsStart.y + 16}
                            textAnchor="middle"
                            className="font-mono text-[10px] fill-text-muted"
                          >
                            {i}h
                          </text>
                        </g>
                      );
                    })}

                    {/* Horizontal Solved Count Grid Lines */}
                    {Array.from({ length: problemCount + 1 }).map((_, solved) => {
                      const step = problemCount > 8 ? 2 : 1;
                      if (solved % step !== 0 && solved !== problemCount) return null;
                      const coordsStart = getSvgCoords(0, solved, contestDuration, problemCount);
                      const coordsEnd = getSvgCoords(contestDuration, solved, contestDuration, problemCount);
                      return (
                        <g key={solved}>
                          <line
                            x1={coordsStart.x}
                            y1={coordsStart.y}
                            x2={coordsEnd.x}
                            y2={coordsEnd.y}
                            stroke="#e2e8f0"
                            strokeWidth={1}
                          />
                          <text
                            x={coordsStart.x - 8}
                            y={coordsStart.y + 4}
                            textAnchor="end"
                            className="font-mono text-[10px] fill-text-muted"
                          >
                            {solved}
                          </text>
                        </g>
                      );
                    })}

                    {/* Draw team stepwise progression lines */}
                    {teams.slice(0, 10).map((team, idx) => {
                      if (!visibleTeams[team.name]) return null;
                      const color = TEAM_COLORS[idx % TEAM_COLORS.length];
                      const isHovered = hoveredTeam === team.name;
                      const { pathStr, solves } = getStepPathString(team, contestDuration, problemCount);

                      return (
                        <g key={team.name}>
                          {/* Smooth vector stroke */}
                          <path
                            d={pathStr}
                            fill="none"
                            stroke={color}
                            strokeWidth={isHovered ? 3.5 : 1.5}
                            strokeOpacity={hoveredTeam === null ? 0.6 : isHovered ? 1.0 : 0.15}
                            className="transition-all duration-300"
                          />

                          {/* Interactive solve dot circles */}
                          {solves.map((solve, sIdx) => {
                            const coords = getSvgCoords(solve.time, sIdx + 1, contestDuration, problemCount);
                            return (
                              <circle
                                key={sIdx}
                                cx={coords.x}
                                cy={coords.y}
                                r={isHovered ? 5.5 : 3.5}
                                fill={color}
                                stroke="#ffffff"
                                strokeWidth={1.5}
                                opacity={hoveredTeam === null ? 0.9 : isHovered ? 1.0 : 0.15}
                                className="cursor-pointer transition-all duration-300"
                                onMouseEnter={() => {
                                  setHoveredTeam(team.name);
                                  setActiveTooltip({
                                    x: coords.x,
                                    y: coords.y,
                                    teamName: team.name,
                                    solvedCount: sIdx + 1,
                                    timeStr: formatMinutes(solve.time),
                                    problem: solve.problem
                                  });
                                }}
                                onMouseLeave={() => {
                                  setHoveredTeam(null);
                                  setActiveTooltip(null);
                                }}
                              />
                            );
                          })}
                        </g>
                      );
                    })}

                    {/* HTML-styled SVG Tooltip Overlay */}
                    {activeTooltip && (
                      <foreignObject
                        x={activeTooltip.x - 100}
                        y={activeTooltip.y - 75}
                        width={200}
                        height={65}
                        pointerEvents="none"
                      >
                        <div className="bg-slate-800 text-white px-2.5 py-1.5 rounded-lg shadow-md border border-slate-700 text-xs font-semibold flex flex-col gap-0.5">
                          <span className="font-bold text-[11px] truncate" style={{ color: TEAM_COLORS[teams.slice(0, 10).findIndex(t => t.name === activeTooltip.teamName) % TEAM_COLORS.length] || '#3b82f6' }}>
                            {activeTooltip.teamName}
                          </span>
                          <div className="flex justify-between items-center text-[10px] text-slate-300 mt-0.5">
                            <span>Solved: <strong className="text-white text-xs">{activeTooltip.solvedCount}</strong></span>
                            <span>Problem: <strong className="text-white">{activeTooltip.problem}</strong></span>
                          </div>
                          <span className="text-[9px] text-slate-400">At time: {activeTooltip.timeStr}</span>
                        </div>
                      </foreignObject>
                    )}
                  </svg>
                </div>

                {/* Side Interactive Legends (Col-span 1) */}
                <div className="lg:col-span-1 border border-gray-150 rounded-lg p-4 flex flex-col gap-3 h-[382px] bg-slate-50/50">
                  <div className="flex flex-col gap-1.5 pb-2 border-b border-gray-200 shrink-0">
                    <h4 className="font-label text-xs text-text-muted uppercase tracking-wider font-bold">Chart Visibility</h4>
                    <div className="flex gap-1.5 text-[10px] font-bold mt-1">
                      <button
                        onClick={() => {
                          const next: Record<string, boolean> = {};
                          teams.slice(0, 10).forEach(t => next[t.name] = true);
                          setVisibleTeams(next);
                        }}
                        className="px-2 py-0.5 rounded bg-gray-200 hover:bg-gray-300 text-text-main transition-colors font-medium text-[10px]"
                      >
                        All
                      </button>
                      <button
                        onClick={() => {
                          const next: Record<string, boolean> = {};
                          teams.slice(0, 10).forEach((t, i) => next[t.name] = i < 3);
                          setVisibleTeams(next);
                        }}
                        className="px-2 py-0.5 rounded bg-gray-200 hover:bg-gray-300 text-text-main transition-colors font-medium text-[10px]"
                      >
                        Top 3
                      </button>
                      <button
                        onClick={() => {
                          const next: Record<string, boolean> = {};
                          teams.slice(0, 10).forEach(t => next[t.name] = false);
                          setVisibleTeams(next);
                        }}
                        className="px-2 py-0.5 rounded bg-gray-200 hover:bg-gray-300 text-text-main transition-colors font-medium text-[10px]"
                      >
                        Clear
                      </button>
                    </div>
                  </div>

                  <div className="flex flex-col gap-1.5 overflow-y-auto overflow-x-hidden flex-grow">
                    {teams.slice(0, 10).map((team, idx) => {
                      const color = TEAM_COLORS[idx % TEAM_COLORS.length];
                      const isHovered = hoveredTeam === team.name;
                      const isVisible = visibleTeams[team.name];

                      return (
                        <div
                          key={team.name}
                          onMouseEnter={() => isVisible && setHoveredTeam(team.name)}
                          onMouseLeave={() => setHoveredTeam(null)}
                          onClick={() => {
                            setVisibleTeams(prev => ({
                              ...prev,
                              [team.name]: !prev[team.name]
                            }));
                          }}
                          className={`flex items-center gap-2 p-2 rounded-lg cursor-pointer transition-all ${
                            isHovered && isVisible ? 'bg-white shadow-sm border border-gray-200 translate-x-1 scale-[1.02]' : 'border border-transparent hover:bg-gray-100/70'
                          } ${!isVisible ? 'opacity-50 hover:opacity-80' : ''}`}
                        >
                          {/* Checkbox indicator */}
                          {isVisible ? (
                            <svg className="w-4 h-4 text-primary shrink-0" fill="currentColor" viewBox="0 0 20 20">
                              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                            </svg>
                          ) : (
                            <div className="w-4 h-4 border-2 border-gray-300 rounded shrink-0 bg-white" />
                          )}

                          <span className="w-3 h-3 rounded-full shrink-0 border border-white shadow-sm" style={{ backgroundColor: color }} />
                          <div className="flex-grow min-w-0">
                            <p className="text-xs font-semibold text-text-main truncate leading-none">{team.name}</p>
                            <span className="text-[10px] text-text-muted leading-none">{team.affiliation}</span>
                          </div>
                          <span className="text-[11px] font-bold text-primary shrink-0 bg-primary-light/50 px-2 py-0.5 rounded-full">
                            {team.solved} AC
                          </span>
                        </div>
                      );
                    })}
                  </div>
                </div>

              </div>
            )}
          </section>
        </div>
      </div>

      {/* Bottom Section: Detailed Scoreboard */}
      <div className="w-full mt-4">
        <section className="bg-white rounded-xl shadow-[0_4px_20px_rgba(18,40,76,0.05)] border border-gray-200 overflow-hidden flex flex-col w-full">
            <div className="p-4 border-b border-gray-200 bg-white flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
              <div>
                <h3 className="font-headline text-headline-md text-text-main font-bold text-lg">Detailed Scoreboard</h3>
                <p className="text-xs text-text-muted mt-1">
                  Complete standings of all {teams.length} participants for the contest problems.
                </p>
              </div>
              <div className="flex items-center gap-4 text-sm flex-wrap bg-slate-50 p-2.5 rounded-lg border border-slate-100">
                <div className="flex items-center gap-1.5">
                  <span className="w-3.5 h-3.5 rounded bg-brand-blue block"></span> 
                  <span className="text-text-muted text-[11px] font-medium">First Solve</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="w-3.5 h-3.5 rounded bg-brand-green block"></span> 
                  <span className="text-text-muted text-[11px] font-medium">Accepted</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="w-3.5 h-3.5 rounded bg-primary block"></span> 
                  <span className="text-text-muted text-[11px] font-medium">Wrong/Failed</span>
                </div>
              </div>
            </div>
            <div className="w-full overflow-hidden">
              {problems.length === 0 ? (
                <div className="p-8 text-center text-text-muted">No problems assigned to this contest.</div>
              ) : (
                <table className="w-full table-fixed text-left border-collapse">
                  <thead>
                    <tr className="bg-surface-gray text-text-main font-label text-[10px] sm:text-xs border-b-2 border-gray-200 uppercase tracking-wider text-center">
                      <th className="p-1 sm:p-2 font-bold w-8 sm:w-12 text-center border-r border-gray-200">#</th>
                      <th className="p-1 sm:p-2 font-bold w-1/4 text-left border-r border-gray-200 truncate">User</th>
                      <th className="p-1 sm:p-2 font-bold w-12 sm:w-16 whitespace-nowrap border-r border-gray-200 text-[9px] sm:text-[11px]">AC/T</th>
                      <th className="p-1 sm:p-2 font-bold w-10 sm:w-14 border-r border-gray-200 text-[9px] sm:text-[11px]">Pen</th>
                      {problems.map((p) => (
                        <th key={p.problemId} className="p-1 sm:p-2 font-semibold border-r border-gray-200 truncate" title={p.title}>
                          {String.fromCharCode(65 + p.orderIndex)}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="text-[10px] sm:text-sm font-body text-text-main divide-y divide-gray-200">
                    {teams.length === 0 ? (
                      <tr>
                        <td colSpan={4 + problems.length} className="p-8 text-center text-text-muted">
                          No submission records yet.
                        </td>
                      </tr>
                    ) : (
                      teams.map((team) => (
                        <tr key={team.name} className="hover:bg-surface-gray transition-colors group">
                          <td className="p-1 sm:p-2 text-center font-black text-primary border-r border-gray-200">
                            {team.rank}
                          </td>
                          <td className="p-1 sm:p-2 border-r border-gray-200 truncate">
                            <div className="flex flex-col truncate">
                              <span className="font-semibold text-text-main truncate">{team.displayName || team.name}</span>
                              <span className="text-[9px] sm:text-xs text-text-muted truncate">{team.affiliation}</span>
                            </div>
                          </td>
                          <td className="p-1 sm:p-2 text-center font-bold whitespace-nowrap border-r border-gray-200 text-[10px] sm:text-xs">
                            {team.solved} / {team.totalAttempts}
                          </td>
                          <td className="p-1 sm:p-2 text-center text-text-muted font-mono border-r border-gray-200 text-[10px] sm:text-xs">
                             {team.totalPenalty}
                           </td>
                          {problems.map((p) => {
                            const label = String.fromCharCode(65 + p.orderIndex);
                            return renderSubmissionCell(team.submissions[label], label);
                          })}
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              )}
            </div>
          </section>
        </div>
        </div>
      </main>
    </>
  );
};
