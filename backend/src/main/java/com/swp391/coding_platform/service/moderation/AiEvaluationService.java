package com.swp391.coding_platform.service.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.moderation.CourseModerationPayload;
import com.swp391.coding_platform.dto.moderation.GeminiRequest;
import com.swp391.coding_platform.dto.moderation.GeminiResponse;
import com.swp391.coding_platform.dto.moderation.ModerationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Service
@Slf4j
public class AiEvaluationService {

    private final WebClient aiWebClient;
    private final ObjectMapper objectMapper;

    public AiEvaluationService(
            @Qualifier("aiWebClient") WebClient aiWebClient,
            ObjectMapper objectMapper
    ) {
        this.aiWebClient = aiWebClient;
        this.objectMapper = objectMapper;
    }

    @Value("${ai.gemini-api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini-model:gemini-1.5-flash}")
    private String geminiModel;

    public ModerationResult evaluateCourse(CourseModerationPayload payload) {
        String prompt = "Bạn là Chuyên gia Kiểm duyệt An toàn và Nội dung. Yêu cầu của bạn là duyệt dữ liệu Khóa học theo các tiêu chí khắt khe sau:\n" +
                "1. Không đánh giá chất lượng sư phạm hay, dở. Chỉ kiểm tra sự vi phạm và tính hợp lệ cơ bản.\n" +
                "2. Ngôn từ tuyệt đối KHÔNG chứa nội dung thô tục, bậy bạ, chống phá nhà nước, tuyên truyền tiêu cực.\n" +
                "3. Tính toàn vẹn: Bất cứ Lesson nào cũng phải có nội dung Theory. Nếu Theory rỗng hoặc trống, đánh dấu vi phạm cho Lesson đó.\n" +
                "4. Chất lượng âm thanh: Nếu thấy trong Video Transcript xuất hiện tag [SYSTEM_ERROR: AUDIO_QUALITY_TOO_BAD] hoặc có các từ ngữ lặp đi lặp lại vô nghĩa (như lặp từ cảm ơn liên tục không có ngữ cảnh), hãy đánh dấu lỗi Chất lượng âm thanh kém.\n" +
                "5. Tính liên quan: Nội dung của Video Transcript (và Theory) phải liên quan chặt chẽ đến chủ đề của Course và Lesson (Ví dụ: Tiêu đề là 'Java Spring Boot' nhưng video lại phát bài hát, nhạc trẻ, nội dung giải trí không ăn nhập thì phải từ chối ngay lập tức).\n" +
                "6. Đối với Quizzes: Chỉ kiểm tra xem ngôn từ (câu hỏi và đáp án) có vi phạm chuẩn mực/thuần phong mỹ tục không, tuyệt đối KHÔNG cần kiểm tra tính đúng/sai của câu hỏi hay đáp án.\n\n" +
                "Hãy trả về MỘT VÀ CHỈ MỘT đối tượng JSON KHỚP ĐÚNG với cấu trúc sau:\n" +
                "{\n" +
                "  \"isClean\": boolean (true nếu HOÀN TOÀN KHÔNG CÓ BẤT KỲ LỖI NÀO ở bất kỳ đâu, false nếu có ít nhất 1 lỗi),\n" +
                "  \"courseViolations\": [\"danh sách lỗi chung của toàn khóa học, ví dụ tỷ lệ video/quiz nếu có\"],\n" +
                "  \"lessonViolations\": [\n" +
                "    {\n" +
                "      \"lessonId\": số ID của bài học,\n" +
                "      \"lessonTitle\": \"Tên bài học\",\n" +
                "      \"violationType\": \"Loại lỗi (ví dụ: MISSING_THEORY, PROFANITY, BAD_AUDIO, IRRELEVANT_CONTENT)\",\n" +
                "      \"reason\": \"Giải thích ngắn gọn lý do lỗi\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "Lưu ý: Chỉ ghi nhận các Lesson có lỗi vào mảng lessonViolations. Lesson nào OK thì bỏ qua.";

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String userContent = prompt + "\n\n--- THÔNG TIN KHÓA HỌC ---\n" + payloadJson;

            return callGemini(userContent);
        } catch (Exception e) {
            log.error("Lỗi khi đóng gói payload: {}", e.getMessage());
            return generateSystemError();
        }
    }

    public ModerationResult evaluateSingleLesson(CourseModerationPayload.LessonPayload lessonPayload) {
        String prompt = "Bạn là Chuyên gia Kiểm duyệt An toàn và Nội dung. Yêu cầu của bạn là duyệt MỘT bài học duy nhất.\n" +
                "1. Không chứa nội dung thô tục, chống phá.\n" +
                "2. Phải có Theory.\n" +
                "3. Video Transcript không được chứa [SYSTEM_ERROR...] hoặc lặp từ vô nghĩa.\n" +
                "4. Đối với Quizzes: Chỉ đánh giá ngôn từ có vi phạm chuẩn mực không, KHÔNG kiểm tra tính đúng/sai của đáp án.\n\n" +
                "Hãy trả về JSON theo cấu trúc:\n" +
                "{\n" +
                "  \"isClean\": boolean,\n" +
                "  \"courseViolations\": [],\n" +
                "  \"lessonViolations\": [ { \"lessonId\": ID, \"lessonTitle\": \"Title\", \"violationType\": \"Type\", \"reason\": \"Lý do\" } ]\n" +
                "}";

        try {
            String payloadJson = objectMapper.writeValueAsString(lessonPayload);
            return callGemini(prompt + "\n\n" + payloadJson);
        } catch (Exception e) {
            return generateSystemError();
        }
    }

    private ModerationResult callGemini(String fullPrompt) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            log.warn("Gemini Eval API key không hợp lệ, trả về Mock Evaluation.");
            return ModerationResult.builder()
                    .isClean(true)
                    .courseViolations(Collections.emptyList())
                    .lessonViolations(Collections.emptyList())
                    .build();
        }

        GeminiRequest requestBody = GeminiRequest.builder()
                .contents(Collections.singletonList(
                        GeminiRequest.Content.builder()
                                .role("user")
                                .parts(Collections.singletonList(
                                        GeminiRequest.Part.builder()
                                                .text(fullPrompt)
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(GeminiRequest.GenerationConfig.builder()
                        .responseMimeType("application/json")
                        .build())
                .build();

        String fullUri = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", geminiModel, geminiApiKey.trim());

        // Retry logic: thử tối đa 3 lần với backoff 5s, 10s, 20s
        int maxRetries = 3;
        long[] delays = {5000, 10000, 20000};

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                GeminiResponse response = aiWebClient.post()
                        .uri(java.net.URI.create(fullUri))
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(GeminiResponse.class)
                        .block();

                if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                    String jsonText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
                    try {
                        return objectMapper.readValue(jsonText, ModerationResult.class);
                    } catch (Exception e) {
                        log.error("Lỗi khi parse JSON từ Gemini: {}", jsonText, e);
                        throw new RuntimeException("Parse JSON thất bại");
                    }
                }
                return generateSystemError();
            } catch (org.springframework.web.reactive.function.client.WebClientResponseException wce) {
                String responseBody = wce.getResponseBodyAsString();
                log.error("Gemini Eval API Error Body: {}", responseBody);
                String errMsg = wce.getMessage() != null ? wce.getMessage() : "";
                if ((errMsg.contains("503") || errMsg.contains("429")) && attempt < maxRetries) {
                    log.warn("[RETRY {}/{}] Gemini Evaluation API tạm thời không khả dụng. Thử lại sau {}s...",
                            attempt, maxRetries, delays[attempt - 1] / 1000);
                    try { Thread.sleep(delays[attempt - 1]); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    throw wce;
                }
            } catch (RuntimeException re) {
                if (re.getMessage() != null && re.getMessage().contains("Parse JSON")) throw re;
                String errMsg = re.getMessage() != null ? re.getMessage() : "";
                if ((errMsg.contains("503") || errMsg.contains("429")) && attempt < maxRetries) {
                    log.warn("[RETRY {}/{}] Gemini Evaluation API tạm thời không khả dụng. Thử lại sau {}s...",
                            attempt, maxRetries, delays[attempt - 1] / 1000);
                    try { Thread.sleep(delays[attempt - 1]); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    throw re;
                }
            }
        }
        return generateSystemError();
    }

    private ModerationResult generateSystemError() {
        return ModerationResult.builder()
                .isClean(false)
                .courseViolations(Collections.singletonList("Hệ thống kiểm duyệt AI gặp lỗi kết nối hoặc phân tích JSON."))
                .lessonViolations(Collections.emptyList())
                .build();
    }
}
