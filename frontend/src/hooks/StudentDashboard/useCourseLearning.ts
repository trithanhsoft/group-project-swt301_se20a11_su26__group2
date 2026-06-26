import { useState, useCallback } from 'react';
import { fetchCourseLearningDetail, fetchCourseLearningCurriculum, completeLesson, type LearningCurriculumChapterResponse } from '../../services/courseService';

export const useCourseLearning = () => {
  const [playerCourseId, setPlayerCourseId] = useState<number | null>(null);
  const [playerCourseTitle, setPlayerCourseTitle] = useState<string>('');
  const [playerCourseAuthor, setPlayerCourseAuthor] = useState<string>('');
  const [playerCourseProgress, setPlayerCourseProgress] = useState<string>('0%');
  const [playerLectureTitle, setPlayerLectureTitle] = useState<string>('');
  const [playerVideoThumbnail, setPlayerVideoThumbnail] = useState<string>('');
  const [playerVideoUrl, setPlayerVideoUrl] = useState<string>('');
  const [playerTheoryContent, setPlayerTheoryContent] = useState<string>('');
  const [learningChapters, setLearningChapters] = useState<LearningCurriculumChapterResponse[]>([]);
  const [selectedLessonId, setSelectedLessonId] = useState<number | null>(null);
  const [isPlayerLoading, setIsPlayerLoading] = useState<boolean>(false);
  const [playerActiveTab, setPlayerActiveTab] = useState<'overview' | 'qa' | 'exercises' | 'quiz'>('overview');
  const [curriculumSections, setCurriculumSections] = useState<Record<string, boolean>>({
    sec1: true,
    sec2: false,
    sec3: false
  });

  const refreshLearningProgress = useCallback(async (courseId: number | string) => {
    try {
      const detail = await fetchCourseLearningDetail(courseId);
      setPlayerCourseProgress(`${detail.progressPercentage}%`);

      const chapters = await fetchCourseLearningCurriculum(courseId);
      setLearningChapters(chapters);
    } catch (err) {
      console.error('Failed to refresh learning progress:', err);
    }
  }, []);

  const handleCompleteLesson = useCallback(async (e: React.MouseEvent, lessonId: number, courseId: number | null) => {
    e.stopPropagation();
    if (!courseId) return;

    setIsPlayerLoading(true);
    try {
      await completeLesson(courseId, lessonId);
      await refreshLearningProgress(courseId);
    } catch (err: any) {
      console.error('Failed to complete lesson:', err);
      alert(err.message || 'Không thể hoàn thành bài học');
    } finally {
      setIsPlayerLoading(false);
    }
  }, [refreshLearningProgress]);

  const getYoutubeEmbedUrl = useCallback((url?: string) => {
    if (!url) return '';
    const regExp = new RegExp('^.*(youtu.be/|v/|u/\\w/|embed/|watch\\?v=|&v=)([^#&\\?]*).*');
    const match = url.match(regExp);
    if (match && match[2].length === 11) {
      return `https://www.youtube.com/embed/${match[2]}`;
    }
    return url;
  }, []);

  return {
    playerCourseId,
    setPlayerCourseId,
    playerCourseTitle,
    setPlayerCourseTitle,
    playerCourseAuthor,
    setPlayerCourseAuthor,
    playerCourseProgress,
    setPlayerCourseProgress,
    playerLectureTitle,
    setPlayerLectureTitle,
    playerVideoThumbnail,
    setPlayerVideoThumbnail,
    playerVideoUrl,
    setPlayerVideoUrl,
    playerTheoryContent,
    setPlayerTheoryContent,
    learningChapters,
    setLearningChapters,
    selectedLessonId,
    setSelectedLessonId,
    isPlayerLoading,
    setIsPlayerLoading,
    playerActiveTab,
    setPlayerActiveTab,
    curriculumSections,
    setCurriculumSections,
    handleCompleteLesson,
    refreshLearningProgress,
    getYoutubeEmbedUrl
  };
};

