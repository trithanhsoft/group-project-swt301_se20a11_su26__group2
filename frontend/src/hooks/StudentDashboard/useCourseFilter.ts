import { useState, useRef, useCallback } from 'react';

export const useCourseFilter = () => {
  const [myCourses, setMyCourses] = useState<any[]>([]);
  const [myCoursesFilter, setMyCoursesFilter] = useState<'all' | 'ongoing' | 'completed'>('all');
  const [contestFilter, setContestFilter] = useState<'all' | 'ongoing' | 'upcoming' | 'ended'>('all');

  const ongoingScrollRef = useRef<HTMLDivElement>(null);
  const completedScrollRef = useRef<HTMLDivElement>(null);

  const scrollLeft = useCallback((ref: React.RefObject<HTMLDivElement | null>) => {
    if (ref.current) {
      ref.current.scrollBy({ left: -ref.current.offsetWidth, behavior: 'smooth' });
    }
  }, []);

  const scrollRight = useCallback((ref: React.RefObject<HTMLDivElement | null>) => {
    if (ref.current) {
      ref.current.scrollBy({ left: ref.current.offsetWidth, behavior: 'smooth' });
    }
  }, []);

  return {
    myCourses,
    setMyCourses,
    myCoursesFilter,
    setMyCoursesFilter,
    contestFilter,
    setContestFilter,
    ongoingScrollRef,
    completedScrollRef,
    scrollLeft,
    scrollRight
  };
};

