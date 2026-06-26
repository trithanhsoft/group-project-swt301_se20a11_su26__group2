import React, { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { problemService } from '../services/problemService';
import type { ProblemDetail, ProblemComment, ProblemSolution } from '../services/problemService';
import { useApp } from '../context/AppContext';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { CodeEditor } from '../components/CodeEditor';
import Editor from '@monaco-editor/react';

export const SolveProblem: React.FC = () => {
  const { user } = useApp();
  const { id } = useParams<{ id: string }>();

  const [activeTab, setActiveTab] = useState<'description' | 'discussion' | 'solutions' | 'submissions' | 'result'>(() => {
    const savedTab = sessionStorage.getItem('solveProblemActiveTab');
    const savedId = sessionStorage.getItem('solveProblemActiveId');
    if (savedId === id && savedTab) {
      return (savedTab as any) || 'description';
    }
    return 'description';
  });

  const [testcasesLogs, setTestcasesLogs] = useState<any[]>([]);
  const [overallResult, setOverallResult] = useState<any>(null);
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
    if (id) {
      sessionStorage.setItem('solveProblemActiveId', id);
      sessionStorage.setItem('solveProblemActiveTab', activeTab);
    }
  }, [id, activeTab]);

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

  // Problem detail states
  const [problem, setProblem] = useState<ProblemDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

  // Track which tabs have already been loaded to avoid duplicate API calls
  const [loadedTabs, setLoadedTabs] = useState<{[key: string]: boolean}>({});

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    // Reset loaded tabs when problem changes
    setLoadedTabs({});
    problemService.fetchProblemDetail(id)
      .then(data => {
        setProblem(data);
        const actualTemplates = data.templates || data.starterTemplates;
        
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
  }, [id]);

  // Lazy-load Submissions only when user clicks the Submissions tab
  useEffect(() => {
    if (!id || activeTab !== 'submissions' || loadedTabs['submissions']) return;
    problemService.fetchProblemSubmissions(id)
      .then(data => {
        setSubmissions(data);
        setLoadedTabs(prev => ({ ...prev, submissions: true }));
      })
      .catch(err => {
        console.error("Failed to load submissions:", err);
      });
  }, [id, activeTab]);

  // Handle changing language
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

  // Handle Reset Code
  const handleResetCode = () => {
    const actualTemplates = problem?.templates || problem?.starterTemplates;
    if (actualTemplates) {
      const defaultCode = getTemplateForLang(selectedLangId, actualTemplates);
      const langKey = LANGUAGE_KEYS[selectedLangId];
      if (langKey) {
        setCodeByLang(prev => ({
          ...prev,
          [langKey]: defaultCode
        }));
      }
    }
  };

  // Resizable logic
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

  // Comments state inside Discussion
  const [comments, setComments] = useState<ProblemComment[]>([]);
  const [commentText, setCommentText] = useState<string>('');
  const [activeReplyId, setActiveReplyId] = useState<number | null>(null);
  const [replyText, setReplyText] = useState<string>('');

  // Lazy-load Comments only when user clicks the Discussion tab
  useEffect(() => {
    if (!id || activeTab !== 'discussion' || loadedTabs['discussion']) return;
    problemService.fetchProblemComments(id)
      .then(data => {
        setComments(data);
        setLoadedTabs(prev => ({ ...prev, discussion: true }));
      })
      .catch(err => {
        console.error("Failed to load comments:", err);
      });
  }, [id, activeTab]);

  const handleAddComment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!id || !commentText.trim()) return;

    problemService.postProblemComment(id, commentText)
      .then(newComment => {
        setComments([newComment, ...comments]);
        setCommentText('');
      })
      .catch(err => {
        alert(err.message || "Failed to post comment. Make sure you are logged in.");
      });
  };

  const getInitials = (name: string) => {
    if (!name) return 'ME';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    if (parts[0].length >= 2) return parts[0].substring(0, 2).toUpperCase();
    return parts[0][0].toUpperCase();
  };

  const getAvatarBg = (name: string) => {
    const bgs = ['bg-brand-blue', 'bg-brand-green', 'bg-orange-500', 'bg-purple-500', 'bg-red-500', 'bg-teal-500'];
    let hash = 0;
    for (let i = 0; i < (name || '').length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
    return bgs[Math.abs(hash) % bgs.length];
  };

  const formatHtmlText = (text: string | undefined | null) => {
    if (!text) return '';
    // Replace literal '\n' with '<br />' for HTML rendering
    return text.replace(/\\n/g, '<br />');
  };

  const formatPreText = (text: string | undefined | null) => {
    if (!text) return '';
    let cleaned = text.replace(/\\n/g, '\n');
    cleaned = cleaned.replace(/\[([a-zA-Z0-9,\s\-.]+)\]/g, '$1');
    return cleaned;
  };

  const handleAddReply = (parentId: number) => {
    if (!id || !replyText.trim()) return;

    problemService.postProblemComment(id, replyText, parentId)
      .then(newReply => {
        setComments(prevComments =>
          prevComments.map(c => {
            if (c.id === parentId) {
              return {
                ...c,
                replies: [...(c.replies || []), newReply]
              };
            }
            return c;
          })
        );
        setActiveReplyId(null);
        setReplyText('');
      })
      .catch(err => {
        alert(err.message || "Failed to post reply. Make sure you are logged in.");
      });
  };

  // Solution states
  const [solution, setSolution] = useState<ProblemSolution | null>(null);
  const [solutionLoading, setSolutionLoading] = useState<boolean>(false);
  const [solutionError, setSolutionError] = useState<string | null>(null);

  const fetchSolution = () => {
    if (!id) return;
    setSolutionLoading(true);
    setSolutionError(null);
    problemService.fetchProblemSolution(id)
      .then(data => {
        setSolution(data);
        setSolutionLoading(false);
        setLoadedTabs(prev => ({ ...prev, solutions: true }));
      })
      .catch(err => {
        console.error("Failed to load solution:", err);
        setSolutionError(err.message || "Failed to load solution.");
        setSolutionLoading(false);
      });
  };

  useEffect(() => {
    if (!id || activeTab !== 'solutions' || loadedTabs['solutions']) return;
    fetchSolution();
  }, [id, activeTab]);

  // Submissions list
  const [submissions, setSubmissions] = useState<any[]>([]);

  // Submit flow states
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const fetchSubmissionsAfterDelay = () => {
    setTimeout(() => {
      if (id) {
        problemService.fetchProblemSubmissions(id).then(data => {
          setSubmissions(data);
          setLoadedTabs(prev => ({ ...prev, submissions: true }));
        }).catch(console.error);

        problemService.fetchProblemDetail(id).then(data => {
          setProblem(prev => prev ? { ...prev, acceptance: data.acceptance, status: data.status } : data);
          // Reset solutions loaded state so it will fetch the new unlocked solution
          setLoadedTabs(prev => ({ ...prev, solutions: false }));
        }).catch(console.error);
      }
    }, 1000);
  };

  const handleSubmit = () => {
    if (!id) return;
    setIsSubmitting(true);
    setTestcasesLogs([]);
    setOverallResult(null);
    setExpandedTestcases({});
    
    const langKey = LANGUAGE_KEYS[selectedLangId];
    const sourceCode = langKey ? codeByLang[langKey] : '';

    problemService.submitSolution(id, selectedLangId, sourceCode)
      .then(() => {
        setActiveTab('result');
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
        alert(err.message || 'Submission failed');
      });
  };

  const getTabClass = (tab: 'description' | 'discussion' | 'solutions' | 'submissions' | 'result') => {
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
        <span className="text-text-muted font-semibold">Loading problem...</span>
      </div>
    );
  }

  if (error || !problem) {
    return (
      <div className="flex flex-col h-[calc(100vh-64px)] w-full items-center justify-center bg-surface-gray text-red-600">
        <span className="material-symbols-outlined text-[48px] mb-2">error</span>
        <span className="font-bold mb-2">Error Loading Problem</span>
        <span className="text-sm text-text-muted">{error || "Problem not found"}</span>
        <Link to="/problems" className="mt-4 bg-primary text-white px-4 py-2 rounded font-bold text-sm">Back to Problems</Link>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-[calc(100vh-64px)] w-full mx-auto overflow-hidden bg-surface-gray">
      {/* Styles injected to ensure identical visual styling of Custom Scrollbars & Resizer */}
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
        <Link to="/problems" className="inline-flex items-center gap-2 text-text-muted hover:text-primary transition-colors text-sm font-medium group">
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
          <span className="text-sm font-medium text-text-muted border-l border-gray-300 pl-4">Acceptance: {problem.acceptance}</span>
        </div>
      </div>

      <div className="flex-grow flex h-[calc(100vh-112px)] overflow-hidden relative" id="split-container">
        {/* Left Pane */}
        <div id="left-pane" className="flex flex-col bg-surface border-r border-gray-200 overflow-hidden" style={{ width: `${leftWidth}%` }}>
          {/* Navbar */}
          <div className="flex items-center gap-6 px-4 bg-surface-gray border-b border-gray-200 shrink-0 overflow-x-auto hide-scrollbar">
            <button className={getTabClass('description')} onClick={() => setActiveTab('description')}>Description</button>
            <button className={getTabClass('discussion')} onClick={() => setActiveTab('discussion')}>Discussion</button>
            <button className={getTabClass('solutions')} onClick={() => setActiveTab('solutions')}>Solutions</button>
            <button className={getTabClass('submissions')} onClick={() => setActiveTab('submissions')}>Submissions</button>
            <button className={getTabClass('result')} onClick={() => setActiveTab('result')}>Test Result</button>
          </div>

          {/* Tab Contents */}
          <div className="flex-grow overflow-y-auto p-6" id="tab-contents">
            {/* Description Tab */}
            {activeTab === 'description' && (
              <div id="tab-description" className="block space-y-6">
                <div className="flex items-center justify-between">
                  <h1 className="text-2xl font-bold text-text-main">{problem.title}</h1>
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

                {(() => {
                  if (!problem.hint) return null;
                  let parsedHints: string[] = [];
                  try {
                    const parsed = JSON.parse(problem.hint);
                    if (Array.isArray(parsed)) parsedHints = parsed;
                    else parsedHints = [problem.hint];
                  } catch {
                    parsedHints = [problem.hint];
                  }
                  
                  return parsedHints.map((h, idx) => (
                    <details key={idx} className="group bg-surface-gray rounded-lg border border-gray-200 mb-2">
                      <summary className="flex items-center justify-between p-4 cursor-pointer font-semibold text-brand-blue">
                        {parsedHints.length > 1 ? `Show Hint ${idx + 1}` : 'Show Hint'}
                        <span className="material-symbols-outlined transition-transform group-open:rotate-180">expand_more</span>
                      </summary>
                      <div className="p-4 border-t border-gray-200 text-text-muted text-sm leading-relaxed bg-surface" dangerouslySetInnerHTML={{ __html: h }} />
                    </details>
                  ));
                })()}
              </div>
            )}

            {/* Discussion Tab */}
            {activeTab === 'discussion' && (
              <div id="tab-discussion" className="block space-y-6">
                <div className="flex justify-between items-center">
                  <h2 className="text-xl font-bold text-brand-blue">Discussion</h2>
                </div>

                {/* Comment Input Box */}
                <form onSubmit={handleAddComment} className="bg-surface border border-gray-200 rounded-lg p-4 shadow-sm flex gap-3">
                  {user && user.avatar ? (
                    <img src={user.avatar} alt={user.name} className="w-10 h-10 rounded-full object-cover shrink-0" />
                  ) : (
                    <div className="w-10 h-10 rounded-full bg-brand-blue flex items-center justify-center text-white text-xs font-bold shrink-0">ME</div>
                  )}
                  <div className="flex-grow space-y-2">
                    {user && <div className="font-bold text-sm text-text-main mb-1">{user.name}</div>}
                    <textarea
                      className="w-full bg-surface-gray border border-gray-200 rounded-lg p-2 text-sm text-text-main focus:ring-primary focus:border-primary outline-none resize-none"
                      rows={2}
                      placeholder="Write a comment..."
                      value={commentText}
                      onChange={(e) => setCommentText(e.target.value)}
                    ></textarea>
                    <div className="flex justify-end">
                      <button type="submit" className="bg-primary hover:bg-primary-hover text-white px-4 py-1.5 rounded-lg text-xs font-bold transition-colors shadow-sm active:scale-95">Comment</button>
                    </div>
                  </div>
                </form>

                {/* Comments List */}
                <div className="space-y-4">
                  {comments.map((comment) => (
                    <div key={comment.id} className="border border-gray-200 rounded-lg p-4 bg-surface space-y-3 shadow-sm">
                      <div className="flex items-center gap-3">
                        {comment.avatar_url ? (
                          <img src={comment.avatar_url} alt={comment.author} className="w-10 h-10 rounded-full object-cover shrink-0" />
                        ) : (
                          <div className={`w-10 h-10 rounded-full ${getAvatarBg(comment.author)} flex items-center justify-center text-white text-sm font-bold shrink-0`}>
                            {getInitials(comment.author)}
                          </div>
                        )}
                        <div>
                          <div className="font-bold text-sm text-text-main">{comment.author}</div>
                          <div className="text-xs text-text-muted">{comment.time}</div>
                        </div>
                      </div>
                      <p className="text-sm text-text-main">{comment.text}</p>
                      <div className="flex gap-4 text-text-muted text-sm font-medium">
                        <button
                          onClick={() => {
                            if (activeReplyId === comment.id) {
                              setActiveReplyId(null);
                            } else {
                              setActiveReplyId(comment.id);
                              setReplyText('');
                            }
                          }}
                          className="flex items-center gap-1 hover:text-primary transition-colors text-xs font-bold"
                        >
                          <span className="material-symbols-outlined text-[16px]">reply</span> Reply
                        </button>
                      </div>

                      {/* Reply Input Box */}
                      {activeReplyId === comment.id && (
                        <div className="mt-3 pl-4 border-l-2 border-primary/40 space-y-2">
                          <textarea
                            className="w-full bg-surface-gray border border-gray-200 rounded-lg p-2 text-xs text-text-main focus:ring-primary focus:border-primary outline-none resize-none"
                            rows={2}
                            placeholder="Write a reply..."
                            value={replyText}
                            onChange={(e) => setReplyText(e.target.value)}
                          />
                          <div className="flex justify-end gap-2">
                            <button
                              onClick={() => setActiveReplyId(null)}
                              className="px-3 py-1 text-xs font-semibold text-text-muted hover:text-text-main transition-colors"
                            >
                              Cancel
                            </button>
                            <button
                              onClick={() => handleAddReply(comment.id)}
                              className="bg-primary hover:bg-primary-hover text-white px-3 py-1 rounded-md text-xs font-bold transition-colors shadow-sm"
                            >
                              Post Reply
                            </button>
                          </div>
                        </div>
                      )}

                      {comment.replies && comment.replies.map((reply) => (
                        <div key={reply.id} className="ml-10 mt-3 pl-4 border-l-2 border-primary space-y-3">
                          <div className="flex items-center gap-3">
                            {reply.avatar_url ? (
                              <img src={reply.avatar_url} alt={reply.author} className="w-8 h-8 rounded-full object-cover shrink-0" />
                            ) : (
                              <div className={`w-8 h-8 rounded-full ${getAvatarBg(reply.author)} flex items-center justify-center text-white text-xs font-bold shrink-0`}>
                                {getInitials(reply.author)}
                              </div>
                            )}
                            <div>
                              <div className="font-bold text-sm text-text-main">{reply.author}</div>
                              <div className="text-xs text-text-muted">{reply.time}</div>
                            </div>
                          </div>
                          <p className="text-sm text-text-main">{reply.text}</p>
                        </div>
                      ))}
                    </div>
                  ))}
                  {comments.length === 0 && (
                    <div className="p-8 text-center text-text-muted border border-dashed border-gray-200 rounded-lg bg-surface">
                      No comments yet. Be the first to start the discussion!
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Solutions Tab */}
            {activeTab === 'solutions' && (
              <div id="tab-solutions" className="block space-y-6">
                <h2 className="text-xl font-bold text-brand-blue">Solutions</h2>
                {solutionLoading ? (
                  <div className="flex justify-center items-center py-12">
                    <svg className="animate-spin h-8 w-8 text-primary" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                    </svg>
                  </div>
                ) : solution ? (
                  <div className="border border-gray-200 rounded-lg overflow-hidden shadow-sm flex flex-col h-[500px]">
                    <div className="bg-surface-gray px-4 py-3 border-b border-gray-200 flex justify-between items-center shrink-0">
                      <div className="flex items-center gap-2">
                        <span className="material-symbols-outlined text-brand-green">check_circle</span>
                        <span className="font-bold text-text-main">{solution.title || problem?.title || 'Official Solution'}</span>
                      </div>
                      <span className="text-sm text-text-muted">Official Solution</span>
                    </div>
                    <div className="flex-grow relative w-full h-full bg-white">
                      <Editor
                        height="100%"
                        width="100%"
                        language={LANGUAGE_KEYS[problem?.language_id || selectedLangId || 62] || 'java'}
                        theme="vs"
                        value={solution.solutionCode || '// An official solution for this problem is not available yet.'}
                        options={{
                          readOnly: true,
                          domReadOnly: true,
                          fontSize: 14,
                          fontWeight: '600',
                          fontFamily: "'Fira Code', 'Courier New', Courier, monospace",
                          minimap: { enabled: false },
                          automaticLayout: true,
                          scrollBeyondLastLine: false,
                          padding: { top: 16, bottom: 16 },
                          tabSize: 4,
                          insertSpaces: true,
                          wordWrap: 'on',
                          lineNumbers: 'on',
                          scrollbar: {
                            vertical: 'visible',
                            horizontal: 'visible',
                            verticalScrollbarSize: 10,
                            horizontalScrollbarSize: 10,
                          },
                        }}
                        loading={
                          <div className="absolute inset-0 flex flex-col items-center justify-center bg-white text-gray-500">
                            <svg className="animate-spin h-8 w-8 text-primary mb-3" fill="none" viewBox="0 0 24 24">
                              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                            </svg>
                            <span className="font-semibold text-sm">Loading Editor...</span>
                          </div>
                        }
                      />
                    </div>
                  </div>
                ) : (
                  <div className="border border-outline-variant/60 rounded-xl bg-surface-gray/30 p-8 text-center flex flex-col items-center justify-center min-h-[300px] border-dashed">
                    <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center text-primary mb-4">
                      <span className="material-symbols-outlined text-[32px] icon-fill" style={{ fontVariationSettings: "'FILL' 1" }}>lock</span>
                    </div>
                    <h3 className="text-headline-sm font-bold text-text-main mb-2">
                      {solutionError && !solutionError.includes("locked") ? "Failed to Load Solution" : "Solution Locked"}
                    </h3>
                    <p className="text-body-md text-text-muted max-w-md mb-6">
                      {solutionError && !solutionError.includes("locked") 
                        ? solutionError 
                        : "To view the author's official solution and optimal approaches, you must first solve this problem and pass all test cases."}
                    </p>
                    {solutionError && !solutionError.includes("locked") ? (
                      <button
                        onClick={fetchSolution}
                        className="px-6 py-2 bg-primary hover:bg-primary-hover text-white text-sm font-bold rounded-lg transition-colors shadow-sm active:scale-95 flex items-center gap-1.5"
                      >
                        Retry Loading
                      </button>
                    ) : (
                      <button
                        onClick={() => setActiveTab('description')}
                        className="px-6 py-2 bg-primary hover:bg-primary-hover text-white text-sm font-bold rounded-lg transition-colors shadow-sm active:scale-95 flex items-center gap-1.5"
                      >
                        <span className="material-symbols-outlined text-[18px]">keyboard_backspace</span> Back to Description
                      </button>
                    )}
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
                          <th className="p-4 w-1/3">Status</th>
                          <th className="p-4">Language</th>
                          <th className="p-4">Runtime</th>
                          <th className="p-4">Memory</th>
                          <th className="p-4 text-right">Time Submitted</th>
                        </tr>
                      </thead>
                      <tbody className="text-sm font-medium text-text-main divide-y divide-gray-100">
                        {submissions.map((sub, sIdx) => (
                          <tr key={sIdx} className="hover:bg-surface-gray/50 transition-colors cursor-pointer group">
                            <td className="p-4">
                              <span className={`${sub.statusClass} font-bold group-hover:underline`}>{sub.status}</span>
                            </td>
                            <td className="p-4 text-text-muted">{sub.lang}</td>
                            <td className="p-4">{sub.runtime}</td>
                            <td className="p-4">{sub.memory}</td>
                            <td className="p-4 text-right text-text-muted">{sub.time}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {/* Result Tab */}
            {activeTab === 'result' && (
              <div id="tab-result" className="block space-y-4 pb-12">
                <div className="flex items-center justify-between border-b border-gray-200 pb-3 mb-4">
                  <h2 className="text-xl font-bold text-brand-blue">Test Result</h2>
                  {overallResult ? (
                    <div className="text-sm font-medium text-text-muted">{overallResult.totalTestcases} Testcases</div>
                  ) : testcasesLogs.length > 0 ? (
                    <div className="text-sm font-medium text-text-muted">{testcasesLogs[testcasesLogs.length - 1]?.processedTestcases || 0} / {testcasesLogs[testcasesLogs.length - 1]?.totalTestcases || 0} Testcases Processed</div>
                  ) : isSubmitting ? (
                    <div className="text-sm font-medium text-warning-color animate-pulse">Running...</div>
                  ) : null}
                </div>

                {overallResult && (
                  <div className={`p-4 rounded-lg border shadow-sm mb-6 ${overallResult.overallVerdict === 'ACCEPTED' ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
                    <h3 className={`font-bold text-lg mb-2 ${overallResult.overallVerdict === 'ACCEPTED' ? 'text-brand-green' : 'text-red-600'}`}>
                      {overallResult.overallVerdict === 'ACCEPTED' ? 'Accepted' : overallResult.overallVerdict.replace(/_/g, ' ')}
                    </h3>
                    <div className="flex gap-6 text-sm text-text-main">
                      <div><span className="text-text-muted font-medium">Runtime:</span> {overallResult.executionTimeMs?.toFixed(1) || 0} ms</div>
                      <div><span className="text-text-muted font-medium">Memory:</span> {(overallResult.memoryUsedKb / 1024).toFixed(1) || 0} MB</div>
                    </div>
                  </div>
                )}

                <div className="space-y-4">
                  {testcasesLogs.map((log, index) => {
                    const isExpanded = expandedTestcases[log.testcaseId] || false;
                    const isSuccess = log.testcaseVerdict === 'ACCEPTED';
                    return (
                      <div key={log.testcaseId} className="border border-gray-200 rounded-lg overflow-hidden bg-white shadow-sm">
                        <div 
                          className="flex items-center justify-between p-4 cursor-pointer hover:bg-gray-50 transition-colors"
                          onClick={() => toggleTestcaseDetails(log.testcaseId)}
                        >
                          <div className="flex items-center gap-3">
                            <div className={`w-6 h-6 rounded-full flex items-center justify-center text-white ${isSuccess ? 'bg-brand-green' : 'bg-red-500'}`}>
                              <span className="material-symbols-outlined text-[16px]">{isSuccess ? 'check' : 'close'}</span>
                            </div>
                            <span className="font-bold text-text-main text-sm">Testcase {index + 1}</span>
                          </div>
                          <button className="text-sm font-semibold text-primary hover:text-primary-hover">
                            {isExpanded ? 'Hide Details' : 'View Details'}
                          </button>
                        </div>
                        
                        {isExpanded && (
                          <div className="p-4 border-t border-gray-100 space-y-4 bg-gray-50/50">
                            <div>
                              <div className="text-xs font-bold text-text-muted uppercase mb-1.5 tracking-wider">Input</div>
                              <div className="bg-surface-gray border border-gray-200 rounded p-3 font-mono text-sm text-text-main whitespace-pre-wrap">{log.input ? log.input.replace(/\\n/g, '\n') : 'N/A'}</div>
                            </div>
                            <div>
                              <div className="text-xs font-bold text-text-muted uppercase mb-1.5 tracking-wider">Expected Output</div>
                              <div className="bg-green-50 border border-green-200 rounded p-3 font-mono text-sm text-brand-green whitespace-pre-wrap">{log.expectedOutput ? log.expectedOutput.replace(/\\n/g, '\n') : 'N/A'}</div>
                            </div>
                            <div>
                              <div className="text-xs font-bold text-text-muted uppercase mb-1.5 tracking-wider">Actual Output</div>
                              <div className={`border rounded p-3 font-mono text-sm whitespace-pre-wrap ${isSuccess ? 'bg-surface-gray border-gray-200 text-text-main' : 'bg-red-50 border-red-200 text-red-600'}`}>{log.actualOutput ? log.actualOutput.replace(/\\n/g, '\n') : 'N/A'}</div>
                            </div>
                            {log.compileOutput && (
                                <div>
                                    <div className="text-xs font-bold text-text-muted uppercase mb-1.5 tracking-wider">Compile/Error Output</div>
                                    <div className="bg-yellow-50 border border-yellow-200 rounded p-3 font-mono text-sm text-yellow-700 whitespace-pre-wrap">{log.compileOutput.replace(/\\n/g, '\n')}</div>
                                </div>
                            )}
                          </div>
                        )}
                      </div>
                    );
                  })}
                  {isSubmitting && testcasesLogs.length === 0 && (
                    <div className="py-12 flex flex-col items-center justify-center text-text-muted">
                      <svg className="animate-spin h-8 w-8 text-primary mb-3" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                      </svg>
                      <span className="font-semibold text-sm">Evaluating your code against test cases...</span>
                    </div>
                  )}
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

          {/* Action Bar (Removed Run Code, kept Submit) */}
          <div className="p-3 bg-surface border-t border-gray-200 flex justify-end gap-3 shrink-0">
            <button
              onClick={handleSubmit}
              disabled={isSubmitting}
              className="px-8 py-2 bg-brand-green hover:bg-[#3d8c38] text-white rounded-lg font-bold transition-colors shadow-sm text-sm active:scale-95 disabled:bg-gray-400 flex items-center justify-center gap-2"
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
          </div>
        </div>
      </div>
    </div>
  );
};
