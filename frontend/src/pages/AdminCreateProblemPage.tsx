import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { adminService } from '../services/adminService';
import type { AdminProblem, AdminProblemTestcase } from '../services/adminService';
import { useApp } from '../context/AppContext';

const GENERATOR_TEMPLATES: Record<string, string> = {
  java: `import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        // Number of test cases\n        int numberOfTests = 3;\n        \n        for (int i = 0; i < numberOfTests; i++) {\n            // Write your logic here\n            \n            // DO NOT REMOVE\n            System.out.println("---TESTCASE---");\n            System.out.println("INPUT:");\n            \n            // Print your input here\n            \n            // DO NOT REMOVE\n            System.out.println("OUTPUT:");\n            \n            // Print your output here\n        }\n    }\n}`,
  python: `# Number of test cases\nnumberOfTests = 3\n\nfor _ in range(numberOfTests):\n    # Write your logic here\n    \n    # DO NOT REMOVE\n    print("---TESTCASE---")\n    print("INPUT:")\n    \n    # Print your input here\n    \n    # DO NOT REMOVE\n    print("OUTPUT:")\n    \n    # Print your output here\n`,
  cpp: `#include <iostream>\nusing namespace std;\n\nint main() {\n    // Number of test cases\n    int numberOfTests = 3;\n    \n    for (int i = 0; i < numberOfTests; i++) {\n        // Write your logic here\n        \n        // DO NOT REMOVE\n        cout << "---TESTCASE---\\n";\n        cout << "INPUT:\\n";\n        \n        // Print your input here\n        \n        // DO NOT REMOVE\n        cout << "OUTPUT:\\n";\n        \n        // Print your output here\n    }\n    return 0;\n}`,
  c: `#include <stdio.h>\n\nint main() {\n    // Number of test cases\n    int numberOfTests = 3;\n    \n    for (int i = 0; i < numberOfTests; i++) {\n        // Write your logic here\n        \n        // DO NOT REMOVE\n        printf("---TESTCASE---\\n");\n        printf("INPUT:\\n");\n        \n        // Print your input here\n        \n        // DO NOT REMOVE\n        printf("OUTPUT:\\n");\n        \n        // Print your output here\n    }\n    return 0;\n}`,
  csharp: `using System;\n\npublic class Solution {\n    public static void Main() {\n        // Number of test cases\n        int numberOfTests = 3;\n        \n        for (int i = 0; i < numberOfTests; i++) {\n            // Write your logic here\n            \n            // DO NOT REMOVE\n            Console.WriteLine("---TESTCASE---");\n            Console.WriteLine("INPUT:");\n            \n            // Print your input here\n            \n            // DO NOT REMOVE\n            Console.WriteLine("OUTPUT:");\n            \n            // Print your output here\n        }\n    }\n}`
};

interface LocationState {
  problem?: AdminProblem;
}

interface AdminCreateProblemPageProps {
  mode: 'create' | 'edit';
}

