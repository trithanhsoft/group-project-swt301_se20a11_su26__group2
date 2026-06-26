import type { ApiResponse } from './courseService';

const BASE_URL = 'http://localhost:8080/nonstopcoding';

export interface RankingUser {
  rank: number;
  userId: number;
  name: string;
  avatar: string;
  points: number;
}

export interface UserRankStats {
  rank: number;
  points: number;
  pointsToNextRank: number;
  nextRankUserName: string;
}

export const rankingService = {
  async fetchGlobalRankings(filter: 'all' | 'weekly' | 'monthly' = 'all'): Promise<RankingUser[]> {
    const response = await fetch(`${BASE_URL}/rankings?filter=${filter}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Mandated by VIBE_CODE.md to send HttpOnly session cookies
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Không thể tải bảng xếp hạng');
    }

    const data: ApiResponse<RankingUser[]> = await response.json();
    return data.result;
  },

  async fetchUserRankStats(filter: 'all' | 'weekly' | 'monthly' = 'all'): Promise<UserRankStats | null> {
    const response = await fetch(`${BASE_URL}/rankings/me?filter=${filter}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Mandated by VIBE_CODE.md to send HttpOnly session cookies
    });

    if (response.status === 401) {
      return null;
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Không thể tải thống kê xếp hạng cá nhân');
    }

    const data: ApiResponse<UserRankStats> = await response.json();
    return data.result;
  }
};
