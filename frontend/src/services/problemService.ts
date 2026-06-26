const BASE_URL = 'http://localhost:8080/nonstopcoding';

export interface ProblemListItem {
  id: number;
  title: string;
  difficulty: 'Easy' | 'Medium' | 'Hard';
  tags: string[];
  score: number;
  totalSubmission: number;
  totalAccepted: number;
  isSolved: boolean;
  status: 'solved' | 'unsolved' | 'attempted';
}

export interface ProblemDetail {
  id: number;
  title: string;
  difficulty: string;
  description: string;
  inputDescription: string;
  outputDescription: string;
  constraints: string;
  exampleInput: string;
  exampleOutput: string;
  hint: string;
  tags: string[];
  templates?: { [key: string]: string };
  starterTemplates?: { [key: string]: string };
  status: 'solved' | 'unsolved' | 'attempted';
  acceptance: string;
  totalSolved: number;
  source_code?: string;
  language_id?: number;
}

export interface ProblemSolution {
  problemId: number;
  title: string;
  solutionCode: string;
}

export interface ProblemSubmission {
  status: string;
  lang: string;
  runtime: string;
  memory: string;
  time: string;
  statusClass: string;
}

export interface SubmitResponse {
  submissionId: number;
  status: string;
  message: string;
}

export interface ProblemComment {
  id: number;
  author: string;
  avatar_url?: string;
  text: string;
  time: string;
  createdAt: string;
  parentId: number | null;
  replies: ProblemComment[];
}

interface ApiResponse<T> {
  status: number;
  code: number;
  message: string;
  result: T;
  timestamp: string;
}

export const problemService = {
  async fetchProblems(): Promise<ProblemListItem[]> {
    const response = await fetch(`${BASE_URL}/api/problems`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    if (!response.ok) {
      throw new Error('Failed to fetch problems');
    }
    const data: ApiResponse<ProblemListItem[]> = await response.json();
    return data.result;
  },

  async fetchProblemDetail(id: number | string): Promise<ProblemDetail> {
    const response = await fetch(`${BASE_URL}/api/problems/${id}/description`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    if (!response.ok) {
      throw new Error('Failed to fetch problem description');
    }
    const data: ApiResponse<ProblemDetail> = await response.json();
    return data.result;
  },

  async submitSolution(problemId: number | string, languageId: number, sourceCode: string, contestId?: number | string): Promise<SubmitResponse> {
    const payload: any = { problemId: Number(problemId), languageId, sourceCode };
    if (contestId !== undefined && contestId !== null) {
      payload.contestId = Number(contestId);
    }
    const response = await fetch(`${BASE_URL}/online-judge/submissions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to submit solution');
    }
    const data: ApiResponse<SubmitResponse> = await response.json();
    return data.result;
  },

  async fetchProblemComments(problemId: number | string): Promise<ProblemComment[]> {
    const response = await fetch(`${BASE_URL}/api/problems/${problemId}/discussion`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    if (!response.ok) {
      throw new Error('Failed to fetch problem comments');
    }
    const data: ApiResponse<ProblemComment[]> = await response.json();
    return data.result;
  },

  async postProblemComment(problemId: number | string, content: string, parentId?: number): Promise<ProblemComment> {
    const response = await fetch(`${BASE_URL}/api/problems/${problemId}/discussion`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify({ content, parentId }),
    });
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to post comment');
    }
    const data: ApiResponse<ProblemComment> = await response.json();
    return data.result;
  },

  async fetchProblemSolution(id: number | string): Promise<ProblemSolution> {
    const response = await fetch(`${BASE_URL}/api/problems/${id}/solution`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to fetch solution. Ensure you have solved the problem.');
    }
    const data: ApiResponse<ProblemSolution> = await response.json();
    return data.result;
  },

  async fetchProblemSubmissions(id: number | string): Promise<ProblemSubmission[]> {
    const response = await fetch(`${BASE_URL}/api/problems/${id}/submissions`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    if (!response.ok) {
      throw new Error('Failed to fetch submissions history');
    }
    const data: ApiResponse<ProblemSubmission[]> = await response.json();
    return data.result;
  },

  async fetchContestProblemDetail(contestId: number | string, problemId: number | string): Promise<ProblemDetail> {
    const response = await fetch(`${BASE_URL}/contests/${contestId}/problems/${problemId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    if (!response.ok) {
      throw new Error('Failed to fetch contest problem description');
    }
    const data: ApiResponse<ProblemDetail> = await response.json();
    return data.result;
  },

  async fetchContestSubmissions(contestId: number | string): Promise<any[]> {
    const response = await fetch(`${BASE_URL}/contests/${contestId}/submissions`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });
    if (!response.ok) {
      throw new Error('Failed to fetch contest submissions');
    }
    const data: ApiResponse<any[]> = await response.json();
    return data.result;
  }
};
