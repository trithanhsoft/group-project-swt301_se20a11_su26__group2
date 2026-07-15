const BASE_URL = 'http://localhost:8080/nonstopcoding';

export const aiService = {
  async generateVisualizer(request: any, signal?: AbortSignal) {
    const response = await fetch(`${BASE_URL}/api/v1/ai/visualizer/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify(request),
      signal
    });
    return response;
  },

  async pollJobStatus(jobId: string, signal?: AbortSignal) {
    const response = await fetch(`${BASE_URL}/api/v1/ai/visualizer/status/${jobId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      signal
    });
    return response;
  },

  async checkCache(problemId: string, signal?: AbortSignal) {
    const response = await fetch(`${BASE_URL}/api/v1/ai/visualizer/${problemId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      signal
    });
    return response;
  }
};
