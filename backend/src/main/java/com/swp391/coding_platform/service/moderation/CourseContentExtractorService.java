package com.swp391.coding_platform.service.moderation;

import com.swp391.coding_platform.entity.course.*;
import com.swp391.coding_platform.repository.course.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseContentExtractorService {

    private final ChapterRepository chapterRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    // 1. Trích xuất Metadata khóa học
    public String extractMetadata(CourseEntity course) {
        log.info("Bắt đầu trích xuất metadata cho khóa học ID: {}", course.getId());
        return String.format(
            "Tiêu đề: %s\nMô tả ngắn: %s\nMô tả chi tiết: %s\nNhững gì sẽ học: %s\nYêu cầu tiên quyết: %s\nĐối tượng mục tiêu: %s",
            course.getTitle(),
            course.getShortDescription(),
            course.getLongDescription(),
            course.getWhatYouLearn(),
            course.getPrerequisites(),
            course.getTargetAudience()
        );
    }

    // 2. Trích xuất tất cả Quiz trong khóa học dưới dạng Markdown cấu trúc
    public String extractQuizContent(Long courseId) {
        log.info("Bắt đầu trích xuất bộ câu hỏi trắc nghiệm (quiz) của khóa học ID: {}", courseId);
        StringBuilder sb = new StringBuilder();
        
        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        for (ChapterEntity chapter : chapters) {
            if (chapter.getLessons() == null) continue;
            for (LessonEntity lesson : chapter.getLessons()) {
                Optional<QuizEntity> quizOpt = quizRepository.findByLessonId(lesson.getId());
                if (quizOpt.isPresent()) {
                    QuizEntity quiz = quizOpt.get();
                    sb.append("### Lesson: ").append(lesson.getTitle()).append("\n");
                    sb.append("Quiz Title: ").append(quiz.getTitle()).append("\n");
                    
                    List<QuizQuestionEntity> questions = quizQuestionRepository.findByQuizIdWithOptions(quiz.getId());
                    int qIndex = 1;
                    for (QuizQuestionEntity question : questions) {
                        sb.append("Câu hỏi ").append(qIndex++).append(": ").append(question.getContent()).append("\n");
                        if (question.getOptions() != null) {
                            for (QuizOptionEntity option : question.getOptions()) {
                                sb.append("  - ").append(option.getContent());
                                if (Boolean.TRUE.equals(option.getIsCorrect())) {
                                    sb.append(" [ĐÁP ÁN ĐÚNG]");
                                }
                                sb.append("\n");
                            }
                        }
                        sb.append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    // 3. Trích xuất văn bản từ tệp PDF qua Cloud URL
    public String extractTextFromPdf(String pdfUrl) {
        log.info("Bắt đầu đọc tệp PDF từ URL: {}", pdfUrl);
        if (pdfUrl == null || pdfUrl.trim().isEmpty()) {
            return "";
        }
        try (InputStream in = new URL(pdfUrl).openStream()) {
            byte[] bytes = in.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                log.info("Đọc PDF thành công, độ dài văn bản: {}", text.length());
                return text;
            }
        } catch (Exception e) {
            log.error("Lỗi khi đọc file PDF từ url: {}", pdfUrl, e);
            return "[LỖI TRÍCH XUẤT PDF: " + e.getMessage() + "]";
        }
    }

    // 4. Trích xuất văn bản từ tệp Word (.docx) qua Cloud URL
    public String extractTextFromDocx(String docxUrl) {
        log.info("Bắt đầu đọc tệp Word DOCX từ URL: {}", docxUrl);
        if (docxUrl == null || docxUrl.trim().isEmpty()) {
            return "";
        }
        try (InputStream in = new URL(docxUrl).openStream();
             XWPFDocument doc = new XWPFDocument(in)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String text = extractor.getText();
            log.info("Đọc Word DOCX thành công, độ dài văn bản: {}", text.length());
            return text;
        } catch (Exception e) {
            log.error("Lỗi khi đọc file DOCX từ url: {}", docxUrl, e);
            return "[LỖI TRÍCH XUẤT DOCX: " + e.getMessage() + "]";
        }
    }
}
