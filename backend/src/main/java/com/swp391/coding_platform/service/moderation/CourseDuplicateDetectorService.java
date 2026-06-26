package com.swp391.coding_platform.service.moderation;

import com.swp391.coding_platform.dto.moderation.GeminiEmbeddingRequest;
import com.swp391.coding_platform.dto.moderation.GeminiEmbeddingResponse;
import com.swp391.coding_platform.repository.course.CourseRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseDuplicateDetectorService {

    private final WebClient aiWebClient;
    private final CourseRepository courseRepository;

    public CourseDuplicateDetectorService(
            @org.springframework.beans.factory.annotation.Qualifier("aiWebClient") WebClient aiWebClient,
            CourseRepository courseRepository
    ) {
        this.aiWebClient = aiWebClient;
        this.courseRepository = courseRepository;
    }

    @Value("${ai.gemini-api-key:}")
    private String geminiApiKey;

    // 1. Gọi Gemini API để sinh vector embedding (768 chiều)
    public List<Double> getEmbedding(String text) {
        log.info("Gọi Gemini API sinh embedding cho văn bản dài {} ký tự...", text.length());
        
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY chưa được khai báo trong cấu hình hệ thống.");
        }

        GeminiEmbeddingRequest requestBody = GeminiEmbeddingRequest.of(text);
        String uri = String.format("/v1beta/models/gemini-embedding-001:embedContent?key=%s", geminiApiKey.trim());

        GeminiEmbeddingResponse response = aiWebClient.post()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiEmbeddingResponse.class)
                .block();

        if (response != null && response.getEmbedding() != null) {
            return response.getEmbedding().getValues();
        }

        throw new RuntimeException("Gemini Embeddings API trả về kết quả rỗng.");
    }

    // 2. Lưu trữ vector vào database (REQUIRES_NEW: transaction độc lập, không ảnh hưởng transaction cha)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCourseEmbedding(Long courseId, String courseText) {
        try {
            List<Double> vector = getEmbedding(courseText);
            String vectorString = formatVectorForSql(vector);
            courseRepository.saveCourseEmbedding(courseId, vectorString);
            log.info("Đã lưu trữ thành công vector embedding cho khóa học ID: {}", courseId);
        } catch (Exception e) {
            log.error("Lỗi khi tạo/lưu vector embedding cho khóa học ID: {} - {}", courseId, e.getMessage());
            // Không re-throw: lỗi embedding không nên chặn toàn bộ moderation pipeline
        }
    }

    // 3. Đối chiếu trùng lặp khóa học
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public DuplicationCheckResult checkDuplication(Long courseId, String courseText) {
        log.info("Bắt đầu kiểm tra trùng lặp cho khóa học ID: {}", courseId);
        try {
            List<Double> vector = getEmbedding(courseText);
            String vectorString = formatVectorForSql(vector);

            // Tìm top 3 khóa học tương đồng nhất
            List<Object[]> matches = courseRepository.findDuplicateCourses(vectorString, courseId, 3);
            if (matches != null && !matches.isEmpty()) {
                Object[] bestMatch = matches.get(0);
                Long matchedCourseId = ((Number) bestMatch[0]).longValue();
                Double similarity = ((Number) bestMatch[1]).doubleValue();

                log.info("Khóa học tương đồng nhất ID: {}, Độ tương đồng: {}", matchedCourseId, similarity);
                return new DuplicationCheckResult(matchedCourseId, similarity, similarity > 0.90);
            }
        } catch (Exception e) {
            log.error("Lỗi trong quá trình kiểm tra trùng lặp khóa học ID: {}", courseId, e);
        }
        return new DuplicationCheckResult(null, 0.0, false);
    }

    // Chuyển List<Double> thành chuỗi [x1, x2, x3...] tương thích với pgvector cast
    private String formatVectorForSql(List<Double> vector) {
        return "[" + vector.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    @Getter
    @AllArgsConstructor
    public static class DuplicationCheckResult {
        private final Long matchedCourseId;
        private final Double similarityScore;
        private final boolean isDuplicate;
    }
}
