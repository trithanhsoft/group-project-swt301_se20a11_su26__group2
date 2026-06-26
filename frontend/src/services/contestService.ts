// ==========================================
// MOCK DATA & UNUSED SERVICE
// TODO: This service is currently not used. Real contest components call APIs directly via fetch.
// If needed, integrate this service with real endpoints and remove mock fallback.
// ==========================================

export interface ContestStanding {
  rank: number;
  username: string;
  score: number;
  solvedCount: number;
  totalTime: number; // in minutes
  problemsSolved: { problemId: string; timeSolved: number; attempts: number }[];
}

export interface Contest {
  id: string;
  title: string;
  status: 'active' | 'upcoming' | 'past';
  startTime: string;
  duration: string;
  participants: number;
  rules: string[];
  problems: string[]; // Problem IDs
  standings: ContestStanding[];
}

// MOCK DATA FALLBACK (UNUSED)
export const mockContests: Contest[] = [];


export const getContestById = async (id: string): Promise<Contest | undefined> => {
  // TODO: Replace with real API fetch
  return mockContests.find(c => c.id === id);
};
export const getActiveContests = async (): Promise<Contest[]> => {
  // TODO: Replace with real API fetch
  return mockContests.filter(c => c.status === 'active');
};
export const getUpcomingContests = async (): Promise<Contest[]> => {
  // TODO: Replace with real API fetch
  return mockContests.filter(c => c.status === 'upcoming');
};
export const getPastContests = async (): Promise<Contest[]> => {
  // TODO: Replace with real API fetch
  return mockContests.filter(c => c.status === 'past');
};

const BASE_URL = 'http://localhost:8080/nonstopcoding';

export interface MyContestStats {
  totalContests: number;
  top1Count: number;
  top2Count: number;
  top3Count: number;
}

export interface MyContestHistory {
  id: number;
  title: string;
  startDate: string;
  endDate: string;
  status: string;
  rank: number;
  totalParticipants: number;
  problemsSolved: number;
  score: number;
}

export const getMyContestStats = async (): Promise<MyContestStats> => {
  const response = await fetch(`${BASE_URL}/contests/my-stats`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });
  if (!response.ok) {
    throw new Error('Failed to fetch my contest stats');
  }
  const data = await response.json();
  return data.result;
};

export const getMyContestHistory = async (): Promise<MyContestHistory[]> => {
  const response = await fetch(`${BASE_URL}/contests/my-history`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });
  if (!response.ok) {
    throw new Error('Failed to fetch my contest history');
  }
  const data = await response.json();
  return data.result;
};


