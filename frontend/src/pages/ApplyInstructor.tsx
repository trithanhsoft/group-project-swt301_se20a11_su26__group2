import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';

interface ApplicationStatusResponse {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  cvUrl: string;
  introduction: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'AI_REJECTED';
  adminNote?: string;
  aiScore?: number;
  aiSummary?: string;
  aiSpecialization?: string;
  aiTechnologies?: string;
  aiExperienceYears?: number;
  aiStrengths?: string;
  aiWeaknesses?: string;
  aiRecommendation?: string;
  createdAt: string;
}

export const ApplyInstructor: React.FC = () => {
  const { user, refreshAuth } = useApp();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState<string>('');
  const [major, setMajor] = useState<string>('Backend Developer');
  const [bio, setBio] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [successMsg, setSuccessMsg] = useState<string>('');
  const [currentApp, setCurrentApp] = useState<ApplicationStatusResponse | null>(null);

  const BASE_URL = 'http://localhost:8080/nonstopcoding';

  const fetchApplicationStatus = async () => {
    try {
      const response = await fetch(`${BASE_URL}/instructor-applications/my-status`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      if (response.ok) {
        const data = await response.json();
        if (data && data.result) {
          setCurrentApp(data.result);
        } else {
          setCurrentApp(null);
        }
      }
    } catch (err) {
      console.error('Error fetching application status:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      fetchApplicationStatus();
    } else {
      setLoading(false);
    }
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName.trim() || !major.trim() || !bio.trim()) {
      setErrorMsg('Please fill out all the fields.');
      return;
    }

    if (bio.trim().length < 50) {
      setErrorMsg('Bio introduction must be at least 50 characters long.');
      return;
    }

    setSubmitting(true);
    setErrorMsg('');
    setSuccessMsg('');

    try {
      const response = await fetch(`${BASE_URL}/instructor-applications/apply`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          fullName,
          major,
          bio
        }),
        credentials: 'include',
      });

      const data = await response.json();

      if (response.ok && data.code === 1000) {
        setSuccessMsg('Successfully registered as an instructor!');
        try {
          await refreshAuth();
        } catch (refreshErr) {
          console.error('Failed to refresh token after instructor registration:', refreshErr);
        }
        // Refresh status immediately
        await fetchApplicationStatus();
      } else {
        setErrorMsg(data.message || 'Failed to register.');
      }
    } catch (err) {
      console.error('Error submitting application:', err);
      setErrorMsg('Server connection error. Please try again later.');
    } finally {
      setSubmitting(false);
    }
  };

  if (!user) {
    return (
      <div className="flex-grow max-w-md mx-auto my-12 p-8 text-center bg-surface border border-gray-150 rounded-2xl shadow-sm relative z-10">
        <span className="material-symbols-outlined text-red-500 text-5xl mb-4">lock</span>
        <h3 className="font-display font-black text-xl text-brand-blue mb-2">Access Denied</h3>
        <p className="font-body text-sm text-text-muted mb-6">You must log in to apply as an instructor.</p>
        <Link to="/login" className="bg-primary hover:bg-primary-hover text-white font-bold text-sm px-6 py-3 rounded-xl transition-all shadow-md">
          Log In
        </Link>
      </div>
    );
  }

  return (
    <div className="flex-grow max-w-[1280px] w-full mx-auto px-6 md:px-16 py-12 flex flex-col gap-8 text-left relative z-10">
      
      {/* Title Header */}
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-black font-display text-brand-blue tracking-tight">Become an Instructor</h1>
        <p className="text-sm text-text-muted max-w-2xl">
          Share your coding knowledge with the community, build your personal brand, and earn an attractive revenue share of up to 70% from your courses.
        </p>
      </div>

      {loading ? (
        <div className="flex items-center justify-center p-20 bg-white border border-gray-100 rounded-2xl shadow-sm">
          <span className="material-symbols-outlined animate-spin text-primary text-4xl">sync</span>
          <span className="ml-3 font-semibold text-text-muted text-sm">Loading application status...</span>
        </div>
      ) : currentApp && currentApp.status === 'PENDING' ? (
        /* Status Card: PENDING */
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          <div className="lg:col-span-8 bg-white rounded-xl shadow-[0_4px_20px_rgba(26,54,93,0.06)] p-6 md:p-8 border border-gray-100 flex flex-col gap-6">
            
            <div className="flex items-start justify-between border-b border-gray-100 pb-4">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-amber-500 text-3xl icon-fill animate-pulse">pending</span>
                <div>
                  <h3 className="text-lg font-bold text-brand-blue">Application Pending Review</h3>
                  <p className="text-xs text-text-muted mt-0.5">Submitted Date: {new Date(currentApp.createdAt).toLocaleDateString('vi-VN')} {new Date(currentApp.createdAt).toLocaleTimeString('vi-VN')}</p>
                </div>
              </div>
              <button 
                onClick={fetchApplicationStatus} 
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-gray-200 text-text-muted hover:text-primary hover:border-primary text-xs font-semibold transition-all"
              >
                <span className="material-symbols-outlined text-sm">refresh</span> Refresh Status
              </button>
            </div>

            <div className="flex flex-col gap-4">
              {currentApp.cvUrl && currentApp.cvUrl !== 'self_registered' && (
                <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                  <span className="text-xs font-bold text-text-muted uppercase tracking-wider">CV Profile (PDF)</span>
                  <a href={currentApp.cvUrl} target="_blank" rel="noreferrer" className="flex items-center gap-2 text-primary hover:underline font-semibold mt-2 text-sm">
                    <span className="material-symbols-outlined text-sm text-primary">picture_as_pdf</span> View Submitted CV
                  </a>
                </div>
              )}
              
              <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                <span className="text-xs font-bold text-text-muted uppercase tracking-wider">Approval Status</span>
                <div className="flex items-center gap-2 mt-2">
                  <span className="px-2.5 py-1 rounded-full bg-amber-50 text-amber-600 border border-amber-200 text-xs font-bold uppercase">Pending Admin Review</span>
                </div>
              </div>

              <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                <span className="text-xs font-bold text-text-muted uppercase tracking-wider block mb-2">Self Introduction</span>
                <p className="text-sm text-text-main leading-relaxed whitespace-pre-wrap">{currentApp.introduction}</p>
              </div>
            </div>
          </div>

          {/* Right Info Column */}
          <div className="lg:col-span-4 flex flex-col gap-6">
            <div className="bg-brand-blue text-white rounded-xl p-6 relative overflow-hidden shadow-md">
              <h3 className="font-bold text-md border-b border-white/20 pb-3 mb-3 flex items-center gap-2">
                <span className="material-symbols-outlined text-primary icon-fill">info</span> Process Details
              </h3>
              <ul className="text-sm text-white/80 space-y-3">
                <li>1. Fill out your name, specialization, and professional bio.</li>
                <li>2. Upon submission, your profile is approved instantly.</li>
                <li>3. Access the Instructor Dashboard immediately to start creating courses.</li>
              </ul>
            </div>
          </div>
        </div>
      ) : currentApp && currentApp.status === 'APPROVED' ? (
        /* Status Card: APPROVED */
        <div className="bg-white rounded-xl shadow-[0_4px_20px_rgba(26,54,93,0.08)] p-12 border border-gray-150 text-center flex flex-col items-center justify-center gap-4 max-w-2xl mx-auto w-full relative z-10">
          <div className="w-16 h-16 rounded-full bg-green-100 text-green-600 flex items-center justify-center border border-green-200 shadow-sm">
            <span className="material-symbols-outlined text-4xl icon-fill">check_circle</span>
          </div>
          <h3 className="text-2xl font-black text-brand-blue tracking-tight">
            {currentApp.cvUrl === 'self_registered' ? 'Registered Successfully!' : 'Application Approved Successfully!'}
          </h3>
          <p className="text-sm text-text-muted max-w-md leading-relaxed">
            {currentApp.cvUrl === 'self_registered' 
              ? 'Congratulations! You are now registered as an Instructor on Nonstop Coding.'
              : 'Congratulations! The Admin has approved your application. You now have Instructor privileges on Nonstop Coding.'}
          </p>
          {currentApp.adminNote && currentApp.cvUrl !== 'self_registered' && (
            <div className="bg-green-50/50 border border-green-100 rounded-xl p-4 w-full text-left my-2">
              <span className="text-xs font-bold text-green-700 block mb-1">Notes from Admin:</span>
              <p className="text-sm text-green-800 italic">"{currentApp.adminNote}"</p>
            </div>
          )}
          <div className="flex gap-4 w-full justify-center pt-4 border-t border-gray-100 mt-4">
            <button 
              onClick={() => {
                navigate('/instructor');
              }} 
              className="bg-primary hover:bg-primary-hover text-white font-extrabold text-sm px-8 py-3 rounded-xl transition-all shadow-md flex items-center gap-2"
            >
              <span>Go to Instructor Dashboard</span>
              <span className="material-symbols-outlined text-sm">arrow_forward</span>
            </button>
          </div>
        </div>
      ) : (
        /* Form: Become Instructor Registration */
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          
          {/* Submission Form */}
          <div className="lg:col-span-8 bg-white rounded-xl shadow-[0_4px_20px_rgba(26,54,93,0.06)] p-6 md:p-8 border border-gray-100">
            {successMsg && (
              <div className="bg-green-50 border border-green-200 text-green-600 p-4 rounded-xl font-bold flex items-center gap-2 mb-6 text-sm">
                <span className="material-symbols-outlined text-[20px]">check_circle</span>
                {successMsg}
              </div>
            )}

            {errorMsg && (
              <div className="bg-red-50 border border-red-200 text-red-600 p-4 rounded-xl font-bold flex items-center gap-2 mb-6 text-sm">
                <span className="material-symbols-outlined text-[20px]">error</span>
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleSubmit} className="flex flex-col gap-6">
              
              <div className="flex flex-col gap-2">
                <label className="text-sm font-bold text-brand-blue" htmlFor="fullName">Full Name</label>
                <input
                  className="w-full bg-white border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  id="fullName"
                  name="fullName"
                  placeholder="Enter your full name..."
                  required
                  type="text"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                />
              </div>

              <div className="flex flex-col gap-2">
                <label className="text-sm font-bold text-brand-blue" htmlFor="major">Professional Specialization</label>
                <select
                  className="w-full bg-white border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
                  id="major"
                  name="major"
                  required
                  value={major}
                  onChange={(e) => setMajor(e.target.value)}
                >
                  <option value="Backend Developer">Backend Developer</option>
                  <option value="Frontend Developer">Frontend Developer</option>
                  <option value="Full Stack Developer">Full Stack Developer</option>
                  <option value="Mobile Developer">Mobile Developer</option>
                  <option value="AI/ML Engineer">AI/ML Engineer</option>
                  <option value="DevOps Engineer">DevOps Engineer</option>
                  <option value="Data Engineer">Data Engineer</option>
                  <option value="Cyber Security Specialist">Cyber Security Specialist</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              <div className="flex flex-col gap-2">
                <label className="text-sm font-bold text-brand-blue" htmlFor="bio">Introduce Yourself & Experience (Bio)</label>
                <textarea
                  className="w-full bg-white border border-gray-300 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all min-h-[160px]"
                  id="bio"
                  name="bio"
                  placeholder="Introduce your programming experience, projects you have completed, and the skills you want to teach on Nonstop Coding..."
                  required
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                />
                <p className="text-xs text-text-muted mt-1">Minimum 50 characters. This profile bio will be displayed to students on your course pages.</p>
              </div>

              <div className="pt-4 border-t border-gray-150 mt-2">
                <button
                  disabled={submitting}
                  className="w-full bg-primary hover:bg-primary-hover text-white py-3.5 rounded-xl transition-all shadow-md hover:shadow-lg flex justify-center items-center gap-2 font-bold text-sm disabled:opacity-50"
                  type="submit"
                >
                  {submitting ? (
                    <>
                      <span className="material-symbols-outlined animate-spin text-sm">sync</span>
                      <span>Registering your profile...</span>
                    </>
                  ) : (
                    <>
                      <span>Register as Instructor</span>
                      <span className="material-symbols-outlined text-sm">send</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>

          {/* Right Info Column */}
          <div className="lg:col-span-4 flex flex-col gap-6">
            <div className="bg-brand-blue text-white rounded-xl p-6 relative overflow-hidden shadow-md">
              <h3 className="font-bold text-md border-b border-white/20 pb-3 mb-3 flex items-center gap-2">
                <span className="material-symbols-outlined text-primary icon-fill">school</span> Become an Instructor
              </h3>
              <p className="text-xs text-white/70 leading-relaxed mb-4">
                Nonstop Coding seeks high-caliber experts passionate about sharing technology with the younger generation.
              </p>
              <ul className="text-xs text-white/80 space-y-3">
                <li className="flex gap-2">
                  <span className="material-symbols-outlined text-primary text-sm shrink-0">check_circle</span>
                  <span>Get an attractive revenue share of up to <strong>70%</strong>.</span>
                </li>
                <li className="flex gap-2">
                  <span className="material-symbols-outlined text-primary text-sm shrink-0">check_circle</span>
                  <span>Leverage the Coding Arena ecosystem with auto-grading for students.</span>
                </li>
                <li className="flex gap-2">
                  <span className="material-symbols-outlined text-primary text-sm shrink-0">check_circle</span>
                  <span>Freely design your course structures and pricing.</span>
                </li>
              </ul>
            </div>

            <div className="bg-white border border-gray-200 rounded-xl p-5 flex items-start gap-4 shadow-sm">
              <span className="material-symbols-outlined text-primary mt-1">verified_user</span>
              <div>
                <h4 className="font-bold text-brand-blue mb-1 text-sm">Instant Professional Onboarding</h4>
                <p className="text-[11px] text-text-muted leading-relaxed">
                  Nonstop Coding allows immediate instructor registration. Fill out your specialization and experience bio, submit, and you can start creating courses right away.
                </p>
              </div>
            </div>
          </div>

        </div>
      )}

    </div>
  );
};
