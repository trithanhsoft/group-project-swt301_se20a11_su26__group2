import { useState, useRef, useCallback } from 'react';
import { fetchQuizByLesson, submitQuiz, type QuizDetail } from '../../services/courseService';

export const useQuizManagement = () => {
  const [currentQuiz, setCurrentQuiz] = useState<QuizDetail | null>(null);
  const [isQuizLoading, setIsQuizLoading] = useState<boolean>(false);
  const [quizError, setQuizError] = useState<string | null>(null);
  const [selectedAnswers, setSelectedAnswers] = useState<Record<number, number | null>>({});
  const [isQuizSubmitting, setIsQuizSubmitting] = useState<boolean>(false);
  const quizTabRef = useRef<HTMLDivElement>(null);
  const tabsContainerRef = useRef<HTMLDivElement>(null);

  const loadQuizDetail = useCallback(async (courseId: number, lessonId: number) => {
    setIsQuizLoading(true);
    setQuizError(null);
    try {
      const quiz = await fetchQuizByLesson(courseId, lessonId);
      setCurrentQuiz(quiz);
      const answers: Record<number, number | null> = {};
      if (quiz.submitted && quiz.questions) {
        quiz.questions.forEach(q => {
          answers[q.questionId] = q.selectedOptionId ?? null;
        });
      } else {
        if (quiz.questions) {
          quiz.questions.forEach(q => {
            answers[q.questionId] = null;
          });
        }
      }
      setSelectedAnswers(answers);
    } catch (err: any) {
      console.error('Error fetching quiz:', err);
      setQuizError(err.message || 'Failed to load quiz details');
      setCurrentQuiz(null);
    } finally {
      setIsQuizLoading(false);
    }
  }, []);

  const handleQuizSubmit = useCallback(async (courseId: number) => {
    if (!courseId || !currentQuiz) return;

    const answers = Object.entries(selectedAnswers).map(([qId, optId]) => ({
      questionId: parseInt(qId),
      selectedOptionId: optId
    }));

    setIsQuizSubmitting(true);
    try {
      await submitQuiz(courseId, currentQuiz.quizId, { answers });
      // Reload quiz to show results
      await loadQuizDetail(courseId, currentQuiz.quizId);
      // Scroll smoothly to the tab bar (higher view)
      setTimeout(() => {
        tabsContainerRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 100);
    } catch (err: any) {
      console.error('Error submitting quiz:', err);
      alert(err.message || 'Failed to submit quiz');
    } finally {
      setIsQuizSubmitting(false);
    }
  }, [currentQuiz, selectedAnswers, loadQuizDetail]);

  return {
    currentQuiz,
    setCurrentQuiz,
    isQuizLoading,
    setIsQuizLoading,
    quizError,
    setQuizError,
    selectedAnswers,
    setSelectedAnswers,
    isQuizSubmitting,
    setIsQuizSubmitting,
    quizTabRef,
    tabsContainerRef,
    loadQuizDetail,
    handleQuizSubmit
  };
};

