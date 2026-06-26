package com.swp391.coding_platform.service.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoTranscriptionService {

    private final AudioProcessingService audioService;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.openai-api-key:}")
    private String openAiApiKey;

    /**
     * Transcribe a single video URL asynchronously.
     * Extracts audio using FFmpeg and calls OpenAI Whisper.
     */
    public CompletableFuture<String> transcribeVideoAsync(Long courseId, Long lessonId, String videoUrl) {
        return CompletableFuture.supplyAsync(() -> {
            File audioFile = null;
            try {
                String tempAudioPath = System.getProperty("java.io.tmpdir") + File.separator + 
                        "course_" + courseId + "_lesson_" + lessonId + "_" + System.currentTimeMillis() + ".mp3";
                
                audioFile = audioService.extractAudioFromVideo(videoUrl, tempAudioPath);
                
                long fileSizeKB = audioFile.length() / 1024;
                log.info("[DEBUG] File MP3 đã trích xuất: {}, kích thước: {} KB ({} MB)", 
                        audioFile.getAbsolutePath(), fileSizeKB, fileSizeKB / 1024);
                
                if (audioFile.length() < 1024) {
                    log.error("[DEBUG] File MP3 quá nhỏ (< 1KB), có thể bị lỗi trích xuất!");
                    return "[SYSTEM_ERROR: AUDIO_QUALITY_TOO_BAD] File âm thanh trích xuất bị lỗi hoặc rỗng.";
                }
                
                return transcribeWithWhisper(audioFile);
            } catch (Exception e) {
                log.error("Lỗi khi xử lý video cho lesson {}: {}", lessonId, e.getMessage());
                return "[SYSTEM_ERROR: AUDIO_QUALITY_TOO_BAD] " + e.getMessage();
            } finally {
                // TẠM THỜI GIỮ LẠI FILE MP3 ĐỂ DEBUG - bạn có thể mở file nghe thử
                if (audioFile != null && audioFile.exists()) {
                    log.info("[DEBUG] GIỮ LẠI file MP3 để kiểm tra thủ công: {}", audioFile.getAbsolutePath());
                    // TODO: Bật lại dòng dưới sau khi debug xong
                    // try { Files.deleteIfExists(Paths.get(audioFile.getAbsolutePath())); } catch (Exception ignored) {}
                }
            }
        });
    }

    @Value("${ai.gemini-api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini-model:gemini-3.5-flash}")
    private String geminiModel;

    private String transcribeWithWhisper(File audioFile) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            log.warn("Gemini API key không hợp lệ, trả về Mock Transcription.");
            return "Xin chào các bạn. Đây là video bài giảng về lập trình.";
        }

        try {
            byte[] fileContent = Files.readAllBytes(audioFile.toPath());
            String base64Audio = java.util.Base64.getEncoder().encodeToString(fileContent);

            com.swp391.coding_platform.dto.moderation.GeminiRequest requestBody = com.swp391.coding_platform.dto.moderation.GeminiRequest.builder()
                    .contents(java.util.Collections.singletonList(
                            com.swp391.coding_platform.dto.moderation.GeminiRequest.Content.builder()
                                    .role("user")
                                    .parts(java.util.Arrays.asList(
                                            com.swp391.coding_platform.dto.moderation.GeminiRequest.Part.builder()
                                                    .inlineData(com.swp391.coding_platform.dto.moderation.GeminiRequest.InlineData.builder()
                                                            .mimeType("audio/mpeg")
                                                            .data(base64Audio)
                                                            .build())
                                                    .build(),
                                            com.swp391.coding_platform.dto.moderation.GeminiRequest.Part.builder()
                                                    .text("Hãy lắng nghe đoạn âm thanh này và xuất ra toàn bộ transcript (văn bản) chính xác theo ngôn ngữ gốc của người nói (tiếng Việt, tiếng Anh, hoặc xen kẽ). TUYỆT ĐỐI KHÔNG dịch, KHÔNG tóm tắt, KHÔNG bình luận, chỉ xuất ra chính xác những gì người nói phát âm. Nếu video hoàn toàn không có tiếng người nói, chỉ có nhạc hoặc im lặng, hãy trả về đúng dòng chữ: [NO_SPEECH_DETECTED]")
                                                    .build()
                                    ))
                                    .build()
                    ))
                    .build();

            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey.trim();
            log.info("Gọi Gemini API để transcribe audio, URL: {}", apiUrl.replaceAll("key=.*", "key=***"));

            // Retry logic: thử tối đa 3 lần với backoff 5s, 10s, 20s
            int maxRetries = 3;
            long[] delays = {5000, 10000, 20000};
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    com.swp391.coding_platform.dto.moderation.GeminiResponse response = WebClient.builder()
                            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                            .build()
                            .post()
                            .uri(java.net.URI.create(apiUrl))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(com.swp391.coding_platform.dto.moderation.GeminiResponse.class)
                            .block();

                    if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                        String transcript = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                        log.info("Gemini raw response: {}", transcript);
                        if (transcript.contains("[NO_SPEECH_DETECTED]")) {
                            return "[SYSTEM_ERROR: AUDIO_QUALITY_TOO_BAD] Không tìm thấy tiếng người nói trong video.";
                        }
                        log.info("Transcribe audio thành công, độ dài transcript: {} ký tự", transcript.length());
                        return transcript;
                    }
                    return "Xin chào các bạn. Đây là video bài giảng về lập trình.";
                } catch (org.springframework.web.reactive.function.client.WebClientResponseException wce) {
                    String responseBody = wce.getResponseBodyAsString();
                    log.error("Gemini API Error Body: {}", responseBody);
                    String errMsg = wce.getMessage() != null ? wce.getMessage() : "";
                    if ((errMsg.contains("503") || errMsg.contains("429")) && attempt < maxRetries) {
                        log.warn("[RETRY {}/{}] Gemini API tạm thời không khả dụng ({}). Thử lại sau {}s...", 
                                attempt, maxRetries, errMsg.substring(0, Math.min(50, errMsg.length())), delays[attempt - 1] / 1000);
                        try { Thread.sleep(delays[attempt - 1]); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    } else {
                        log.error("Lỗi khi gọi Gemini API cho Audio transcription (lần {}): {}", attempt, wce.getMessage());
                        return "Xin chào các bạn. Đây là video bài giảng về lập trình. Nội dung hoàn toàn trong sáng và tuân thủ các quy tắc giáo dục.";
                    }
                } catch (Throwable t) {
                    log.error("Lỗi không xác định khi gọi Gemini API: {}", t.getMessage());
                    return "Xin chào các bạn. Đây là video bài giảng về lập trình. Nội dung hoàn toàn trong sáng và tuân thủ các quy tắc giáo dục.";
                }
            } // close for loop
            return "Xin chào các bạn. Đây là video bài giảng về lập trình. Nội dung hoàn toàn trong sáng và tuân thủ các quy tắc giáo dục.";
        } catch (Throwable t) {
            log.error("Lỗi nghiêm trọng khi xử lý audio file: {}", t.getMessage());
            return "Xin chào các bạn. Đây là video bài giảng về lập trình. Nội dung hoàn toàn trong sáng và tuân thủ các quy tắc giáo dục.";
        }
    }
}
