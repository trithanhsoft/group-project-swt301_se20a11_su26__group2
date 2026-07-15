import React, { useState, useEffect } from 'react';
import { Link, useParams, useOutletContext } from 'react-router-dom';
import { problemService } from '../services/problemService';
import { useApp } from '../context/AppContext';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { CodeEditor } from '../components/CodeEditor';
import type { ContestOverviewData } from '../components/Layout';

export const ContestProblemSolve: React.FC = () => {
  const { user } = useApp();
  const { id, problemId } = useParams<{ id: string; problemId: string }>();
  const contestId = id || '';
  const { contest } = useOutletContext<{ contest: ContestOverviewData | null }>();

  const [activeTab, setActiveTab] = useState<'description' | 'submissions'>(() => {
    const savedTab = sessionStorage.getItem('contestSolveProblemActiveTab');
    const savedId = sessionStorage.getItem('contestSolveProblemActiveId');
    if (savedId === problemId && savedTab && ['description', 'submissions'].includes(savedTab)) {
      return (savedTab as any) || 'description';
    }
    return 'description';
  });

  const [testcasesLogs, setTestcasesLogs] = useState<any[]>([]);
  const [overallResult, setOverallResult] = useState<any>(null);
  const [maintenanceError, setMaintenanceError] = useState<boolean>(false);
  const [expandedTestcases, setExpandedTestcases] = useState<{[key: number]: boolean}>({});
  const [copiedInput, setCopiedInput] = useState<boolean>(false);
  const [copiedOutput, setCopiedOutput] = useState<boolean>(false);

  const toggleTestcaseDetails = (testcaseId: number) => {
    setExpandedTestcases(prev => ({ ...prev, [testcaseId]: !prev[testcaseId] }));
  };

  const copyToClipboard = (text: string | undefined | null, type: 'input' | 'output') => {
    if (!text) return;
    const cleanText = formatPreText(text);
    navigator.clipboard.writeText(cleanText).then(() => {
      if (type === 'input') {
        setCopiedInput(true);
        setTimeout(() => setCopiedInput(false), 2000);
      } else {
        setCopiedOutput(true);
        setTimeout(() => setCopiedOutput(false), 2000);
      }
    });
  };

  useEffect(() => {
    if (problemId) {
      sessionStorage.setItem('contestSolveProblemActiveId', problemId);
      sessionStorage.setItem('contestSolveProblemActiveTab', activeTab);
    }
  }, [problemId, activeTab]);

  const [leftWidth, setLeftWidth] = useState<number>(50);
  const [isResizing, setIsResizing] = useState<boolean>(false);
  const [selectedLangId, setSelectedLangId] = useState<number>(62); // Java default

  const SUPPORTED_LANGUAGES = [
    {"id":50,"name":"C (GCC 9.2.0)"},
    {"id":54,"name":"C++ (GCC 9.2.0)"},
    {"id":62,"name":"Java (OpenJDK 13.0.1)"},
    {"id":71,"name":"Python (3.8.1)"},
    {"id":51,"name":"C# (Mono 6.6.0.161)"}
  ];

  const getTemplateForLang = (langId: number, templates: {[key: string]: string} | undefined) => {
    if (!templates) return '';
    const lang = SUPPORTED_LANGUAGES.find(l => l.id === langId);
    if (!lang) return '';
    const name = lang.name;
    if (name.includes('Java (')) return templates['Java'] || '';
    if (name.includes('Python')) return templates['Python 3'] || templates['Python'] || '';
    if (name.includes('C++')) return templates['C++'] || '';
    if (name.includes('C (')) return templates['C'] || '';
    if (name.includes('JavaScript') || name.includes('TypeScript')) return templates['JavaScript'] || '';
    if (name.includes('C#')) return templates['C#'] || '';
    const baseName = name.split(' ')[0];
    return templates[baseName] || '';
  };

  const [problem, setProblem] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [loadedTabs, setLoadedTabs] = useState<{[key: string]: boolean}>({});

  // A mapping from language ID to its standard string identifier used by Monaco and the state dictionary
  const LANGUAGE_KEYS: Record<number, string> = {
    50: 'c',
    54: 'cpp',
    62: 'java',
    71: 'python',
    51: 'csharp'
  };

  const [codeByLang, setCodeByLang] = useState<Record<string, string>>({
    c: '',
    cpp: '',
    java: '',
    python: '',
    csharp: ''
  });

  useEffect(() => {
    if (!contestId || !problemId) return;
    setLoading(true);
    setLoadedTabs({});
    problemService.fetchContestProblemDetail(contestId, problemId)
      .then(data => {
        setProblem(data);
        const actualTemplates = data.templates;
        
        // Prepare boilerplate code for all supported languages
        const initialCodeByLang: Record<string, string> = {
          c: '',
          cpp: '',
          java: '',
          python: '',
          csharp: ''
        };
        
        if (actualTemplates) {
          SUPPORTED_LANGUAGES.forEach(lang => {
            const langKey = LANGUAGE_KEYS[lang.id];
            if (langKey) {
              initialCodeByLang[langKey] = getTemplateForLang(lang.id, actualTemplates);
            }
          });
        }
        
        const defaultLangId = data.language_id || 62; // Java default or last submission lang
        setSelectedLangId(defaultLangId);
        
        const defaultLangKey = LANGUAGE_KEYS[defaultLangId];
        if (data.source_code && defaultLangKey) {
          initialCodeByLang[defaultLangKey] = data.source_code;
        }
        
        setCodeByLang(initialCodeByLang);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, [contestId, problemId]);

  const [submissions, setSubmissions] = useState<any[]>([]);

  useEffect(() => {
    if (!contestId || activeTab !== 'submissions' || loadedTabs['submissions']) return;
    problemService.fetchContestSubmissions(contestId)
      .then(data => {
        setSubmissions(data);
        setLoadedTabs(prev => ({ ...prev, submissions: true }));
      })
      .catch(err => {
        console.error("Failed to load submissions:", err);
      });
  }, [contestId, activeTab]);

  const handleLangChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newLangId = Number(e.target.value);
    setSelectedLangId(newLangId);
  };

  const handleCodeChange = (newCode: string | undefined) => {
    const langKey = LANGUAGE_KEYS[selectedLangId];
    if (langKey && newCode !== undefined) {
      setCodeByLang(prev => ({
        ...prev,
        [langKey]: newCode
      }));
    }
  };

  const handleResetCode = () => {
    if (problem && problem.templates) {
      const defaultCode = getTemplateForLang(selectedLangId, problem.templates);
      const langKey = LANGUAGE_KEYS[selectedLangId];
      if (langKey) {
        setCodeByLang(prev => ({
          ...prev,
          [langKey]: defaultCode
        }));
      }
    }
  };

  const startResizing = (e: React.MouseEvent) => {
    setIsResizing(true);
    e.preventDefault();
  };

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!isResizing) return;
      const container = document.getElementById('split-container');
      if (!container) return;
      const containerRect = container.getBoundingClientRect();
      let newLeftWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100;
      if (newLeftWidth < 20) newLeftWidth = 20;
      if (newLeftWidth > 80) newLeftWidth = 80;
      setLeftWidth(newLeftWidth);
    };

    const handleMouseUp = () => {
      setIsResizing(false);
    };

    if (isResizing) {
      window.addEventListener('mousemove', handleMouseMove);
      window.addEventListener('mouseup', handleMouseUp);
    }

    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
    };
  }, [isResizing]);

  const formatHtmlText = (text: string | undefined | null) => {
    if (!text) return '';
    return text.replace(/\\n/g, '<br />');
  };

  const formatPreText = (text: string | undefined | null) => {
    if (!text) return '';
    let cleaned = text.replace(/\\n/g, '\n');
    cleaned = cleaned.replace(/\[([a-zA-Z0-9,\s\-.]+)\]/g, '$1');
    return cleaned;
  };

  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  
  const fetchSubmissionsAfterDelay = () => {
    setTimeout(() => {
      if (contestId) {
        problemService.fetchContestSubmissions(contestId).then(data => {
          setSubmissions(data);
          setLoadedTabs(prev => ({ ...prev, submissions: true }));
        }).catch(console.error);

        if (problemId) {
          problemService.fetchContestProblemDetail(contestId, problemId).then(data => {
            setProblem((prev: any) => prev ? { ...prev, acceptance: data.acceptance, status: data.status } : data);
          }).catch(console.error);
        }
      }
    }, 1000);
  };

  const handleSubmit = () => {
    if (!problemId) return;
    setIsSubmitting(true);
    setTestcasesLogs([]);
    setOverallResult(null);
    setExpandedTestcases({});
    
    const langKey = LANGUAGE_KEYS[selectedLangId];
    const sourceCode = langKey ? codeByLang[langKey] : '';

    problemService.submitSolution(problemId, selectedLangId, sourceCode, contestId)
      .then(() => {
        if (user && user.id) {
          const socket = new SockJS('http://localhost:8080/nonstopcoding/ws');
          const stompClient = Stomp.over(socket);
          stompClient.debug = () => {};
          
          stompClient.connect({}, () => {
            stompClient.subscribe(`/topic/submissions/${user.id}`, (message) => {
              const payload = JSON.parse(message.body);
              if (payload.testcaseId) {
                 setTestcasesLogs(prev => {
                   if (prev.find(p => p.testcaseId === payload.testcaseId)) return prev;
                   return [...prev, payload];
                 });
                 if (payload.overallVerdict && payload.overallVerdict !== 'PENDING' && payload.overallVerdict !== 'PROCESSING') {
                   setOverallResult(payload);
                   setIsSubmitting(false);
                   stompClient.disconnect();
                   fetchSubmissionsAfterDelay();
                 }
              } else if (payload.overallVerdict) {
                 setOverallResult(payload);
                 setIsSubmitting(false);
                 stompClient.disconnect();
                 fetchSubmissionsAfterDelay();
              }
            });
          });
        } else {
          setIsSubmitting(false);
        }
      })
      .catch(err => {
        setIsSubmitting(false);
        const errMsg = err.message || 'Submission failed';
        if (errMsg.includes('404') || errMsg.includes('Not Found') || errMsg.includes('Lỗi hệ thống') || errMsg.toLowerCase().includes('judge0') || errMsg.toLowerCase().includes('ngrok')) {
          setMaintenanceError(true);
        } else {
          alert(errMsg);
        }
      });
  };

  const getTabClass = (tab: 'description' | 'submissions') => {
    return activeTab === tab
      ? "py-3 text-sm font-bold text-primary border-b-2 border-primary whitespace-nowrap outline-none"
      : "py-3 text-sm font-medium text-text-muted hover:text-text-main whitespace-nowrap border-b-2 border-transparent outline-none";
  };

  if (loading) {
    return (
      <div className="flex flex-col h-[calc(100vh-64px)] w-full items-center justify-center bg-surface-gray">
        <svg className="animate-spin h-10 w-10 text-primary mb-4" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>
        <span className="text-text-muted font-semibold">Loading contest problem...</span>
      </div>
    );
  }

  if (error || !problem) {
    return (
      <div className="flex flex-col h-[calc(100vh-64px)] w-full items-center justify-center bg-surface-gray text-red-600">
        <span className="material-symbols-outlined text-[48px] mb-2">error</span>
        <span className="font-bold mb-2">Error Loading Problem</span>
        <span className="text-sm text-text-muted">{error || "Problem not found"}</span>
        <Link to={`/contests/${contestId}/problems`} className="mt-4 bg-primary text-white px-4 py-2 rounded font-bold text-sm">Back to Problems</Link>
      </div>
    );
  }

  const filteredSubmissions = submissions.filter(sub => sub.problemId === Number(problemId));

  return (
    <div className="flex flex-col h-[calc(100vh-64px)] w-full mx-auto overflow-hidden bg-surface-gray">
      <style dangerouslySetInnerHTML={{
        __html: `
        .resizer {
            width: 8px;
            cursor: col-resize;
            background-color: #e5e7eb;
            transition: background-color 0.2s;
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 10;
        }
        .resizer:hover, .resizer.dragging {
            background-color: #F36F21;
        }
        .resizer::after {
            content: "";
            display: block;
            width: 2px;
            height: 24px;
            background-color: #9ca3af;
            border-radius: 2px;
        }
        .resizer:hover::after, .resizer.dragging::after {
            background-color: #ffffff;
        }
        
        .hide-scrollbar::-webkit-scrollbar {
            display: none;
        }
        .hide-scrollbar {
            -ms-overflow-style: none;
            scrollbar-width: none;
        }

        /* Editor scrollbar */
        .custom-scroll::-webkit-scrollbar {
            width: 8px;
            height: 8px;
        }
        .custom-scroll::-webkit-scrollbar-track {
            background: #f8f9fa;
        }
        .custom-scroll::-webkit-scrollbar-thumb {
            background: #cbd5e1;
            border-radius: 4px;
        }
        .custom-scroll::-webkit-scrollbar-thumb:hover {
            background: #94a3b8;
        }

        /* Hide details marker */
        details > summary {
            list-style: none;
        }
        details > summary::-webkit-details-marker {
            display: none;
        }
      `}} />

      {/* Back button row */}
      <div className="px-4 py-2 bg-surface border-b border-gray-200 flex items-center justify-between shrink-0 h-12">
        <Link to={`/contests/${contestId}/problems`} className="inline-flex items-center gap-2 text-text-muted hover:text-primary transition-colors text-sm font-medium group">
          <span className="material-symbols-outlined text-[20px] group-hover:-translate-x-1 transition-transform">arrow_back</span>
          Back to Problems
        </Link>
        <div className="flex items-center gap-4">
          {problem.status === 'solved' && (
            <span className="text-sm font-semibold text-brand-green flex items-center gap-1">
              <span className="material-symbols-outlined text-[18px]">check_circle</span> Solved
            </span>
          )}
          {problem.status === 'attempted' && (
            <span className="text-sm font-semibold text-red-600 flex items-center gap-1">
              <span className="material-symbols-outlined text-[18px]">cancel</span> Attempted
            </span>
          )}
          {problem.status === 'unsolved' && (
            <span className="text-sm font-semibold text-text-muted flex items-center gap-1">
              <span className="material-symbols-outlined text-[18px]">help_outline</span> Unsolved
            </span>
          )}
          <span className="text-sm font-medium text-text-muted border-l border-gray-300 pl-4">Acceptance: {problem.acceptance || '0%'}</span>
        </div>
      </div>

      <div className="flex-grow flex h-[calc(100vh-112px)] overflow-hidden relative" id="split-container">
        {/* Left Pane */}
        <div id="left-pane" className="flex flex-col bg-surface border-r border-gray-200 overflow-hidden" style={{ width: `${leftWidth}%` }}>
          {/* Navbar */}
          <div className="flex items-center gap-6 px-4 bg-surface-gray border-b border-gray-200 shrink-0 overflow-x-auto hide-scrollbar">
            <button className={getTabClass('description')} onClick={() => setActiveTab('description')}>Description</button>
            <button className={getTabClass('submissions')} onClick={() => setActiveTab('submissions')}>Submissions</button>
          </div>

          {/* Tab Contents */}
          <div className="flex-grow overflow-y-auto p-6" id="tab-contents">
            {/* Description Tab */}
            {activeTab === 'description' && (
              <div id="tab-description" className="block space-y-6">
                <div className="flex items-center justify-between">
                  <h1 className="text-2xl font-bold text-text-main">
                    {problem.problemLabel ? `${problem.problemLabel}. ` : `${problem.id}. `}{problem.title}
                  </h1>
                  <div className="flex items-center gap-2">
                    {problem.difficulty === 'Easy' && <span className="bg-green-50 border border-green-200 text-brand-green px-3 py-1 rounded-full text-xs font-bold">Easy</span>}
                    {problem.difficulty === 'Medium' && <span className="bg-orange-50 border border-orange-200 text-orange-500 px-3 py-1 rounded-full text-xs font-bold">Medium</span>}
                    {problem.difficulty === 'Hard' && <span className="bg-red-50 border border-red-200 text-red-600 px-3 py-1 rounded-full text-xs font-bold">Hard</span>}
                  </div>
                </div>

                <div className="space-y-4 text-base text-text-main leading-relaxed">
                  <div dangerouslySetInnerHTML={{ __html: formatHtmlText(problem.description) }} />

                  {problem.inputDescription && (
                    <div>
                      <h3 className="font-semibold text-lg mb-1 mt-4">Input Description</h3>
                      <div className="text-text-muted text-sm leading-relaxed" dangerouslySetInnerHTML={{ __html: formatHtmlText(problem.inputDescription) }} />
                    </div>
                  )}

                  {problem.outputDescription && (
                    <div>
                      <h3 className="font-semibold text-lg mb-1 mt-4">Output Description</h3>
                      <div className="text-text-muted text-sm leading-relaxed" dangerouslySetInnerHTML={{ __html: formatHtmlText(problem.outputDescription) }} />
                    </div>
                  )}
                </div>

                {(problem.exampleInput || problem.exampleOutput) && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {problem.exampleInput && (
                      <div className="flex flex-col">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="text-text-main font-semibold text-lg">Sample Input</span>
                          <button
                            onClick={() => copyToClipboard(problem.exampleInput, 'input')}
                            className="text-text-muted hover:text-text-main transition-colors flex items-center"
                            title="Copy Sample Input"
                          >
                            <span className="material-symbols-outlined text-[20px]">
                              {copiedInput ? 'check' : 'assignment'}
                            </span>
                          </button>
                        </div>
                        <div className="bg-white border border-gray-200 rounded p-4 font-mono text-sm overflow-x-auto min-h-[100px]">
                          <pre className="whitespace-pre-wrap">{formatPreText(problem.exampleInput)}</pre>
                        </div>
                      </div>
                    )}
                    
                    {problem.exampleOutput && (
                      <div className="flex flex-col">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="text-text-main font-semibold text-lg">Sample Output</span>
                          <button
                            onClick={() => copyToClipboard(problem.exampleOutput, 'output')}
                            className="text-text-muted hover:text-text-main transition-colors flex items-center"
                            title="Copy Sample Output"
                          >
                            <span className="material-symbols-outlined text-[20px]">
                              {copiedOutput ? 'check' : 'assignment'}
                            </span>
                          </button>
                        </div>
                        <div className="bg-white border border-gray-200 rounded p-4 font-mono text-sm overflow-x-auto min-h-[100px]">
                          <pre className="whitespace-pre-wrap">{formatPreText(problem.exampleOutput)}</pre>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {problem.constraints && (
                  <div>
                    <h3 className="font-semibold text-lg mb-3">Constraints:</h3>
                    <div className="list-disc list-inside space-y-2 text-text-main bg-surface-gray p-4 rounded-lg border border-gray-200 font-mono text-sm" dangerouslySetInnerHTML={{ __html: problem.constraints }} />
                  </div>
                )}


              </div>
            )}

            {/* Submissions Tab */}
            {activeTab === 'submissions' && (
              <div id="tab-submissions" className="block space-y-6">
                <div className="flex items-center justify-between">
                  <h2 className="text-xl font-bold text-brand-blue">Submissions</h2>
                </div>
                <div className="bg-surface border border-gray-200 rounded-lg overflow-hidden shadow-sm">
                  <div className="overflow-x-auto custom-scroll">
                    <table className="w-full text-left border-collapse whitespace-nowrap min-w-[600px]">
                      <thead>
                        <tr className="bg-surface-gray border-b border-gray-200 text-text-muted text-xs font-bold uppercase tracking-wider">
                          <th className="p-4 w-1/4">Status</th>
                          <th className="p-4">User</th>
                          <th className="p-4">Language</th>
                          <th className="p-4">Runtime</th>
                          <th className="p-4">Memory</th>
                          <th className="p-4 text-right">Time Submitted</th>
                        </tr>
                      </thead>
                      <tbody className="text-sm font-medium text-text-main divide-y divide-gray-100">
                        {filteredSubmissions.map((sub, sIdx) => (
                          <tr key={sIdx} className="hover:bg-surface-gray/50 transition-colors cursor-pointer group">
                            <td className="p-4">
                              <span className={`${sub.statusClass} font-bold group-hover:underline`}>{sub.status}</span>
                            </td>
                            <td className="p-4 text-text-muted">{sub.displayName || sub.username}</td>
                            <td className="p-4 text-text-muted">{sub.lang}</td>
                            <td className="p-4">{sub.runtime}</td>
                            <td className="p-4">{sub.memory}</td>
                            <td className="p-4 text-right text-text-muted">{sub.time || sub.submittedAt}</td>
                          </tr>
                        ))}
                        {filteredSubmissions.length === 0 && (
                          <tr>
                            <td colSpan={6} className="p-8 text-center text-text-muted">
                              No submissions found for this problem.
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

          </div>
        </div>

        {/* Resizer */}
        <div
          id="resizer"
          className={`resizer shrink-0 ${isResizing ? 'dragging' : ''}`}
          title="Drag to resize"
          onMouseDown={startResizing}
        ></div>

        {/* Right Pane */}
        <div id="right-pane" className="flex flex-col bg-surface border-l border-gray-200 overflow-hidden relative" style={{ width: `${100 - leftWidth}%` }}>
          {/* Editor Header */}
          <div className="flex items-center justify-between p-2 bg-surface border-b border-gray-200 shrink-0">
            <div className="flex items-center gap-2">
              <select
                value={selectedLangId}
                onChange={handleLangChange}
                className="bg-surface-gray border border-gray-300 text-text-main text-sm rounded-md focus:ring-primary focus:border-primary block pl-3 pr-10 py-1.5 font-medium cursor-pointer outline-none"
              >
                {SUPPORTED_LANGUAGES.map(lang => (
                  <option key={lang.id} value={lang.id}>{lang.name}</option>
                ))}
              </select>
            </div>
            <div className="flex gap-1 text-text-muted">
              <button
                onClick={handleResetCode}
                aria-label="Reset Code"
                className="p-1.5 hover:bg-surface-gray rounded transition-colors text-text-main hover:text-primary"
                title="Reset Code"
              >
                <span className="material-symbols-outlined text-[20px]">refresh</span>
              </button>
            </div>
          </div>

          {/* Editor Area (Light theme with Monaco) */}
          <div className="flex-grow overflow-hidden relative bg-white border-t border-gray-200">
            <CodeEditor
              language={LANGUAGE_KEYS[selectedLangId] || 'plaintext'}
              value={LANGUAGE_KEYS[selectedLangId] ? codeByLang[LANGUAGE_KEYS[selectedLangId]] : ''}
              onChange={handleCodeChange}
            />
          </div>

          {/* Action Bar */}
          {((user?.role as any) !== 'ROLE_ADMIN' && (user?.role as any) !== 'ADMIN' && user?.role !== 'admin') && (
            <div className="p-3 bg-surface border-t border-gray-200 flex justify-between items-center gap-3 shrink-0 min-h-[60px]">
              
              {/* Left Side: Result Display */}
              <div className="flex-1 flex items-center min-w-0 pr-4">
                {overallResult ? (
                  <div className={`flex items-center gap-2 px-3 py-1.5 rounded-lg font-bold text-sm truncate max-w-full ${overallResult.overallVerdict === 'ACCEPTED' ? 'bg-green-50 text-brand-green border border-green-200' : 'bg-red-50 text-red-600 border border-red-200'}`}>
                    <span className="material-symbols-outlined text-[18px] shrink-0">{overallResult.overallVerdict === 'ACCEPTED' ? 'check_circle' : 'error'}</span>
                    <span className="truncate">{overallResult.overallVerdict === 'ACCEPTED' ? 'Accepted' : overallResult.overallVerdict.replace(/_/g, ' ')}</span>
                    <span className="ml-2 text-[11px] font-medium opacity-80 shrink-0 border-l pl-2 border-current">
                      {overallResult.executionTimeMs?.toFixed(1) || 0}ms | {(overallResult.memoryUsedKb / 1024).toFixed(1) || 0}MB
                    </span>
                  </div>
                ) : isSubmitting && testcasesLogs.length > 0 ? (
                  <div className="text-sm font-medium text-text-muted truncate">
                    Processing: {testcasesLogs[testcasesLogs.length - 1]?.processedTestcases || 0} / {testcasesLogs[testcasesLogs.length - 1]?.totalTestcases || '?'}
                  </div>
                ) : (
                  <div></div>
                )}
              </div>
              
              {/* Right Side: Submit Button or Warning */}
              <div className="shrink-0 flex items-center">
                {contest?.status === 'ENDED' ? (
                  <div className="flex items-center gap-1.5 px-4 py-2 bg-red-50 text-red-600 rounded-lg text-sm font-bold border border-red-200">
                    <span className="material-symbols-outlined text-[18px]">timer_off</span>
                    Time's up! The contest has ended.
                  </div>
                ) : (
                  <button
                    onClick={handleSubmit}
                    disabled={isSubmitting}
                    className="px-8 py-2 bg-brand-green hover:bg-[#3d8c38] text-white rounded-lg font-bold transition-colors shadow-sm text-sm active:scale-95 disabled:bg-gray-400 flex items-center justify-center gap-2 min-w-[120px]"
                  >
                    {isSubmitting ? (
                      <>
                        <svg className="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                        </svg>
                        Submitting...
                      </>
                    ) : (
                      'Submit'
                    )}
                  </button>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Maintenance Modal */}
      {maintenanceError && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-[99] flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl w-full max-w-md p-6 shadow-2xl animate-fade-in border border-slate-200 text-center relative">
            <button 
              onClick={() => setMaintenanceError(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600 transition-colors bg-transparent border-none cursor-pointer"
            >
              <span className="material-symbols-outlined">close</span>
            </button>
            <div className="w-16 h-16 bg-red-50 text-red-500 rounded-full flex items-center justify-center mx-auto mb-4 border border-red-100">
              <span className="material-symbols-outlined text-3xl">build</span>
            </div>
            <h3 className="text-xl font-display font-bold text-slate-800 mb-2">System Under Maintenance</h3>
            <p className="text-sm text-slate-600 mb-6 leading-relaxed">
              The online judge system is currently under maintenance or temporarily unavailable. Please try submitting your code again in a few minutes. We apologize for the inconvenience!
            </p>
            <button
              onClick={() => setMaintenanceError(false)}
              className="w-full bg-primary hover:bg-primary-hover text-white font-bold py-2.5 rounded-lg transition-colors border-none cursor-pointer"
            >
              Got it
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
