package com.swp391.coding_platform.service.ai;

import com.swp391.coding_platform.dto.request.ai.AiVisualizerRequest;
import com.swp391.coding_platform.dto.response.ai.AiVisualizerResponse;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.problem.ProblemVisualizerCache;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ai.AiGenerationException;
import com.swp391.coding_platform.exception.ai.RateLimitExceededException;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.problem.ProblemVisualizerCacheRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiVisualizerServiceTest {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private ChatClient aiVisualizerChatClient;

    @Mock
    private PromptSanitizerService sanitizerService;

    @Mock
    private ProblemVisualizerCacheRepository cacheRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private ChatResponse chatResponse;

    @Mock
    private Generation generation;

    @Mock
    private org.springframework.ai.chat.messages.AssistantMessage message;

    @InjectMocks
    private AiVisualizerService aiVisualizerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiVisualizerService, "maxRequestsPerDay", 5);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validateProblemScope_ContestScope_ThrowsException() {
        ProblemEntity problem = new ProblemEntity();
        problem.setProblemScope(ProblemScope.CONTEST);
        when(problemRepository.findById(1)).thenReturn(Optional.of(problem));

        assertThrows(AiGenerationException.class, () -> aiVisualizerService.validateProblemScope("1"));
    }

    @Test
    void getCurrentUserId_UserFound_ReturnsUserId() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        UserEntity user = new UserEntity();
        user.setId(10);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        String userId = aiVisualizerService.getCurrentUserId();
        assertEquals("10", userId);
    }

    @Test
    void getCurrentUserId_UserNotFound_ThrowsException() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> aiVisualizerService.getCurrentUserId());
    }

    @Test
    void getCachedVisualizer_NotForceRegenerateAndHit_ReturnsResponse() {
        ProblemVisualizerCache cache = new ProblemVisualizerCache();
        cache.setHtmlContent("<html></html>");
        cache.setDetectedAlgorithm("Algo");
        cache.setTimeComplexity("O(1)");

        when(cacheRepository.findByProblemIdAndUserIdAndPromptVersion(anyString(), anyString(), anyInt())).thenReturn(Optional.of(cache));

        Optional<AiVisualizerResponse> result = aiVisualizerService.getCachedVisualizer("1", "user1", false);
        
        assertTrue(result.isPresent());
        assertTrue(result.get().isFromCache());
        assertEquals("<html></html>", result.get().getHtmlContent());
    }

    @Test
    void generateVisualizer_RateLimitExceeded_ThrowsException() {
        AiVisualizerRequest request = new AiVisualizerRequest();
        request.setProblemId("1");
        request.setForceRegenerate(true);
        
        ReflectionTestUtils.setField(aiVisualizerService, "maxRequestsPerDay", 0);

        assertThrows(RateLimitExceededException.class, () -> aiVisualizerService.generateVisualizer(request, "user1"));
    }

    @Test
    void callAiAndParse_ValidResponse_ReturnsResponse() {
        String mockResponse = "###ALGORITHM_START### BFS ###ALGORITHM_END###" +
                "###COMPLEXITY_START### O(V+E) ###COMPLEXITY_END###" +
                "###HTML_START### <!doctype html><html><script></script></html> ###HTML_END###";

        when(aiVisualizerChatClient.prompt().user(anyString()).call().chatResponse()).thenReturn(chatResponse);
        
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(message);
        when(message.getContent()).thenReturn(mockResponse);

        AiVisualizerResponse result = aiVisualizerService.callAiAndParse("dummy prompt");
        
        assertFalse(result.isFromCache());
        assertEquals("BFS", result.getDetectedAlgorithm());
        assertEquals("O(V+E)", result.getTimeComplexity());
        assertTrue(result.getHtmlContent().contains("<!doctype html>"));
    }
    
    @Test
    void callAiAndParse_InvalidResponse_ThrowsException() {
        String mockResponse = "###ERROR_START### INVALID: Input error ###ERROR_END###";

        when(aiVisualizerChatClient.prompt().user(anyString()).call().chatResponse()).thenReturn(chatResponse);
        
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(message);
        when(message.getContent()).thenReturn(mockResponse);

        assertThrows(AiGenerationException.class, () -> aiVisualizerService.callAiAndParse("dummy prompt"));
    }
}
