import React from 'react';

interface EmptyStateProps {
  icon: string;
  title: string;
  description: string;
  themeColor: 'primary' | 'green' | 'blue';
  action?: {
    label: string;
    onClick: () => void;
  };
}

export const EmptyState: React.FC<EmptyStateProps> = ({ icon, title, description, themeColor, action }) => {
  const colorMap = {
    primary: {
      bg: 'bg-primary-light/50',
      text: 'text-primary',
      btnBg: 'bg-primary hover:bg-primary-hover text-white',
    },
    green: {
      bg: 'bg-brand-green-light',
      text: 'text-brand-green',
      btnBg: 'bg-brand-green hover:bg-brand-green-hover text-white',
    },
    blue: {
      bg: 'bg-blue-50',
      text: 'text-brand-blue-light',
      btnBg: 'bg-brand-blue hover:bg-brand-blue-light text-white',
    },
  };

  const colors = colorMap[themeColor] || colorMap.primary;

  return (
    <div className="w-full flex flex-col items-center justify-center p-8 bg-surface rounded-2xl border border-dashed border-outline-variant shadow-sm transition-all duration-300 hover:shadow-md">
      <div className={`w-16 h-16 ${colors.bg} ${colors.text} rounded-full flex items-center justify-center mb-4 transition-transform duration-300 hover:scale-105`}>
        <span className="material-symbols-outlined text-3xl">{icon}</span>
      </div>
      <h3 className="text-base font-bold text-text-main mb-1">{title}</h3>
      <p className="text-xs text-text-muted max-w-sm text-center leading-relaxed">
        {description}
      </p>
      {action && (
        <button
          onClick={action.onClick}
          className={`mt-4 px-5 py-2 ${colors.btnBg} text-xs font-black rounded-xl transition-all shadow-sm`}
        >
          {action.label}
        </button>
      )}
    </div>
  );
};

