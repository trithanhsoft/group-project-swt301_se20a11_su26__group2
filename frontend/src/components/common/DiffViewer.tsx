import React, { useMemo } from 'react';
import * as Diff from 'diff';

interface DiffViewerProps {
  oldText: string;
  newText: string;
  mode?: 'words' | 'lines';
}

const DiffViewer: React.FC<DiffViewerProps> = ({ oldText, newText, mode = 'words' }) => {
  const diffResult = useMemo(() => {
    const oldStr = oldText || '';
    const newStr = newText || '';
    return mode === 'lines' 
      ? Diff.diffLines(oldStr, newStr)
      : Diff.diffWords(oldStr, newStr);
  }, [oldText, newText, mode]);

  return (
    <div className="font-mono text-sm whitespace-pre-wrap break-words border border-slate-200 rounded-lg p-4 bg-slate-50 leading-relaxed overflow-auto max-h-[500px]">
      {diffResult.map((part, index) => {
        let className = 'text-slate-700';
        if (part.added) {
          className = 'bg-green-200 text-green-900 px-0.5 rounded-sm font-medium';
        } else if (part.removed) {
          className = 'bg-red-200 text-red-900 px-0.5 rounded-sm line-through opacity-70';
        }
        
        return (
          <span key={index} className={className}>
            {part.value}
          </span>
        );
      })}
    </div>
  );
};

export default DiffViewer;
