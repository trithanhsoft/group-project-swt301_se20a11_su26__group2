const BASE_URL = 'http://localhost:8080/nonstopcoding';

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface InstructorCourse {
  id: string;
  title: string;
  topic: string;
  price: string;
  studentsCount: number;
  rating: number;
  reviewsCount: number;
  status: 'published' | 'review' | 'draft' | 'rejected';
  icon: string;
  gradient: string;
  description: string;
  thumbnailUrl?: string;
  level?: string;
}

export interface SalesHistoryItem {
  id: string;
  studentName: string;
  courseId: string;
  courseTitle: string;
  amount: number;
  timestamp: string;
}

export interface RecentRegistration {
  studentName: string;
  avatar: string;
  course: string;
  time: string;
  amount: string;
}

export interface PayoutHistoryItem {
  id: string;
  payoutPeriod: string;
  amount: number;
  bankName: string;
  bankAccountNumber: string;
  status: 'SUCCESS' | 'PROCESSING' | 'PENDING' | 'FAILED';
  transactionReference: string;
  adminNote?: string;
}

export interface CourseBreakdownItem {
  courseId: string;
  courseTitle: string;
  amount: number;
  percentage: number;
}

export interface MonthlyChartItem {
  label: string;
  year: number;
  month: number;
  amount: number;
  count: number;
}

export interface CourseRegistrationsItem {
  courseId: string;
  courseTitle: string;
  count: number;
}

export interface InstructorRevenueSummary {
  totalGrossRevenue: number;
  totalNetRevenue: number;
  totalActualTakeHome: number;
}

export interface InstructorCourseRegistrationsResponse {
  courseRegistrations: CourseRegistrationsItem[];
  totalTrendRegistrations: number;
}

export interface CreateCoursePayload {
  title: string;
  shortDescription: string;
  longDescription?: string;
  level?: string;
  topic?: string;
  categoryIds?: number[];
  isFree?: boolean;
  price?: number;
  whatYouLearn?: string[];
  courseHighlight?: string[];
  technologyTool?: string[];
  prerequisites?: string[];
  targetAudience?: string[];
  completionBenefits?: string[];
  thumbnailUrl?: string;
}

function mapBackendStatusToFrontend(status: string): 'published' | 'review' | 'draft' | 'rejected' {
  if (!status) return 'draft';
  const s = status.toUpperCase();
  if (s === 'APPROVED' || s === 'PUBLISHED') return 'published';
  if (s === 'PENDING' || s === 'REVIEW') return 'review';
  if (s === 'REJECTED') return 'rejected';
  if (s === 'DRAFTS' || s === 'DRAFT') return 'draft';
  return 'draft';
}

