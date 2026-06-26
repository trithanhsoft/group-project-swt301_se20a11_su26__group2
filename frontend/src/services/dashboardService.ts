const BASE_URL = 'http://localhost:8080/nonstopcoding';

export interface DashboardStatsResponse {
    enrolled: number;
    completedCourses: number;
    solvedPractice: number;
    totalPracticeProblems: number;
    ranking: number;
    totalUsers: number;
    currentBalance: number;
}

export interface UserActivityResponse {
    userId: number;
    year: number;
    maxStreak: number;
    currentStreak: number;
    activeDates: string[];
}

export interface CourseListItemResponse {
    id: number;
    title: string;
    thumbnailUrl: string;
    shortDescription: string;
    price: number;
    averageRating: number;
    totalReviews: number;
    totalEnrolled: number;
    enrolled: boolean;
    progressPercentage: number;
    instructorName: string;
}

export interface SubmissionStatisticResponse {
    totalSubmissions: number;
    totalAccepted: number;
    totalWrongAnswer: number;
    totalTimeLimitExceeded: number;
    totalMemoryLimitExceeded: number;
}

export const dashboardService = {
  async getDashboardStats(): Promise<DashboardStatsResponse> {
    const response = await fetch(`${BASE_URL}/me/dashboard-stats`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch dashboard stats');
    }
    
    const data = await response.json();
    return data.result;
  },
  
  async getUserActivities(year?: number): Promise<UserActivityResponse> {
    const url = year ? `${BASE_URL}/me/activities?year=${year}` : `${BASE_URL}/me/activities`;
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch user activities');
    }
    
    const data = await response.json();
    return data.result;
  },

  async getEnrolledCourses(): Promise<CourseListItemResponse[]> {
    const response = await fetch(`${BASE_URL}/me/enrolled-courses`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch enrolled courses');
    }
    
    const data = await response.json();
    return data.result;
  },

  async getSubmissionStatistics(): Promise<SubmissionStatisticResponse> {
    const response = await fetch(`${BASE_URL}/me/submission-statistics`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch submission statistics');
    }
    
    const data = await response.json();
    return data.result;
  }
};
