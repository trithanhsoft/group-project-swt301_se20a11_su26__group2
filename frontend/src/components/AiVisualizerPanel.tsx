import React, { useRef, useEffect, useState } from 'react';
import { useAiVisualizer } from '../hooks/useAiVisualizer';

interface AiVisualizerPanelProps {
    problemRequest: any;
}

const AiVisualizerPanel: React.FC<AiVisualizerPanelProps> = ({ problemRequest }) => {
    const { isLoading, data, error, jobStatus, generate, regenerate, checkCache } = useAiVisualizer();
    const iframeRef = useRef(null);
    const [userInput, setUserInput] = useState<string>('');

    useEffect(() => {
        if (problemRequest && problemRequest.problemId) {
            checkCache(problemRequest.problemId);
        }
    }, [problemRequest?.problemId, checkCache]);

    const handleGenerate = () => {
        generate({ ...problemRequest, userInput });
    };

    const handleRegenerate = () => {
        regenerate({ ...problemRequest, userInput });
    };

    const handleSpeedChange = (e: any) => {
        const speed = parseInt(e.target.value, 10);
        // postMessage to iframe to adjust delay
        if (iframeRef.current && (iframeRef.current as HTMLIFrameElement).contentWindow) {
            (iframeRef.current as HTMLIFrameElement).contentWindow!.postMessage({ type: 'SET_SPEED', value: speed }, '*');
        }
    };

    return (
        <div className="p-4 bg-gray-800 rounded-xl shadow-lg text-white">
            {!isLoading && !data && (
                <div className="flex flex-col items-center justify-center p-8 space-y-6">
                    <div className="text-center max-w-md space-y-2">
                        <p className="text-gray-300">
                            Want to see how this algorithm works? AI can analyze and simulate it step-by-step for you.
                        </p>
                        <p className="text-sm text-gray-400">
                            (Optional) You can provide your own input below for the AI to simulate, otherwise it will use the problem's default example input.
                        </p>
                    </div>
                    
                    <div className="w-full max-w-md">
                        <label htmlFor="userInput" className="block text-sm font-medium text-gray-400 mb-1">
                            Custom Input (Optional)
                        </label>
                        <textarea
                            id="userInput"
                            rows={3}
                            className="w-full bg-gray-900 text-gray-200 border border-gray-600 rounded-lg p-3 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 placeholder-gray-600 transition"
                            placeholder="Enter your custom input here..."
                            value={userInput}
                            onChange={(e) => setUserInput(e.target.value)}
                        />
                    </div>

                    {error && (
                        <div className="w-full max-w-md p-4 border border-red-500 bg-red-900/20 rounded-lg text-red-400 text-sm text-center">
                            {error}
                        </div>
                    )}

                    <button
                        onClick={handleGenerate}
                        disabled={isLoading}
                        className="px-6 py-3 bg-blue-600 hover:bg-blue-500 rounded-lg font-bold transition-all shadow-md flex items-center space-x-2"
                    >
                        <span>{error ? '🔄 Try Again' : '🤖 Ask AI to Simulate Algorithm'}</span>
                    </button>
                </div>
            )}

            {isLoading && (
                <div className="flex flex-col items-center justify-center p-12 space-y-4 animate-pulse">
                    <div className="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                    <p className="text-yellow-400 font-semibold text-lg">AI is analyzing the problem and generating the simulation... Please wait</p>
                    <p className="text-gray-400 text-sm">(Usually takes about 15-30 seconds)</p>
                </div>
            )}



            {data && !isLoading && (
                <div className="flex flex-col space-y-4">
                    <div className="flex flex-wrap items-center justify-between gap-4">
                        <div className="flex space-x-2">
                            <span className="px-3 py-1 bg-green-900 text-green-300 border border-green-500 rounded-full text-sm font-bold">
                                🚀 Algorithm: {data.detectedAlgorithm}
                            </span>
                            <span className="px-3 py-1 bg-purple-900 text-purple-300 border border-purple-500 rounded-full text-sm font-bold">
                                ⏱ O-Big: {data.timeComplexity}
                            </span>
                            {data.fromCache && (
                                <span className="px-3 py-1 bg-gray-700 text-gray-300 border border-gray-500 rounded-full text-sm">
                                    ⚡ From cache
                                </span>
                            )}
                        </div>
                        <div className="flex items-center space-x-4">
                            <div className="flex items-center space-x-2 text-sm">
                                <label htmlFor="speedSelect" className="text-gray-400">Speed:</label>
                                <select 
                                    id="speedSelect"
                                    onChange={handleSpeedChange} 
                                    defaultValue="1000"
                                    className="bg-gray-800 text-white border border-gray-600 rounded px-2 py-1 outline-none focus:border-blue-500"
                                >
                                    <option value="2000">0.25x</option>
                                    <option value="1000">0.5x (Default)</option>
                                    <option value="500">1x</option>
                                </select>
                            </div>
                            <div className="flex items-center space-x-2">
                                <input
                                    type="text"
                                    placeholder="New custom input (optional)..."
                                    className="bg-gray-900 text-gray-200 border border-gray-600 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:border-blue-500 w-48 transition-colors"
                                    value={userInput}
                                    onChange={(e) => setUserInput(e.target.value)}
                                />
                                <button
                                    onClick={handleRegenerate}
                                    className="px-4 py-1.5 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm font-semibold transition whitespace-nowrap"
                                >
                                    🔄 Regenerate
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="w-full h-[calc(100vh-220px)] min-h-[600px] border-none rounded-lg bg-gray-900 overflow-hidden relative shadow-inner">
                        <iframe
                            ref={iframeRef}
                            srcDoc={data.htmlContent}
                            sandbox="allow-scripts"
                            className="w-full h-full"
                            title="AI Visualizer"
                        />
                    </div>
                </div>
            )}
        </div>
    );
};

export default AiVisualizerPanel;
