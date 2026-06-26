import React, { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { fetchCourseDetail, fetchCourseCurriculum, fetchCourseReviews, submitCourseReview, type CourseDetailResponse, type CurriculumChapterResponse, type CourseReviewStatsResponse } from '../services/courseService';

export const CourseDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [course, setCourse] = useState<CourseDetailResponse | null>(null);
  const [curriculum, setCurriculum] = useState<CurriculumChapterResponse[]>([]);
  const [reviewsStats, setReviewsStats] = useState<CourseReviewStatsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Tab Switching State
  const [activeTab, setActiveTab] = useState<'about' | 'curriculum' | 'reviews'>('about');

  // Curriculum Accordion State
  const [openChapters, setOpenChapters] = useState<Record<number, boolean>>({});

  // Cart & Video Modal Interactive States
  const [successMessage, setSuccessMessage] = useState('');
  const [previewVideoUrl, setPreviewVideoUrl] = useState<string | null>(null);

  // Review State
  const [reviewFormStar, setReviewFormStar] = useState<number>(0);
  const [reviewFormContent, setReviewFormContent] = useState<string>('');
  const [isEditingReview, setIsEditingReview] = useState<boolean>(false);
  const [submittingReview, setSubmittingReview] = useState<boolean>(false);
  const [reviewError, setReviewError] = useState<string | null>(null);

  const { addToCart, user, cart } = useApp();
  const navigate = useNavigate();

  useEffect(() => {
    const loadDetail = async () => {
      if (!id) return;
      setLoading(true);
      setError(null);
      try {
        const [detailData, curriculumData, reviewsData] = await Promise.all([
          fetchCourseDetail(id),
          fetchCourseCurriculum(id).catch((err) => {
            console.error('Failed to load curriculum:', err);
            return [] as CurriculumChapterResponse[];
          }),
          fetchCourseReviews(id).catch((err) => {
            console.error('Failed to load reviews:', err);
            return null;
          }),
        ]);
        const sortedCurriculum = curriculumData
          .map(chapter => ({
            ...chapter,
            lessons: [...chapter.lessons].sort((a, b) => a.orderIndex - b.orderIndex)
          }))
          .sort((a, b) => a.orderIndex - b.orderIndex);

        setCourse(detailData);
        setCurriculum(sortedCurriculum);
        setReviewsStats(reviewsData);
      } catch (err: any) {
        console.error("Error fetching course detail", err);
        setError(err.message || 'Failed to load course details');
      } finally {
        setLoading(false);
      }
    };
    loadDetail();
  }, [id]);

  const isAddedToCart = id ? cart.includes(id) : false;

  useEffect(() => {
    if (curriculum && curriculum.length > 0) {
      setOpenChapters(prev => ({
        ...prev,
        [curriculum[0].id]: true
      }));
    }
  }, [curriculum]);

  const toggleChapter = (chapterId: number) => {
    setOpenChapters(prev => ({
      ...prev,
      [chapterId]: !prev[chapterId]
    }));
  };

  const handleAddToCart = () => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (course) {
      addToCart(course.id.toString());
      setSuccessMessage('Course added to cart successfully!');
      setTimeout(() => {
        setSuccessMessage('');
      }, 2500);
    }
  };

  const handleSubmitReview = async () => {
    if (!id || reviewFormStar === 0 || !reviewFormContent.trim()) {
      setReviewError('Please select a star rating and enter a review content.');
      return;
    }
    setSubmittingReview(true);
    setReviewError(null);
    try {
      await submitCourseReview(id, reviewFormStar, reviewFormContent);
      // Reload reviews
      const reviewsData = await fetchCourseReviews(id);
      setReviewsStats(reviewsData);
      setSuccessMessage('Review submitted successfully!');
      setIsEditingReview(false);
      setTimeout(() => setSuccessMessage(''), 2500);
    } catch (err: any) {
      setReviewError(err.message || 'An error occurred while submitting the review');
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleEditClick = () => {
    if (reviewsStats?.myReview) {
      setReviewFormStar(reviewsStats.myReview.star);
      setReviewFormContent(reviewsStats.myReview.content);
      setIsEditingReview(true);
    }
  };

  const parseList = (text?: string): string[] => {
    if (!text) return [];
    if (text.includes('#')) {
      return text.split('#').map(item => item.trim()).filter(Boolean);
    }
    return text.split('\n').map(line => line.replace(/^[-*•\d.]+\s*/, '').trim()).filter(Boolean);
  };

  const getInstructorAvatar = (url?: string) => {
    if (url && url.trim() !== '') return url;
    return 'https://ui-avatars.com/api/?name=' + encodeURIComponent(course?.instructorName || 'Instructor') + '&background=12284C&color=fff';
  };

  if (loading) {
    return (
      <div className="w-full min-h-[500px] flex flex-col items-center justify-center text-center p-12">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary mb-4"></div>
        <h3 className="text-base font-bold text-brand-blue">Loading Course Details...</h3>
        <p className="text-xs text-text-muted mt-1">Retrieving dynamic parameters from the server.</p>
      </div>
    );
  }

  if (error || !course) {
    return (
      <div className="flex-grow flex flex-col items-center justify-center p-8 mt-20">
        <span className="material-symbols-outlined text-6xl text-gray-300 mb-4">error_outline</span>
        <h2 className="text-2xl font-bold text-gray-700">Oops!</h2>
        <p className="text-sm text-red-600 mt-2">{error || 'Course not found'}</p>
        <button 
          onClick={() => navigate('/courses')}
          className="mt-6 bg-brand-blue text-white px-6 py-2 rounded-full font-bold hover:bg-brand-blue-light transition-colors"
        >
          Back to courses list
        </button>
      </div>
    );
  }

  const whatYouLearnList = parseList(course.whatYouLearn);
  const highlightsList = parseList(course.courseHighlight);
  const prerequisitesList = parseList(course.prerequisites);
  const audienceList = parseList(course.targetAudience);
  const benefitsList = parseList(course.completionBenefits);
  const techToolsList = course.technologyTool ? course.technologyTool.split(/[#,]/).map(s => s.trim()).filter(Boolean) : [];

  return (
    <div className="w-full text-left">
      <style>{`
        .active-tab {
          border-bottom: 2px solid #F36F21;
          color: #F36F21;
          font-weight: 700;
        }

        .bento-shadow {
          box-shadow: 0 4px 20px rgba(26, 54, 93, 0.08);
        }
      `}</style>

      {successMessage && (
        <div className="fixed top-20 right-8 bg-brand-green border border-brand-green/30 text-white p-4 rounded-xl z-50 font-bold flex items-center gap-2 animate-fade-in shadow-xl">
          <span className="material-symbols-outlined text-[20px] icon-fill">check_circle</span>
          {successMessage}
        </div>
      )}

      {previewVideoUrl && (
        <div 
          onClick={() => setPreviewVideoUrl(null)}
          className="fixed inset-0 z-[100] flex items-center justify-center bg-black/80 backdrop-blur-sm animate-fade-in"
        >
          <div 
            onClick={(e) => e.stopPropagation()}
            className="relative w-full max-w-3xl aspect-video bg-black rounded-2xl overflow-hidden shadow-2xl mx-4 flex items-center justify-center"
          >
            <button 
              onClick={() => setPreviewVideoUrl(null)}
              className="absolute top-4 right-4 bg-white/20 hover:bg-white/40 text-white rounded-full p-2 transition-all z-10 flex items-center justify-center"
            >
              <span className="material-symbols-outlined">close</span>
            </button>
            
            {previewVideoUrl === 'NO_VIDEO' ? (
              <div className="text-center text-white space-y-4 p-8">
                <span className="material-symbols-outlined text-6xl text-gray-500">videocam_off</span>
                <h3 className="text-2xl font-bold">No Preview Video Available</h3>
                <p className="text-gray-400 font-body">This content does not have a video preview, or it is currently under maintenance. Please enroll in the course to access all materials.</p>
              </div>
            ) : previewVideoUrl.includes('youtube.com') || previewVideoUrl.includes('youtu.be') ? (
              <iframe 
                className="w-full h-full"
                src={previewVideoUrl}
                title="Course Preview Video"
                frameBorder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              ></iframe>
            ) : (
              <video 
                className="w-full h-full object-contain bg-black"
                src={previewVideoUrl}
                controls
                autoPlay
              >
                Your browser does not support the video tag.
              </video>
            )}
          </div>
        </div>
      )}

      <div className="bg-brand-blue text-white pt-24 pb-32">
        <div className="max-w-[1440px] mx-auto px-4 md:px-16 flex flex-col lg:flex-row gap-12 text-left">
          <div className="w-full lg:w-[72%] space-y-6">
            <div className="flex gap-3">
              <span className="bg-primary-light text-primary border border-primary/30 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider">
                {course.categoryName || 'GENERAL'}
              </span>
              <span className="bg-brand-blue-light text-white border border-white/50 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider">
                {course.type || 'ONLINE'}
              </span>
            </div>
            <h1 className="text-4xl md:text-5xl font-bold leading-tight">{course.title}</h1>
            <p className="text-lg md:text-xl font-medium max-w-3xl text-gray-300 font-body leading-relaxed">
              {course.shortDescription}
            </p>
            <div className="flex flex-wrap gap-6 items-center pt-2">
              <div className="flex items-center gap-1">
                <span className="material-symbols-outlined text-yellow-400" style={{ fontVariationSettings: '"FILL" 1' }}>
                  star
                </span>
                <span className="font-bold text-white">{course.averageRating}</span>
                <span className="text-sm text-gray-300 ml-1">({course.totalReviews} ratings)</span>
              </div>
              <div className="flex items-center gap-1.5 text-gray-300">
                <span className="material-symbols-outlined text-sm">group</span>
                <span className="text-sm">{course.totalEnrolled.toLocaleString('en-US')} students</span>
              </div>
            </div>
            <div className="pt-4 flex items-center gap-4">
              <img
                alt="Instructor"
                className="w-12 h-12 rounded-full object-cover border-2 border-white/20"
                src={getInstructorAvatar(course.instructorAvatarUrl)}
              />
              <div>
                <p className="text-sm font-medium text-gray-300">
                  Created by <span className="text-white font-bold">{course.instructorName}</span>
                </p>
                <p className="text-sm text-gray-400">{course.instructorTitle || 'Instructor'}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <main className="max-w-[1440px] mx-auto px-4 md:px-16 pb-20 -mt-20 relative z-10">
        <div className="flex flex-col lg:flex-row gap-12 text-left">
          <div className="w-full lg:w-[72%] bg-surface rounded-2xl p-8 shadow-sm border border-gray-200">
            <div className="flex gap-8 border-b border-gray-200 mb-10 overflow-x-auto whitespace-nowrap" id="course-tabs">
              <button
                onClick={() => setActiveTab('about')}
                className={`pb-4 text-body-md transition-all ${activeTab === 'about' ? 'active-tab text-primary' : 'text-text-muted hover:text-primary'}`}
              >
                About
              </button>
              <button
                onClick={() => setActiveTab('curriculum')}
                className={`pb-4 text-body-md transition-all ${activeTab === 'curriculum' ? 'active-tab text-primary' : 'text-text-muted hover:text-primary'}`}
              >
                Curriculum
              </button>
              <button
                onClick={() => setActiveTab('reviews')}
                className={`pb-4 text-body-md transition-all ${activeTab === 'reviews' ? 'active-tab text-primary' : 'text-text-muted hover:text-primary'}`}
              >
                Reviews
              </button>
            </div>

            <div className="space-y-12">
              <div id="content-about" className={`space-y-12 transition-all duration-300 ${activeTab === 'about' ? '' : 'hidden'}`}>
                <section>
                  <h2 className="text-headline-md font-bold text-text-main mb-6 flex items-center gap-3">
                    <span className="w-1.5 h-8 bg-primary rounded-full"></span>Course Description
                  </h2>
                  <div className="text-body-lg text-text-muted leading-relaxed max-w-4xl whitespace-pre-line font-body">
                    {course.longDescription}
                  </div>
                </section>

                {whatYouLearnList.length > 0 && (
                  <section>
                    <h2 className="text-headline-md font-bold text-text-main mb-6 flex items-center gap-3">
                      <span className="w-1.5 h-8 bg-primary rounded-full"></span>What you'll learn
                    </h2>
                    <div className="grid md:grid-cols-2 gap-4">
                      {whatYouLearnList.map((item, idx) => (
                        <div key={idx} className="p-6 bg-surface-gray rounded-xl border border-gray-200 flex items-start gap-4 hover:border-brand-green transition-all">
                          <span className="material-symbols-outlined text-brand-green" style={{ fontVariationSettings: '"FILL" 1' }}>
                            check_circle
                          </span>
                          <p className="text-body-md font-semibold text-text-main font-body leading-relaxed">{item}</p>
                        </div>
                      ))}
                    </div>
                  </section>
                )}

                {highlightsList.length > 0 && (
                  <section>
                    <h2 className="text-headline-md font-bold text-text-main mb-6 flex items-center gap-3">
                      <span className="w-1.5 h-8 bg-primary rounded-full"></span>Course Highlights
                    </h2>
                    <div className="flex flex-wrap gap-4">
                      {highlightsList.map((item, idx) => (
                        <div key={idx} className="flex items-center gap-3 bg-surface-gray border border-gray-200 px-5 py-3 rounded-xl hover:border-primary transition-all">
                          <span className="material-symbols-outlined text-primary text-2xl">
                            {idx % 3 === 0 ? 'developer_mode' : idx % 3 === 1 ? 'all_inclusive' : 'person_celebrate'}
                          </span>
                          <span className="font-bold text-text-main font-body">{item}</span>
                        </div>
                      ))}
                    </div>
                  </section>
                )}

                {techToolsList.length > 0 && (
                  <section>
                    <h2 className="text-headline-md font-bold text-text-main mb-6 flex items-center gap-3">
                      <span className="w-1.5 h-8 bg-primary rounded-full"></span>Technologies {"&"} Tools
                    </h2>
                    <div className="flex flex-wrap gap-3">
                      {techToolsList.map((tool, idx) => (
                        <span key={idx} className="px-6 py-2 border border-gray-200 rounded-full text-text-muted font-medium hover:bg-gray-50 transition-colors cursor-default font-body">
                          {tool}
                        </span>
                      ))}
                    </div>
                  </section>
                )}

                {prerequisitesList.length > 0 && (
                  <section>
                    <h2 className="text-headline-md font-bold text-text-main mb-6 flex items-center gap-3">
                      <span className="w-1.5 h-8 bg-primary rounded-full"></span>Prerequisites
                    </h2>
                    <div className="border-l-4 border-primary bg-surface-gray p-6 rounded-r-xl">
                      <ul className="list-disc list-inside space-y-2 text-text-muted font-body">
                        {prerequisitesList.map((item, idx) => (
                          <li key={idx} className="leading-relaxed">{item}</li>
                        ))}
                      </ul>
                    </div>
                  </section>
                )}

                {audienceList.length > 0 && (
                  <section>
                    <h2 className="text-headline-md font-bold text-text-main mb-6 flex items-center gap-3">
                      <span className="w-1.5 h-8 bg-primary rounded-full"></span>Target Audience
                    </h2>
                    <div className="space-y-4">
                      {audienceList.map((item, idx) => (
                        <div key={idx} className="flex items-center gap-3">
                          <span className="material-symbols-outlined text-text-muted">
                            {idx % 3 === 0 ? 'school' : idx % 3 === 1 ? 'code_blocks' : 'terminal'}
                          </span>
                          <span className="text-text-muted font-semibold font-body">{item}</span>
                        </div>
                      ))}
                    </div>
                  </section>
                )}

                {benefitsList.length > 0 && (
                  <section>
                    <h2 className="text-headline-md font-bold text-text-main mb-6 flex items-center gap-3">
                      <span className="w-1.5 h-8 bg-primary rounded-full"></span>Completion Benefits
                    </h2>
                    <div className="p-6 bg-surface-gray rounded-2xl border border-primary/20 flex flex-col sm:flex-row flex-wrap gap-4 items-center">
                      <div className="flex flex-wrap gap-4 flex-1">
                        {benefitsList.map((item, idx) => (
                          <div key={idx} className="flex items-center gap-2 bg-surface px-4 py-2.5 rounded-xl border border-gray-200">
                            <span className="material-symbols-outlined text-brand-green text-lg">check_circle</span>
                            <span className="font-bold text-text-main font-body text-sm">{item}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </section>
                )}
              </div>

              <div id="content-curriculum" className={`space-y-12 transition-all duration-300 ${activeTab === 'curriculum' ? '' : 'hidden'}`}>
                <section className="space-y-6">
                  <div className="flex justify-between items-end mb-4">
                    <h2 className="text-headline-md font-bold text-text-main">Course Content</h2>
                    <p className="text-body-md text-text-muted font-medium font-body">
                      {course.totalLessons} lessons • {course.totalQuizzes} quizzes
                    </p>
                  </div>
                  {curriculum.length === 0 ? (
                    <div className="p-8 text-center bg-gray-50 border border-gray-100 rounded-lg">
                      <span className="material-symbols-outlined text-4xl text-gray-300 mb-2">construction</span>
                      <p className="font-semibold">Detailed curriculum is being updated.</p>
                      <p className="text-sm text-text-muted mt-1">Please check back later for the complete syllabus.</p>
                    </div>
                  ) : (
                    <div className="border border-gray-200 rounded-xl overflow-hidden divide-y divide-gray-200 bg-surface">
                      {curriculum.map((chapter) => {
                        const isOpen = !!openChapters[chapter.id];
                        return (
                          <div key={chapter.id} className="bg-surface-gray">
                            <div 
                              onClick={() => toggleChapter(chapter.id)}
                              className="p-4 flex justify-between items-center cursor-pointer select-none bg-surface-gray hover:bg-gray-50 transition-colors"
                            >
                              <div className="flex items-center gap-3">
                                <span 
                                  className="material-symbols-outlined text-text-main transition-transform duration-200"
                                  style={{ transform: isOpen ? 'rotate(0deg)' : 'rotate(-90deg)' }}
                                >
                                  expand_more
                                </span>
                                <h3 className="font-bold text-text-main">{chapter.title}</h3>
                              </div>
                              <span className="text-body-sm text-text-muted font-medium font-body">
                                {chapter.lessons.length} {chapter.lessons.length === 1 ? 'lesson' : 'lessons'}
                              </span>
                            </div>
                            {isOpen && (
                              <div className="bg-surface divide-y divide-gray-200">
                                {chapter.lessons.length === 0 ? (
                                  <div className="p-6 text-center text-text-muted text-sm font-body">
                                    No lessons available in this chapter.
                                  </div>
                                ) : (
                                  chapter.lessons.map((lesson) => (
                                    <div key={lesson.id} className="p-4 flex items-center justify-between hover:bg-gray-50 transition-colors">
                                      <div className="flex items-center gap-3">
                                        <span className="material-symbols-outlined text-brand-green text-[20px]">
                                          {lesson.type === 'video' ? 'play_circle' : lesson.type === 'coding' ? 'code' : 'description'}
                                        </span>
                                        <span className="text-body-md text-text-main font-body">
                                          {lesson.title}
                                        </span>
                                      </div>
                                      {lesson.isTrial && (
                                        <button 
                                          onClick={() => setPreviewVideoUrl(lesson.videoUrl || 'NO_VIDEO')}
                                          className="bg-primary text-white text-body-sm font-bold px-3 py-1 rounded hover:bg-primary-hover transition-all"
                                        >
                                          Preview
                                        </button>
                                      )}
                                    </div>
                                  ))
                                )}
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  )}
                </section>
              </div>

              {/* 3. REVIEWS SECTION */}
              <div id="content-reviews" className={`space-y-12 transition-all duration-300 ${activeTab === 'reviews' ? '' : 'hidden'}`}>
                <section className="space-y-8 animate-in fade-in duration-500">
                  <h2 className="text-headline-md font-bold text-text-main mb-6">Student Feedback</h2>
                  <div className="flex flex-col md:flex-row gap-10 items-start md:items-center p-8 bg-surface-gray border border-gray-200 rounded-2xl">
                    <div className="flex flex-col items-center gap-2 shrink-0">
                      <span className="text-6xl font-extrabold text-primary">{reviewsStats?.averageRating || course.averageRating || 0}</span>
                      <div className="flex text-yellow-400">
                        {Array.from({ length: 5 }).map((_, i) => (
                          <span key={i} className="material-symbols-outlined" style={{ fontVariationSettings: i < Math.round(reviewsStats?.averageRating || course.averageRating || 0) ? '"FILL" 1' : '' }}>star</span>
                        ))}
                      </div>
                      <span className="text-body-md font-bold text-text-main">Course Rating</span>
                    </div>
                    <div className="flex-1 w-full space-y-3">
                      {[5, 4, 3, 2, 1].map(star => {
                        const count = reviewsStats?.starDistribution?.[star] || 0;
                        const total = reviewsStats?.totalReviews || 1; // avoid division by 0
                        const percentage = reviewsStats?.totalReviews ? Math.round((count / total) * 100) : 0;
                        const isMyStar = reviewsStats?.myReview?.star === star;
                        
                        return (
                          <div key={star} className="flex items-center gap-4">
                            <div className="w-full bg-gray-200 rounded-full h-2.5 overflow-hidden">
                              <div className={`h-full ${isMyStar ? 'bg-[#F36F21]' : 'bg-primary'}`} style={{ width: `${percentage}%` }}></div>
                            </div>
                            <div className={`flex items-center gap-4 min-w-[160px] font-body ${isMyStar ? 'font-bold text-[#F36F21]' : ''}`}>
                              <div className="flex text-yellow-400">
                                {Array.from({ length: 5 }).map((_, i) => (
                                  <span key={i} className="material-symbols-outlined text-[18px]" style={{ fontVariationSettings: i < star ? '"FILL" 1' : '' }}>star</span>
                                ))}
                              </div>
                              <span className={`text-body-sm ${isMyStar ? 'font-bold' : 'font-semibold text-text-muted'}`}>{count}</span>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>

                  <div className="space-y-6 pt-6">
                    <h3 className="text-headline-sm font-bold text-text-main">Reviews</h3>

                    {/* Review Form Area */}
                    {user && course.enrolled && (!reviewsStats?.myReview || isEditingReview) && (
                      <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm mb-8">
                        <h4 className="font-bold text-lg mb-4">{isEditingReview ? 'Edit your review' : 'Review this course'}</h4>
                        
                        <div className="flex gap-2 mb-4 text-yellow-400">
                          {[1, 2, 3, 4, 5].map((star) => (
                            <span 
                              key={star} 
                              className="material-symbols-outlined text-3xl cursor-pointer hover:scale-110 transition-transform" 
                              style={{ fontVariationSettings: star <= reviewFormStar ? '"FILL" 1' : '"FILL" 0' }}
                              onClick={() => setReviewFormStar(star)}
                            >
                              star
                            </span>
                          ))}
                        </div>
                        
                        <textarea 
                          value={reviewFormContent}
                          onChange={(e) => setReviewFormContent(e.target.value)}
                          placeholder="Share your experience about this course..."
                          className="w-full p-4 border border-gray-300 rounded-xl focus:ring-2 focus:ring-primary focus:border-transparent outline-none resize-none min-h-[120px] mb-2"
                        />
                        
                        {reviewError && <p className="text-red-500 text-sm mb-4">{reviewError}</p>}
                        
                        <div className="flex justify-end gap-3">
                          {isEditingReview && (
                            <button 
                              onClick={() => setIsEditingReview(false)}
                              className="px-6 py-2 text-gray-600 font-bold hover:bg-gray-100 rounded-lg transition-colors"
                            >
                              Cancel
                            </button>
                          )}
                          <button 
                            onClick={handleSubmitReview}
                            disabled={submittingReview}
                            className="bg-primary text-white px-8 py-2 font-bold rounded-lg hover:bg-primary-hover transition-colors disabled:opacity-50"
                          >
                            {submittingReview ? 'Submitting...' : 'Submit Review'}
                          </button>
                        </div>
                      </div>
                    )}

                    {/* My Review Display */}
                    {user && reviewsStats?.myReview && !isEditingReview && (
                      <div className="bg-orange-50 border border-orange-100 p-6 rounded-xl mb-8 relative">
                        <div className="absolute top-6 right-6">
                          <button 
                            onClick={handleEditClick}
                            className="text-primary hover:text-primary-hover font-bold flex items-center gap-1 bg-white px-3 py-1.5 rounded-full border border-orange-200 shadow-sm"
                          >
                            <span className="material-symbols-outlined text-[18px]">edit</span> Edit
                          </button>
                        </div>
                        <div className="flex items-center gap-4 mb-3">
                          {reviewsStats.myReview.avatarUrl ? (
                            <img src={reviewsStats.myReview.avatarUrl} alt="Me" className="w-12 h-12 rounded-full object-cover shrink-0 border-2 border-white shadow-sm" />
                          ) : (
                            <div className="w-12 h-12 rounded-full bg-primary flex items-center justify-center text-white font-bold shrink-0 shadow-sm">
                              {(reviewsStats.myReview.displayName || 'You').substring(0, 2).toUpperCase()}
                            </div>
                          )}
                          <div>
                            <p className="font-bold text-text-main flex items-center gap-2">
                              {reviewsStats.myReview.displayName || 'You'}
                              <span className="bg-primary text-white text-[10px] px-2 py-0.5 rounded-full font-bold uppercase tracking-wider">YOURS</span>
                            </p>
                            <div className="flex text-yellow-400 scale-75 origin-left -ml-1">
                              {Array.from({ length: 5 }).map((_, i) => (
                                <span key={i} className="material-symbols-outlined" style={{ fontVariationSettings: i < reviewsStats!.myReview!.star ? '"FILL" 1' : '' }}>star</span>
                              ))}
                            </div>
                          </div>
                        </div>
                        <p className="text-body-md text-text-main leading-relaxed font-body mt-2">
                          {reviewsStats.myReview.content}
                        </p>
                      </div>
                    )}

                    <div className="divide-y divide-gray-200">
                      {reviewsStats?.reviews?.content?.length ? (
                        reviewsStats.reviews.content.filter(r => r.id !== reviewsStats.myReview?.id).map(review => (
                          <div key={review.id} className="py-8 flex flex-col sm:flex-row gap-6">
                            {review.avatarUrl ? (
                              <img src={review.avatarUrl} alt={review.displayName} className="w-12 h-12 rounded-full object-cover shrink-0" />
                            ) : (
                              <div className="w-12 h-12 rounded-full bg-brand-blue flex items-center justify-center text-white font-bold shrink-0">
                                {review.displayName.substring(0, 2).toUpperCase()}
                              </div>
                            )}
                            <div className="flex-1 space-y-3">
                              <div className="flex justify-between items-start">
                                <div>
                                  <p className="font-bold text-text-main">{review.displayName}</p>
                                  <div className="flex text-yellow-400 scale-75 origin-left -ml-1">
                                    {Array.from({ length: 5 }).map((_, i) => (
                                      <span key={i} className="material-symbols-outlined" style={{ fontVariationSettings: i < review.star ? '"FILL" 1' : '' }}>star</span>
                                    ))}
                                  </div>
                                </div>
                                <span className="text-body-sm text-text-muted font-body">
                                  {new Date(review.createdAt).toLocaleDateString()}
                                </span>
                              </div>
                              <p className="text-body-md text-text-main leading-relaxed font-body">
                                {review.content}
                              </p>
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className="py-8 text-center text-text-muted">
                          No reviews available yet.
                        </div>
                      )}
                    </div>
                  </div>
                </section>
              </div>
            </div>
          </div>

          {/* Sidebar (28%) */}
          <div className="w-full lg:w-[28%] -mt-[320px]">
            <div className="sticky top-24 bg-surface rounded-2xl shadow-xl border border-gray-200 overflow-hidden">
              {/* Video Preview */}
              <div 
                onClick={() => {
                  let url = 'NO_VIDEO';
                  if (curriculum) {
                    for (const chap of curriculum) {
                      const trial = chap.lessons.find(l => l.isTrial && l.videoUrl);
                      if (trial && trial.videoUrl) {
                        url = trial.videoUrl;
                        break;
                      }
                    }
                  }
                  setPreviewVideoUrl(url);
                }}
                className="relative w-full aspect-video group cursor-pointer"
              >
                <img
                  alt="Course Preview"
                  className="w-full h-full object-cover"
                  src={course.thumbnailUrl || "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800"}
                />
                <div className="absolute inset-0 bg-black/40 flex items-center justify-center group-hover:bg-black/50 transition-colors">
                  <div className="w-16 h-16 bg-white/90 rounded-full flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                    <span className="material-symbols-outlined text-primary text-4xl ml-1">play_arrow</span>
                  </div>
                </div>
                <div className="absolute bottom-4 left-0 w-full text-center">
                  <span className="text-white font-bold text-lg drop-shadow-md">Preview this course</span>
                </div>
              </div>
              <div className="p-6">
                {/* Pricing Section */}
                {!course.enrolled && (
                  <div className="flex items-end gap-3 mb-6">
                    {course.price === 0 ? (
                      <span className="text-3xl font-extrabold text-brand-green">Free</span>
                    ) : (
                      <span className="text-3xl font-extrabold text-text-main">
                        {course.price.toLocaleString('vi-VN')}đ
                      </span>
                    )}
                  </div>
                )}
                {/* Action Buttons */}
                <div className="space-y-3 mb-6">
                  {course.enrolled ? (
                    <button
                      onClick={() => navigate('/dashboard')}
                      className="w-full flex items-center justify-center gap-2 py-4 bg-primary text-white text-center font-bold rounded-xl shadow-md cursor-pointer hover:bg-primary-hover transition-all font-body"
                    >
                      <span>Continue Learning</span>
                      <span className="material-symbols-outlined text-[20px]">arrow_forward</span>
                    </button>
                  ) : isAddedToCart ? (
                    <Link
                      to="/shopping-cart"
                      className="w-full block py-4 bg-brand-blue hover:bg-brand-blue-light text-white text-center font-bold rounded-xl transition-all shadow-md font-body"
                    >
                      Go to Cart
                    </Link>
                  ) : (
                    <button
                      onClick={handleAddToCart}
                      className="w-full py-4 bg-primary text-white font-bold rounded-xl hover:bg-primary-hover active:scale-[0.98] transition-all shadow-md font-body"
                    >
                      Add to Cart
                    </button>
                  )}
                </div>
                {/* Course Info List */}
                <div className="space-y-4">
                  <h3 className="font-bold text-text-main mb-4 font-body">This course includes:</h3>
                  <div className="flex items-center gap-3 text-text-muted font-body">
                    <span className="material-symbols-outlined text-[20px]">description</span>
                    <span className="text-sm">{course.totalLessons} lessons</span>
                  </div>
                  <div className="flex items-center gap-3 text-text-muted font-body">
                    <span className="material-symbols-outlined text-[20px]">quiz</span>
                    <span className="text-sm">{course.totalQuizzes} quizzes</span>
                  </div>
                  <div className="flex items-center gap-3 text-text-muted font-body">
                    <span className="material-symbols-outlined text-[20px]">play_circle</span>
                    <span className="text-sm">{course.totalVideos} videos</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
