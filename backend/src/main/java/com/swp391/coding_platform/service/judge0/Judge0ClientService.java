package com.swp391.coding_platform.service.judge0;

import com.swp391.coding_platform.dto.judge0.Judge0BatchRequest;
import com.swp391.coding_platform.dto.judge0.Judge0CallbackPayload;
import com.swp391.coding_platform.dto.judge0.Judge0TokenResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@Service
public class Judge0ClientService {

    private final WebClient judge0WebClient;

    public Judge0ClientService(
            @org.springframework.beans.factory.annotation.Qualifier("judge0WebClient") WebClient judge0WebClient
    ) {
        this.judge0WebClient = judge0WebClient;
    }

    public List<Judge0TokenResponse> sendBatchSubmission(Judge0BatchRequest request) {
        log.info("Sending {} testcases to Judge0...", request.getSubmissions().size());

        List<Judge0TokenResponse> tokens = judge0WebClient.post()
                // Gắn query param base64_encoded=false vì ta gửi text thuần
                .uri("/submissions/batch?base64_encoded=false")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), Judge0BatchRequest.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Judge0TokenResponse>>() {})
                .block(); // Chặn chờ kết quả (Vì đã có timeout 20s ở config nên rất an toàn)

        log.info("Successfully received {} tokens from Judge0.", tokens != null ? tokens.size() : 0);
        return tokens;
    }

    public Judge0CallbackPayload submitSynchronous(int languageId, String sourceCode) {
        log.info("Sending synchronous testcase generation request to Judge0 (langId={})...", languageId);

        var requestBody = java.util.Map.of(
            "language_id", languageId,
            "source_code", sourceCode
        );

        Judge0CallbackPayload response = judge0WebClient.post()
                .uri("/submissions?base64_encoded=false&wait=true")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestBody), Object.class)
                .retrieve()
                .bodyToMono(Judge0CallbackPayload.class)
                .block();

        log.info("Received synchronous response from Judge0. Status: {}", response != null && response.getStatus() != null ? response.getStatus().getDescription() : "UNKNOWN");
        return response;
    }
}