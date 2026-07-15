import { useState, useCallback, useRef, useEffect } from 'react';
import { aiService } from '../services/aiService';

export const useAiVisualizer = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [data, setData] = useState<any>(null);
    const [error, setError] = useState<string | null>(null);
    const [jobStatus, setJobStatus] = useState('idle'); // 'idle' | 'polling' | 'success' | 'error'

    const abortControllerRef = useRef<AbortController | null>(null);
    const pollingIntervalRef = useRef<any>(null);

    const clearPolling = () => {
        if (pollingIntervalRef.current) {
            clearInterval(pollingIntervalRef.current);
            pollingIntervalRef.current = null;
        }
    };

    const cleanup = useCallback(() => {
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }
        clearPolling();
        setIsLoading(false);
    }, []);

    useEffect(() => {
        return cleanup; // Cleanup on unmount
    }, [cleanup]);

    const pollJobStatus = async (jobId: string, signal?: AbortSignal) => {
        setJobStatus('polling');
        
        pollingIntervalRef.current = setInterval(async () => {
            try {
                const response = await aiService.pollJobStatus(jobId, signal);
                if (!response.ok) throw new Error('Lỗi khi kiểm tra trạng thái.');
                
                const json = await response.json();
                const jobStatusRes = json.result; // this is the JobStatusResponse
                
                if (jobStatusRes.status === 'SUCCESS') {
                    clearPolling();
                    setData(jobStatusRes.result);
                    setJobStatus('success');
                    setIsLoading(false);
                } else if (jobStatusRes.status === 'FAILED') {
                    clearPolling();
                    setError(jobStatusRes.errorMessage || 'Lỗi khi tạo mô phỏng.');
                    setJobStatus('error');
                    setIsLoading(false);
                }
                // If PENDING or PROCESSING, continue polling
            } catch (err: any) {
                if (err.name !== 'AbortError') {
                    clearPolling();
                    setError(err.message || 'Lỗi mạng khi kiểm tra trạng thái.');
                    setJobStatus('error');
                    setIsLoading(false);
                }
            }
        }, 3000);
    };

    const generate = useCallback(async (request: any) => {
        if (isLoading) return; // Debounce / Guard
        
        cleanup(); // abort previous requests if any
        abortControllerRef.current = new AbortController();
        const { signal } = abortControllerRef.current;

        setIsLoading(true);
        setError(null);
        setJobStatus('idle');

        try {
            const response = await aiService.generateVisualizer(request, signal);

            if (response.status === 200) {
                // Cache hit
                const json = await response.json();
                setData(json.result);
                setJobStatus('success');
                setIsLoading(false);
            } else if (response.status === 202) {
                // Async job started
                const json = await response.json();
                pollJobStatus(json.result.jobId, signal);
            } else if (response.status === 429) {
                const json = await response.json();
                throw new Error(json.message || 'Bạn đã vượt quá giới hạn lượt dùng trong ngày.');
            } else if (response.status === 502 || response.status === 504) {
                const json = await response.json();
                throw new Error(json.message || 'AI đang quá tải hoặc gặp lỗi. Vui lòng thử lại sau.');
            } else {
                throw new Error('Đã xảy ra lỗi không xác định.');
            }
        } catch (err: any) {
            if (err.name !== 'AbortError') {
                setError(err.message);
                setJobStatus('error');
                setIsLoading(false);
            }
        }
    }, [isLoading, cleanup]);

    const regenerate = useCallback((request: any) => {
        generate({ ...request, forceRegenerate: true });
    }, [generate]);

    const checkCache = useCallback(async (problemId: string) => {
        try {
            setIsLoading(true);
            const response = await aiService.checkCache(problemId);
            if (response.ok) {
                const json = await response.json();
                if (json.result) {
                    setData(json.result);
                    setJobStatus('success');
                }
            }
        } catch (err) {
            console.error('Failed to check cache', err);
        } finally {
            setIsLoading(false);
        }
    }, []);

    return { isLoading, data, error, jobStatus, generate, regenerate, cleanup, checkCache };
};