export const instructorService = {
  async uploadMedia(file: File, folderName: string = 'courses'): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('folderName', folderName);

    const response = await fetch(`${BASE_URL}/instructor/upload`, {
      method: 'POST',
      credentials: 'include',
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to upload media');
    }

    const data = await response.json();
    return data.result.secureUrl;
  },

  async getCourses(): Promise<InstructorCourse[]> {
    const response = await fetch(`${BASE_URL}/instructor/courses`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      if (response.status === 403) throw new Error('SUSPENDED');
      throw new Error('Failed to fetch instructor courses');
    }

    const data = await response.json();
    return (data.result || []).map((c: any) => ({
      ...c,
      status: mapBackendStatusToFrontend(c.status)
    }));
  },

  async getCategories(): Promise<Category[]> {
    const response = await fetch(`${BASE_URL}/categories`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    if (!response.ok) {
      throw new Error('Failed to fetch categories');
    }
    const data = await response.json();
    return data.result;
  },

  async submitCourseForReview(courseId: string): Promise<void> {
    const response = await fetch(`${BASE_URL}/instructor/courses/${courseId}/submit-review`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('Failed to submit course for review');
    }
  },

  async createCourse(courseData: CreateCoursePayload): Promise<InstructorCourse> {
    const response = await fetch(`${BASE_URL}/instructor/courses`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(courseData),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to create course');
    }

    const data = await response.json();
    return {
      ...data.result,
      status: mapBackendStatusToFrontend(data.result.status)
    };
  },

  async getCourseDetail(courseId: string): Promise<any> {
    const response = await fetch(`${BASE_URL}/instructor/courses/${courseId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('Failed to fetch course details');
    }

    const data = await response.json();
    if (data.result && data.result.status) {
      data.result.status = mapBackendStatusToFrontend(data.result.status);
    }
    return data.result;
  },

  async updateCourse(courseId: string, courseData: {
    title?: string;
    shortDescription?: string;
    longDescription?: string;
    level?: string;
    topic?: string;
    categoryIds?: number[];
    isFree?: boolean;
    price?: number;
    thumbnailUrl?: string;
    whatYouLearn?: string;
    courseHighlight?: string;
    technologyTool?: string;
    prerequisites?: string;
    targetAudience?: string;
    completionBenefits?: string;
    chapters?: {
      id?: number;
      title: string;
      lessons: {
        id?: number;
        title: string;
        video: string;
        theory: string;
        isTrial: boolean;
        status?: string;
        quizzes?: {
          id?: number;
          title: string;
          questions: {
            id?: number;
            content: string;
            options: {
              id?: number;
              content: string;
              isCorrect: boolean;
            }[];
          }[];
        }[];
      }[];
    }[];
  }): Promise<InstructorCourse> {
    const response = await fetch(`${BASE_URL}/instructor/courses/${courseId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(courseData),
    });

    if (!response.ok) {
      throw new Error('Failed to update course');
    }

    const data = await response.json();
    return {
      ...data.result,
      status: mapBackendStatusToFrontend(data.result.status)
    };
  },



  async getRevenueSummary(filter?: string, startDate?: string, endDate?: string): Promise<InstructorRevenueSummary> {
    let url = `${BASE_URL}/instructor/revenue/summary`;
    const params = new URLSearchParams();
    if (filter) params.append('filter', filter);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const queryString = params.toString();
    if (queryString) {
      url += `?${queryString}`;
    }

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      if (response.status === 403) throw new Error('SUSPENDED');
      throw new Error('Failed to fetch instructor revenue summary');
    }

    const data = await response.json();
    return data.result;
  },

  async getSalesHistory(filter?: string, startDate?: string, endDate?: string): Promise<SalesHistoryItem[]> {
    let url = `${BASE_URL}/instructor/revenue/sales-history`;
    const params = new URLSearchParams();
    if (filter) params.append('filter', filter);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const queryString = params.toString();
    if (queryString) {
      url += `?${queryString}`;
    }

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      if (response.status === 403) throw new Error('SUSPENDED');
      throw new Error('Failed to fetch instructor sales history');
    }

    const data = await response.json();
    return data.result;
  },

  async getRecentRegistrations(): Promise<RecentRegistration[]> {
    const response = await fetch(`${BASE_URL}/instructor/revenue/recent-registrations`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      if (response.status === 403) throw new Error('SUSPENDED');
      throw new Error('Failed to fetch instructor recent registrations');
    }

    const data = await response.json();
    return data.result;
  },

  async getPayoutHistory(): Promise<PayoutHistoryItem[]> {
    const response = await fetch(`${BASE_URL}/instructor/revenue/payout-history`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      if (response.status === 403) throw new Error('SUSPENDED');
      throw new Error('Failed to fetch instructor payout history');
    }

    const data = await response.json();
    return data.result;
  },

  async getCourseBreakdown(filter?: string, startDate?: string, endDate?: string): Promise<CourseBreakdownItem[]> {
    let url = `${BASE_URL}/instructor/revenue/course-breakdown`;
    const params = new URLSearchParams();
    if (filter) params.append('filter', filter);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const queryString = params.toString();
    if (queryString) {
      url += `?${queryString}`;
    }

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      if (response.status === 403) throw new Error('SUSPENDED');
      throw new Error('Failed to fetch instructor course breakdown');
    }

    const data = await response.json();
    return data.result;
  },

  async getMonthlyChartData(): Promise<MonthlyChartItem[]> {
    const response = await fetch(`${BASE_URL}/instructor/revenue/chart-data`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      if (response.status === 403) throw new Error('SUSPENDED');
      throw new Error('Failed to fetch instructor monthly chart data');
    }

    const data = await response.json();
    return data.result;
  },

  async getCourseRegistrations(trendTimeframe?: string): Promise<InstructorCourseRegistrationsResponse> {
    let url = `${BASE_URL}/instructor/revenue/course-registrations`;
    const params = new URLSearchParams();
    if (trendTimeframe) params.append('trendTimeframe', trendTimeframe);

    const queryString = params.toString();
    if (queryString) {
      url += `?${queryString}`;
    }

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      if (response.status === 403) throw new Error('SUSPENDED');
      throw new Error('Failed to fetch instructor course registrations trend');
    }

    const data = await response.json();
    return data.result;
  },

  async getCourseStatistics(courseId: string): Promise<any> {
    const response = await fetch(`${BASE_URL}/instructor/courses/${courseId}/statistics`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('Failed to fetch course statistics');
    }

    const data = await response.json();
    return data.result;
  },

  async getCourseModerationReport(courseId: string | number): Promise<any> {
    const response = await fetch(`${BASE_URL}/api/moderation/report/${courseId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    if (!response.ok) {
      throw new Error('Failed to fetch course moderation report');
    }
    return response.json();
  }
};
