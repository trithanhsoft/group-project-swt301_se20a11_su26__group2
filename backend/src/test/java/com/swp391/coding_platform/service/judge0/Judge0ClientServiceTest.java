package com.swp391.coding_platform.service.judge0;

import com.swp391.coding_platform.dto.judge0.Judge0BatchRequest;
import com.swp391.coding_platform.dto.judge0.Judge0CallbackPayload;
import com.swp391.coding_platform.dto.judge0.Judge0TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Judge0ClientServiceTest {

    @Mock
    private WebClient judge0WebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private Judge0ClientService judge0ClientService;

    @BeforeEach
    void setUp() {
        judge0ClientService = new Judge0ClientService(judge0WebClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendBatchSubmission_Success() {
        Judge0BatchRequest request = mock(Judge0BatchRequest.class);
        when(request.getSubmissions()).thenReturn(Collections.emptyList());

        Judge0TokenResponse tokenResponse = mock(Judge0TokenResponse.class);
        List<Judge0TokenResponse> expectedResponse = List.of(tokenResponse);

        when(judge0WebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Mono.class), eq(Judge0BatchRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedResponse));

        List<Judge0TokenResponse> actualResponse = judge0ClientService.sendBatchSubmission(request);

        assertNotNull(actualResponse);
        assertEquals(1, actualResponse.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendBatchSubmission_Error() {
        Judge0BatchRequest request = mock(Judge0BatchRequest.class);
        when(request.getSubmissions()).thenReturn(Collections.emptyList());

        when(judge0WebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Mono.class), eq(Judge0BatchRequest.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("API Error")));

        assertThrows(RuntimeException.class, () -> judge0ClientService.sendBatchSubmission(request));
    }

    @Test
    @SuppressWarnings("unchecked")
    void submitSynchronous_Success() {
        Judge0CallbackPayload payload = mock(Judge0CallbackPayload.class);
        
        when(judge0WebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Mono.class), eq(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(eq(Judge0CallbackPayload.class))).thenReturn(Mono.just(payload));

        Judge0CallbackPayload actualResponse = judge0ClientService.submitSynchronous(62, "System.out.println();");

        assertNotNull(actualResponse);
    }
}