export const AdminCreateProblemPage: React.FC<AdminCreateProblemPageProps> = ({ mode }) => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LocationState;
  
  // Create a local popup state for errors and success messages
  const [alertData, setAlertData] = useState<{ msg: string; type: string } | null>(null);
  const showGlobalToast = useCallback((msg: string, type: string) => setAlertData({ msg, type }), []);

  const [editingProblemId, setEditingProblemId] = useState<number | null>(mode === 'edit' && id ? parseInt(id, 10) : null);
  const [newProbTitle, setNewProbTitle] = useState('');
  const [newProbDesc, setNewProbDesc] = useState('');
  const [newProbInputDesc, setNewProbInputDesc] = useState('');
  const [newProbOutputDesc, setNewProbOutputDesc] = useState('');
  const [newProbConstraints, setNewProbConstraints] = useState('');
  const [newProbExampleInput, setNewProbExampleInput] = useState('');
  const [newProbExampleOutput, setNewProbExampleOutput] = useState('');
  const [newProbHints, setNewProbHints] = useState<string[]>(['']);
  const [newProbScope, setNewProbScope] = useState<'LESSON' | 'CONTEST' | 'SHARED' | 'PRACTICE'>('PRACTICE');
  const [newProbDifficulty, setNewProbDifficulty] = useState<'EASY' | 'MEDIUM' | 'HARD'>('MEDIUM');
  const [newProbScore, setNewProbScore] = useState(100);
  const [newProbTimeLimit, setNewProbTimeLimit] = useState(2000);
  const [newProbMemoryLimit, setNewProbMemoryLimit] = useState(128000);
  const [newProbIsPublic, setNewProbIsPublic] = useState(false);
  const [newProbSolutions, setNewProbSolutions] = useState('');
  const [newProbTags, setNewProbTags] = useState<string[]>([]);
  const [newProbStarterC, setNewProbStarterC] = useState('');
  const [newProbStarterCpp, setNewProbStarterCpp] = useState('');
  const [newProbStarterJava, setNewProbStarterJava] = useState('');
  const [newProbStarterPython, setNewProbStarterPython] = useState('');
  const [newProbStarterCsharp, setNewProbStarterCsharp] = useState('');
  const [starterActiveTab, setStarterActiveTab] = useState('C');
  const [allTags, setAllTags] = useState<{ id: number; name: string; slug: string }[]>([]);
  
  const [testcasesList, setTestcasesList] = useState<Omit<AdminProblemTestcase, 'id'>[]>([]);
  const [testCaseGenerationMode, setTestCaseGenerationMode] = useState<'manual' | 'generate'>('manual');
  const [generatorLanguage, setGeneratorLanguage] = useState('java');
  const [generatorCode, setGeneratorCode] = useState(GENERATOR_TEMPLATES['java']);
  const [generateLoading, setGenerateLoading] = useState(false);
  const [generateError, setGenerateError] = useState<string | null>(null);

  useEffect(() => {
    if (mode === 'edit' && id) {
      const fetchProblem = async () => {
        try {
          let problem = state?.problem;
          if (!problem) {
            const res = await adminService.getProblems();
            problem = res.find((p: AdminProblem) => p.id === parseInt(id, 10));
          }
          if (problem) {
            setNewProbTitle(problem.title);
            setNewProbDesc(problem.description);
            setNewProbInputDesc(problem.inputDescription || '');
            setNewProbOutputDesc(problem.outputDescription || '');
            setNewProbConstraints(problem.constraints || '');
            setNewProbExampleInput(problem.exampleInput || '');
            setNewProbExampleOutput(problem.exampleOutput || '');
            setNewProbHints(problem.hint ? [problem.hint] : ['']);
            setNewProbScope(problem.problemScope);
            setNewProbDifficulty(problem.difficulty);
            setNewProbScore(problem.score);
            setNewProbTimeLimit(problem.timeLimitMs);
            setNewProbMemoryLimit(problem.memoryLimitKb);
            setNewProbIsPublic(problem.isPublic);
            setNewProbSolutions(problem.solutions || '');
            setNewProbTags(problem.tags || []);
            setNewProbStarterC(problem.starterTemplates?.['C'] || '');
            setNewProbStarterCpp(problem.starterTemplates?.['C++'] || '');
            setNewProbStarterJava(problem.starterTemplates?.['Java'] || '');
            setNewProbStarterPython(problem.starterTemplates?.['Python 3'] || '');
            setNewProbStarterCsharp(problem.starterTemplates?.['C#'] || '');
            
            // fetch testcases
            const tcRes = await adminService.getProblemTestcases(problem.id);
            if (tcRes) {
                setTestcasesList(tcRes);
            }
          }
        } catch (err) {
          showGlobalToast("Failed to fetch problem details", "error");
        }
      };
      fetchProblem();
    }
  }, [mode, id, showGlobalToast]);

  const fetchProblems = async () => {
      // Dummy function just to satisfy the handlers below that expect it
  };

  const handleCloseCreateProblem = () => {
    navigate('/admin/problems');
  };

  const handleCloseEditProblem = () => {
    navigate('/admin/problems');
  };
  const handleCreateProblemSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newProbTitle.trim()) { showGlobalToast("Problem Title is required.", "error"); return; }
    if (!newProbDesc.trim()) { showGlobalToast("Problem Description is required.", "error"); return; }
    if (!newProbInputDesc.trim()) { showGlobalToast("Input Description is required.", "error"); return; }
    if (!newProbOutputDesc.trim()) { showGlobalToast("Output Description is required.", "error"); return; }
    if (!newProbConstraints.trim()) { showGlobalToast("Constraints are required.", "error"); return; }
    if (!newProbExampleInput.trim()) { showGlobalToast("Example Input is required.", "error"); return; }
    if (!newProbExampleOutput.trim()) { showGlobalToast("Example Output is required.", "error"); return; }
    if (newProbScore <= 0) { showGlobalToast("Max Score must be greater than 0.", "error"); return; }
    if (newProbTimeLimit <= 0) { showGlobalToast("Time Limit must be greater than 0.", "error"); return; }
    if (newProbMemoryLimit <= 0) { showGlobalToast("Memory Limit must be greater than 0.", "error"); return; }
    if (newProbIsPublic) {
      if (testcasesList.length === 0) { showGlobalToast("At least one test case is required.", "error"); return; }
      if (testcasesList.some(tc => !tc.inputData.trim() || !tc.expectedOutput.trim())) { showGlobalToast("All test cases must have input and expected output data.", "error"); return; }
    }

    try {
      const starterTemplates: Record<string, string> = {};
      if (newProbStarterC) starterTemplates['C'] = newProbStarterC;
      if (newProbStarterCpp) starterTemplates['C++'] = newProbStarterCpp;
      if (newProbStarterJava) starterTemplates['Java'] = newProbStarterJava;
      if (newProbStarterPython) starterTemplates['Python 3'] = newProbStarterPython;
      if (newProbStarterCsharp) starterTemplates['C#'] = newProbStarterCsharp;

      const newProb = await adminService.createProblem({
        title: newProbTitle.trim(),
        description: newProbDesc.trim(),
        inputDescription: newProbInputDesc.trim(),
        outputDescription: newProbOutputDesc.trim(),
        constraints: newProbConstraints.trim(),
        exampleInput: newProbExampleInput.trim(),
        exampleOutput: newProbExampleOutput.trim(),
        hint: JSON.stringify(newProbHints.filter(h => h.trim() !== '')),
        problemScope: newProbScope,
        difficulty: newProbDifficulty,
        totalTestcases: 0,
        timeLimitMs: newProbTimeLimit,
        memoryLimitKb: newProbMemoryLimit,
        isPublic: newProbIsPublic,
        score: newProbScore,
        solutions: newProbSolutions.trim(),
        tags: newProbTags,
        starterTemplates,
        isDeleted: false
      });

      if (testcasesList.length > 0) {
        try {
          const tcsToSave = testcasesList.map((tc, idx) => ({
            ...tc,
            problemId: newProb.id,
            orderIndex: idx + 1
          }));
          await adminService.saveProblemTestcases(newProb.id, tcsToSave);
          if (newProbIsPublic) {
            await adminService.updateProblemPublicStatus(newProb.id, true);
          }
        } catch (tcError) {
          showGlobalToast("Problem created, but failed to save testcases.", "error");
        }
      }

      showGlobalToast(`Problem "${newProb.title}" created successfully!`, "success");
      navigate('/admin/problems');
    } catch (error) {
      showGlobalToast("Failed to create problem", "error");
    }
  };

  const handleEditProblemSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (editingProblemId === null) return;
    if (!newProbTitle.trim()) { showGlobalToast("Problem Title is required.", "error"); return; }
    if (!newProbDesc.trim()) { showGlobalToast("Problem Description is required.", "error"); return; }
    if (!newProbInputDesc.trim()) { showGlobalToast("Input Description is required.", "error"); return; }
    if (!newProbOutputDesc.trim()) { showGlobalToast("Output Description is required.", "error"); return; }
    if (!newProbConstraints.trim()) { showGlobalToast("Constraints are required.", "error"); return; }
    if (!newProbExampleInput.trim()) { showGlobalToast("Example Input is required.", "error"); return; }
    if (!newProbExampleOutput.trim()) { showGlobalToast("Example Output is required.", "error"); return; }
    if (newProbScore <= 0) { showGlobalToast("Max Score must be greater than 0.", "error"); return; }
    if (newProbTimeLimit <= 0) { showGlobalToast("Time Limit must be greater than 0.", "error"); return; }
    if (newProbMemoryLimit <= 0) { showGlobalToast("Memory Limit must be greater than 0.", "error"); return; }
    if (newProbIsPublic) {
      if (testcasesList.length === 0) { showGlobalToast("At least one test case is required.", "error"); return; }
      if (testcasesList.some(tc => !tc.inputData.trim() || !tc.expectedOutput.trim())) { showGlobalToast("All test cases must have input and expected output data.", "error"); return; }
    }

    try {
      const starterTemplates: Record<string, string> = {};
      if (newProbStarterC) starterTemplates['C'] = newProbStarterC;
      if (newProbStarterCpp) starterTemplates['C++'] = newProbStarterCpp;
      if (newProbStarterJava) starterTemplates['Java'] = newProbStarterJava;
      if (newProbStarterPython) starterTemplates['Python 3'] = newProbStarterPython;
      if (newProbStarterCsharp) starterTemplates['C#'] = newProbStarterCsharp;

      const updatedProb = await adminService.updateProblem(editingProblemId, {
        title: newProbTitle.trim(),
        description: newProbDesc.trim(),
        inputDescription: newProbInputDesc.trim(),
        outputDescription: newProbOutputDesc.trim(),
        constraints: newProbConstraints.trim(),
        exampleInput: newProbExampleInput.trim(),
        exampleOutput: newProbExampleOutput.trim(),
        hint: JSON.stringify(newProbHints.filter(h => h.trim() !== '')),
        problemScope: newProbScope,
        difficulty: newProbDifficulty,
        totalTestcases: 0,
        timeLimitMs: newProbTimeLimit,
        memoryLimitKb: newProbMemoryLimit,
        isPublic: newProbIsPublic,
        score: newProbScore,
        solutions: newProbSolutions.trim(),
        tags: newProbTags,
        starterTemplates,
        isDeleted: false
      });

      try {
        const tcsToSave = testcasesList.map((tc, idx) => ({
          ...tc,
          problemId: editingProblemId,
          orderIndex: idx + 1
        }));
        await adminService.saveProblemTestcases(editingProblemId, tcsToSave);
        if (newProbIsPublic) {
          await adminService.updateProblemPublicStatus(editingProblemId, true);
        }
      } catch (tcError) {
        showGlobalToast("Problem metadata updated, but failed to save testcases.", "error");
      }

      showGlobalToast(`Problem "${updatedProb.title}" updated successfully!`, "success");
      navigate('/admin/problems');
    } catch (error) {
      showGlobalToast("Failed to update problem", "error");
    }
  };

  const handleRunAndGenerateTestcases = async () => {
    setGenerateError(null);
    setGenerateLoading(true);
    try {
      const response = await fetch('http://localhost:8080/nonstopcoding/instructor/testcases/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          language: generatorLanguage,
          code: generatorCode
        })
      });
      
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'An error occurred while generating testcases.');
      }
      
      const generatedTestcases = data.result;
      if (generatedTestcases && generatedTestcases.length > 0) {
        setTestcasesList(prev => [
          ...prev, 
          ...generatedTestcases.map((tc: any, index: number) => ({
             problemId: editingProblemId || 0,
             inputData: tc.input,
             expectedOutput: tc.output,
             orderIndex: prev.length + index + 1,
             isHidden: false
          }))
        ]);
        setTestCaseGenerationMode('manual');
        showGlobalToast(`Generated ${generatedTestcases.length} testcases successfully!`, "success");
      } else {
        setGenerateError("Code executed successfully but no test cases were found. Please check your output format.");
      }
    } catch (err: any) {
      setGenerateError(err.message || "An error occurred while generating testcases.");
    } finally {
      setGenerateLoading(false);
    }
  };

  return (
    <div className="w-full min-h-screen bg-[#f0f4f9] p-6">
      <div className="bg-white w-full rounded-3xl overflow-hidden shadow-sm border border-slate-200">
        <div className="bg-brand-blue px-6 py-4 flex items-center shrink-0">
          <div className="flex items-center gap-4">
            <button
              type="button"
              onClick={() => navigate('/admin/problems')}
              className="text-white/70 hover:text-white transition-colors flex items-center gap-1.5 text-sm font-medium uppercase tracking-wider"
            >
              <span className="material-symbols-outlined text-[20px]">arrow_back</span>
              Back
            </button>
            <div className="h-6 w-px bg-white/20"></div>
            <h2 className="text-white font-display font-black text-xl flex items-center gap-2">
              <span className="material-symbols-outlined text-primary">code</span>
              {mode === 'edit' ? "Edit Programming Problem" : "Create Programming Problem"}
            </h2>
          </div>
        </div>

            <form onSubmit={mode === 'edit' ? handleEditProblemSubmit : handleCreateProblemSubmit} className="p-6 flex flex-col gap-6">
              
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Problem Title *</label>
                <input required type="text" value={newProbTitle} onChange={e => setNewProbTitle(e.target.value)} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:ring-primary focus:border-primary text-brand-blue" placeholder="e.g. Two Sum" />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Difficulty Level</label>
                  <div className="flex p-1 bg-slate-100/80 rounded-xl border border-slate-200/50 shadow-inner">
                    {['EASY', 'MEDIUM', 'HARD'].map(diff => {
                      const isSelected = newProbDifficulty === diff;
                      let textColor = 'text-brand-blue';
                      if (isSelected) {
                        if (diff === 'EASY') textColor = 'text-emerald-600';
                        if (diff === 'MEDIUM') textColor = 'text-amber-500';
                        if (diff === 'HARD') textColor = 'text-rose-600';
                      }
                      return (
                        <label key={diff} className={`flex-1 flex items-center justify-center py-2 rounded-lg cursor-pointer transition-all duration-300 text-[13px] font-bold tracking-wide ${isSelected ? `bg-white ${textColor} shadow-sm ring-1 ring-black/5` : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'}`}>
                          <input type="radio" name="probDifficulty" value={diff} checked={isSelected} onChange={() => setNewProbDifficulty(diff as any)} className="hidden" />
                          {diff}
                        </label>
                      );
                    })}
                  </div>
                </div>
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Scope</label>
                  <div className="flex p-1 bg-slate-100/80 rounded-xl border border-slate-200/50 shadow-inner">
                    {['PRACTICE', 'CONTEST', 'SHARED'].map(sc => {
                      const isSelected = newProbScope === sc;
                      return (
                        <label key={sc} className={`flex-1 flex items-center justify-center py-2 rounded-lg cursor-pointer transition-all duration-300 text-[13px] font-bold tracking-wide ${isSelected ? 'bg-white text-primary shadow-sm ring-1 ring-black/5' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'}`}>
                          <input type="radio" name="probScope" value={sc} checked={isSelected} onChange={() => setNewProbScope(sc as any)} className="hidden" />
                          {sc}
                        </label>
                      );
                    })}
                  </div>
                </div>
              </div>

              {allTags.length > 0 && (
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Problem Tags</label>
                  <div className="flex flex-wrap gap-2 mt-1">
                    {allTags.map(tag => {
                      const isSelected = newProbTags.includes(tag.name);
                      return (
                        <button
                          key={tag.id}
                          type="button"
                          onClick={() => {
                            if (isSelected) {
                              setNewProbTags(newProbTags.filter(t => t !== tag.name));
                            } else {
                              setNewProbTags([...newProbTags, tag.name]);
                            }
                          }}
                          className={`px-4 py-1.5 rounded-full text-xs font-bold transition-all border ${isSelected ? 'bg-indigo-50 border-indigo-200 text-indigo-600 font-extrabold shadow-sm' : 'bg-white border-slate-200 text-slate-500 hover:bg-slate-50 hover:shadow-sm'}`}
                        >
                          {tag.name}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Problem Description (Markdown) *</label>
                <textarea required value={newProbDesc} onChange={e => setNewProbDesc(e.target.value)} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:ring-primary focus:border-primary text-brand-blue resize-y h-32" placeholder="Explain the problem here..." />
              </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="flex flex-col gap-1.5">
                    <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Input Description *</label>
                    <textarea rows={2} value={newProbInputDesc} onChange={e => setNewProbInputDesc(e.target.value)} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:ring-primary focus:border-primary text-brand-blue" placeholder="Describe input structure..." />
                  </div>
                  <div className="flex flex-col gap-1.5">
                    <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Output Description *</label>
                    <textarea rows={2} value={newProbOutputDesc} onChange={e => setNewProbOutputDesc(e.target.value)} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:ring-primary focus:border-primary text-brand-blue" placeholder="Describe output structure..." />
                  </div>
                </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Constraints *</label>
                <textarea rows={2} value={newProbConstraints} onChange={e => setNewProbConstraints(e.target.value)} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:ring-primary focus:border-primary text-brand-blue" placeholder="e.g. 1 <= nums.length <= 10^5" />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Example Input *</label>
                  <textarea rows={2} value={newProbExampleInput} onChange={e => setNewProbExampleInput(e.target.value)} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-mono focus:ring-primary focus:border-primary text-brand-blue" placeholder="Input sample..." />
                </div>
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Example Output *</label>
                  <textarea rows={2} value={newProbExampleOutput} onChange={e => setNewProbExampleOutput(e.target.value)} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-mono focus:ring-primary focus:border-primary text-brand-blue" placeholder="Output sample..." />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Max Score *</label>
                  <input type="number" value={newProbScore} onChange={e => setNewProbScore(Number(e.target.value))} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:ring-primary focus:border-primary text-brand-blue" />
                </div>
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Time Limit (ms) *</label>
                  <input type="number" value={newProbTimeLimit} onChange={e => setNewProbTimeLimit(Number(e.target.value))} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:ring-primary focus:border-primary text-brand-blue" />
                </div>
                <div className="flex flex-col gap-1.5">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Memory Limit (KB) *</label>
                  <input type="number" value={newProbMemoryLimit} onChange={e => setNewProbMemoryLimit(Number(e.target.value))} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:ring-primary focus:border-primary text-brand-blue" />
                </div>
              </div>

              <div className="flex flex-col gap-3">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Hints</label>
                  <button
                    type="button"
                    onClick={() => setNewProbHints(prev => [...prev, ''])}
                    className="px-3 py-1.5 bg-primary/10 hover:bg-primary/20 text-primary text-xs font-bold rounded-lg transition-colors flex items-center gap-1"
                  >
                    <span className="material-symbols-outlined text-[14px]">add</span> Add Hint
                  </button>
                </div>
                {newProbHints.map((hint, idx) => (
                  <div key={idx} className="flex items-center gap-2">
                    <input
                      type="text"
                      value={hint}
                      onChange={e => setNewProbHints(prev => prev.map((h, i) => i === idx ? e.target.value : h))}
                      className="flex-1 bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-medium focus:ring-primary focus:border-primary text-brand-blue"
                      placeholder={`Hint ${idx + 1}...`}
                    />
                    {newProbHints.length > 1 && (
                      <button
                        type="button"
                        onClick={() => setNewProbHints(prev => prev.filter((_, i) => i !== idx))}
                        className="text-slate-400 hover:text-red-500 transition-colors p-2"
                      >
                        <span className="material-symbols-outlined text-[18px]">delete</span>
                      </button>
                    )}
                  </div>
                ))}
              </div>

              {/* TESTCASES SECTION */}
              <div className="flex flex-col gap-4 mt-4 border-t border-slate-200 pt-6">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <h4 className="font-display font-black text-lg text-brand-blue uppercase tracking-wider">Test Cases {newProbIsPublic && '*'}</h4>
                    <div className="flex bg-slate-100 rounded-lg p-1 shadow-inner">
                      <button 
                        type="button" 
                        onClick={() => setTestCaseGenerationMode('manual')}
                        className={`px-4 py-1.5 text-xs font-bold rounded-md transition-all duration-300 ${testCaseGenerationMode === 'manual' ? 'bg-white text-primary shadow-sm transform scale-100' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'}`}
                      >
                        Manual Input
                      </button>
                      <button 
                        type="button" 
                        onClick={() => setTestCaseGenerationMode('generate')}
                        className={`px-4 py-1.5 text-xs font-bold rounded-md transition-all duration-300 flex items-center gap-1 ${testCaseGenerationMode === 'generate' ? 'bg-gradient-to-r from-orange-100 to-orange-50 text-primary border border-orange-200 shadow-sm transform scale-100' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-200/50'}`}
                      >
                        <span className="material-symbols-outlined text-[14px]">auto_awesome</span> Auto Generate
                      </button>
                    </div>
                  </div>
                  {testCaseGenerationMode === 'manual' && (
                    <button type="button" onClick={() => setTestcasesList(prev => [...prev, { problemId: editingProblemId || 0, inputData: '', expectedOutput: '', orderIndex: prev.length + 1 }])} className="px-3 py-1.5 bg-primary/10 hover:bg-primary/20 text-primary text-xs font-bold rounded-lg transition-colors flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">add</span> Add Test Case
                    </button>
                  )}
                </div>
                
                {testCaseGenerationMode === 'generate' ? (
                  <div className="flex flex-col gap-4 bg-gradient-to-b from-slate-50 to-white border border-slate-200 rounded-xl p-5 shadow-sm animate-fade-in relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-32 h-32 bg-orange-500/5 rounded-full blur-2xl -mr-10 -mt-10 pointer-events-none"></div>
                    <div className="flex items-center justify-between relative z-10">
                      <div className="flex items-center gap-2">
                        <div className="w-8 h-8 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                          <span className="material-symbols-outlined text-sm">code_blocks</span>
                        </div>
                        <p className="text-xs font-medium text-slate-600">Write code to generate test cases. This code will run on the server.</p>
                      </div>
                      <div className="flex items-center gap-3 bg-white px-3 py-1.5 rounded-lg border border-slate-200 shadow-sm">
                        <label className="text-xs font-black text-brand-blue uppercase tracking-wider">Language:</label>
                        <select 
                          value={generatorLanguage}
                          onChange={(e) => {
                            setGeneratorLanguage(e.target.value);
                            setGeneratorCode(GENERATOR_TEMPLATES[e.target.value] || '');
                          }}
                          className="bg-transparent text-sm font-bold text-primary focus:outline-none cursor-pointer"
                        >
                          <option value="c">C</option>
                          <option value="cpp">C++</option>
                          <option value="java">Java</option>
                          <option value="python">Python</option>
                          <option value="csharp">C#</option>
                        </select>
                      </div>
                    </div>
                    <div className="w-full h-[320px] border border-slate-200 rounded-xl overflow-hidden shadow-inner bg-white relative group">
                      <Editor
                        height="100%"
                        defaultLanguage={generatorLanguage === 'c' || generatorLanguage === 'cpp' ? 'cpp' : generatorLanguage === 'csharp' ? 'csharp' : generatorLanguage}
                        language={generatorLanguage === 'c' || generatorLanguage === 'cpp' ? 'cpp' : generatorLanguage === 'csharp' ? 'csharp' : generatorLanguage}
                        theme="vs-light"
                        value={generatorCode}
                        onChange={(value) => setGeneratorCode(value || '')}
                        options={{
                          minimap: { enabled: false },
                          fontSize: 13,
                          lineHeight: 24,
                          padding: { top: 16, bottom: 16 },
                          fontFamily: "'JetBrains Mono', 'Fira Code', Consolas, monospace",
                          scrollBeyondLastLine: false,
                          smoothScrolling: true,
                          cursorBlinking: "smooth",
                          stickyScroll: { enabled: false },
                          automaticLayout: true
                        }}
                      />
                    </div>
                    <div className="flex flex-col gap-3 relative z-10">
                      <div className="flex justify-end items-center">
                        <button 
                          type="button" 
                          onClick={handleRunAndGenerateTestcases} 
                          disabled={generateLoading}
                          className={`px-5 py-2.5 ${generateLoading ? 'bg-slate-400 cursor-not-allowed' : 'bg-gradient-to-r from-orange-500 to-primary hover:from-orange-600 hover:to-primary-dark hover:scale-[1.02] hover:-translate-y-0.5 shadow-md hover:shadow-lg'} text-white text-sm font-black rounded-xl transition-all duration-300 flex items-center gap-2`}
                        >
                          {generateLoading ? (
                            <>
                              <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                              Generating...
                            </>
                          ) : (
                            <>
                              <span className="material-symbols-outlined text-base">play_arrow</span>
                              Run & Generate Testcases
                            </>
                          )}
                        </button>
                      </div>
                      {generateError && (
                        <div className="bg-red-50/80 backdrop-blur-sm border border-red-200 text-red-600 rounded-xl p-4 animate-fade-in shadow-sm relative overflow-hidden">
                          <p className="text-xs font-black mb-1 flex items-center gap-1.5 uppercase tracking-wider">
                            <span className="material-symbols-outlined text-[16px]">error</span> Generation Error
                          </p>
                          <pre className="text-[12px] whitespace-pre-wrap font-mono overflow-x-auto text-red-800 bg-white/50 p-3 rounded-lg mt-2 border border-red-100">{generateError}</pre>
                        </div>
                      )}
                    </div>
                  </div>
                ) : testcasesList.length === 0 ? (
                  <div className="text-center py-6 border-2 border-dashed border-slate-200 rounded-xl">
                    <p className="text-xs text-text-muted font-bold">No test cases added yet.</p>
                  </div>
                ) : (

                  <div className="flex flex-col gap-4">
                    {testcasesList.map((tc, idx) => (
                      <div key={idx} className="bg-slate-50 border border-slate-200 rounded-xl p-4 flex flex-col gap-3">
                        <div className="flex items-start justify-between">
                          <span className="font-display font-black text-brand-blue text-xs uppercase">Test Case {idx + 1}</span>
                          <div className="flex items-center gap-3">
                            <label className="flex items-center gap-1.5 text-xs font-bold text-slate-600 cursor-pointer">
                              <input type="checkbox" checked={tc.isHidden} onChange={(e) => setTestcasesList(prev => prev.map((item, i) => i === idx ? { ...item, isHidden: e.target.checked } : item))} className="rounded text-primary focus:ring-primary" />
                              Hidden
                            </label>
                            <button type="button" onClick={() => setTestcasesList(prev => prev.filter((_, i) => i !== idx))} className="text-slate-400 hover:text-red-500 transition-colors">
                              <span className="material-symbols-outlined text-sm">delete</span>
                            </button>
                          </div>
                        </div>
                        <div className="grid grid-cols-2 gap-3">
                          <div className="flex flex-col gap-1">
                            <label className="text-[10px] font-bold text-text-muted uppercase tracking-wider">Input</label>
                            <textarea value={tc.inputData} onChange={(e) => setTestcasesList(prev => prev.map((item, i) => i === idx ? { ...item, inputData: e.target.value } : item))} className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-xs font-medium focus:ring-primary focus:border-primary text-brand-blue resize-none h-16" placeholder="e.g. [1, 2, 3]" />
                          </div>
                          <div className="flex flex-col gap-1">
                            <label className="text-[10px] font-bold text-text-muted uppercase tracking-wider">Expected Output</label>
                            <textarea value={tc.expectedOutput || ''} onChange={(e) => setTestcasesList(prev => prev.map((item, i) => i === idx ? { ...item, expectedOutput: e.target.value } : item))} className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-xs font-medium focus:ring-primary focus:border-primary text-brand-blue resize-none h-16" placeholder="e.g. [3, 2, 1]" />
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Starter Templates */}
              <div className="flex flex-col gap-1 border border-slate-200/60 rounded-2xl p-4 bg-slate-50/50 mt-4">
                <div className="flex justify-between items-center mb-2">
                  <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Starter Code Templates (Optional)</label>
                  <div className="flex gap-1.5">
                    {(['C', 'C++', 'Java', 'Python 3', 'C#'] as const).map(lang => (
                      <button key={lang} type="button" onClick={() => setStarterActiveTab(lang)} className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${starterActiveTab === lang ? 'bg-primary text-white shadow-sm' : 'bg-white border border-slate-200 text-slate-500 hover:bg-slate-100'}`}>
                        {lang}
                      </button>
                    ))}
                  </div>
                </div>
                {starterActiveTab === 'C' && <textarea rows={8} value={newProbStarterC} onChange={e => setNewProbStarterC(e.target.value)} className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm font-mono focus:ring-primary focus:border-primary text-brand-blue resize-y" placeholder="void solve() {\n}" />}
                {starterActiveTab === 'C++' && <textarea rows={8} value={newProbStarterCpp} onChange={e => setNewProbStarterCpp(e.target.value)} className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm font-mono focus:ring-primary focus:border-primary text-brand-blue resize-y" placeholder="class Solution {\npublic:\n    void solve() {\n    }\n};" />}
                {starterActiveTab === 'Java' && <textarea rows={8} value={newProbStarterJava} onChange={e => setNewProbStarterJava(e.target.value)} className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm font-mono focus:ring-primary focus:border-primary text-brand-blue resize-y" placeholder="class Solution {\n    public void solve() {\n    }\n}" />}
                {starterActiveTab === 'Python 3' && <textarea rows={8} value={newProbStarterPython} onChange={e => setNewProbStarterPython(e.target.value)} className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm font-mono focus:ring-primary focus:border-primary text-brand-blue resize-y" placeholder="class Solution:\n    def solve(self):\n        pass" />}
                {starterActiveTab === 'C#' && <textarea rows={8} value={newProbStarterCsharp} onChange={e => setNewProbStarterCsharp(e.target.value)} className="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-sm font-mono focus:ring-primary focus:border-primary text-brand-blue resize-y" placeholder="public class Solution {\n    public void Solve() {\n    }\n}" />}
              </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-brand-blue uppercase tracking-wider">Solution Code</label>
                <textarea rows={12} value={newProbSolutions} onChange={e => setNewProbSolutions(e.target.value)} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-mono focus:ring-primary focus:border-primary text-brand-blue resize-y" placeholder="Sample solution code..." />
              </div>

              <div className="flex items-center gap-3 mt-2 bg-slate-50 p-4 rounded-xl border border-slate-200">
                <input type="checkbox" checked={newProbIsPublic} onChange={e => setNewProbIsPublic(e.target.checked)} className="rounded text-primary border-slate-300 w-5 h-5" />
                <label className="text-sm font-bold text-brand-blue">Make this problem public immediately</label>
              </div>

              <div className="flex gap-4 mt-6">
                <button type="submit" className="flex-1 bg-gradient-to-r from-primary to-primary-hover text-white font-black text-sm py-4 rounded-xl shadow-lg hover:shadow-xl hover:-translate-y-0.5 transition-all">
                  {mode === 'edit' ? "Save Problem & Testcases" : "Create Problem & Testcases"}
                </button>
              </div>

            </form>

      {alertData && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-brand-blue/40 backdrop-blur-sm p-4 animate-fade-in">
          <div className="bg-white rounded-2xl shadow-2xl border border-slate-100 w-full max-w-sm overflow-hidden flex flex-col">
            <div className={`px-6 py-4 flex items-center gap-3 text-white ${alertData.type === 'error' ? 'bg-red-500' : alertData.type === 'success' ? 'bg-emerald-500' : 'bg-primary'}`}>
              <span className="material-symbols-outlined text-2xl">
                {alertData.type === 'error' ? 'error' : alertData.type === 'success' ? 'check_circle' : 'info'}
              </span>
              <h3 className="font-display font-black text-lg">
                {alertData.type === 'error' ? 'Error' : alertData.type === 'success' ? 'Success' : 'Notification'}
              </h3>
            </div>
            <div className="p-6">
              <p className="text-brand-blue font-medium leading-relaxed">{alertData.msg}</p>
            </div>
            <div className="px-6 pb-6 flex justify-end">
              <button 
                onClick={() => setAlertData(null)}
                className="px-6 py-2.5 bg-slate-100 hover:bg-slate-200 text-brand-blue font-bold text-sm rounded-xl transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
    </div>
  );
};
