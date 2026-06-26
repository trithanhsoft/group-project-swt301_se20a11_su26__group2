# TÀI LIỆU KIẾN TRÚC & WORKFLOW: HỆ THỐNG KIỂM DUYỆT KHÓA HỌC BẰNG AI
*(AI Course Moderation Architecture)*

---

## 1. Cấu trúc 3-Tier Layer & SOLID
Hệ thống được đập đi xây lại theo chuẩn Clean Architecture, phân tách rõ ràng trách nhiệm của từng class. Các class cũ rườm rà sẽ bị xóa bỏ.

### 🔴 Controller Layer (Giao tiếp API)
*Nhận Request từ Frontend, trả về Response chuẩn, không chứa Business Logic.*
- `InstructorCourseController`: Mở API cho Giảng viên nộp khóa học (`submit-review`) hoặc cập nhật bài học.
- `AdminCourseModerationController`: Cung cấp API cho Admin xem báo cáo lỗi chi tiết từng Lesson và chốt duyệt (APPROVE/REJECT tay).

### 🟡 Service Layer (Business Logic & Orchestration)
*Chia nhỏ Interface theo chuẩn Interface Segregation (I trong SOLID).*

**A. Orchestrator (Điều phối chính):**
- `CourseModerationService` (Interface) -> `CourseModerationServiceImpl`:
  - Chịu trách nhiệm tổng hợp toàn bộ Workflow. 
  - Giao tiếp với DB (Repository) để lấy thông tin.
  - Chứa logic bắt buộc (Pre-check): Ràng buộc 50% video, bắt buộc có Theory.

**B. External Integrations (Tích hợp hệ thống ngoài):**
- `VideoTranscriptionService`: Xử lý mảng Media (Video -> Audio -> Text). Tích hợp chạy Đa luồng (CompletableFuture) trong class này.
- `AiEvaluationService`: Chuyên đóng gói JSON, gọi API Google Gemini, và parse kết quả JSON trả về thành Object.

**C. Message Queue Listener:**
- `ModerationTaskConsumer`: Lắng nghe RabbitMQ. Nhận ID và gọi đến `CourseModerationService` để xử lý ngầm, đảm bảo luồng API chính không bị block.

### 🟢 Repository Layer (Database Access)
- `CourseModerationReportRepository`: Lưu lại mảng JSON lỗi của từng bài học để Admin và Giảng viên xem.
- `CourseRepository`, `LessonRepository`: Truy xuất và cập nhật trạng thái (`PENDING`, `REJECTED`, `WAITING_FOR_ADMIN`, `PENDING_UPDATE`).

---

## 2. Workflows Chi Tiết

### Workflow 1: Nộp khóa học MỚI (DRAFTS -> Duyệt toàn bộ)

**1. Gửi Yêu Cầu (API)**
- **Giảng viên gọi API:** `PUT /api/v1/instructor/courses/{courseId}/submit-review`
- **Controller** gọi `InstructorCourseService.submitCourseForReview()`.
- **Hành động:** 
  - Đổi trạng thái Course thành `PENDING`.
  - Bắn tin nhắn `{ "courseId": 123, "type": "FULL_COURSE" }` vào RabbitMQ.
  - Trả về `200 OK` ngay lập tức cho Giảng viên.

**2. Xử lý Ngầm (Background Worker)**
- `ModerationTaskConsumer` nhận tin nhắn từ RabbitMQ.
- **Bước 1 (Pre-check):** `CourseModerationService` đếm số lượng Lesson. Nếu vi phạm (Không có bài nào có video, hoặc số video < 50%) -> Bỏ qua AI, lưu DB lỗi, chuyển Course thành `REJECTED`.
- **Bước 2 (Multi-threading):** Gọi `VideoTranscriptionService.transcribeCourseVideos(lessons)`. Khởi tạo N luồng song song cắt mp4 và gọi OpenAI Whisper. Chờ toàn bộ xong (Timeout 15 phút).
- **Bước 3 (Build JSON Payload):** Tạo file JSON chuẩn (Lưu ý: Đối với phần Quiz, AI chỉ đánh giá ngôn từ xem có vi phạm chuẩn mực/thuần phong mỹ tục không, KHÔNG cần kiểm tra tính đúng/sai của đáp án):
  ```json
  {
    "courseTitle": "Lập trình Java",
    "chapters": [
       {
         "title": "Chương 1", 
         "lessons": [
            { 
               "lessonId": 1, 
               "theory": "...", 
               "videoTranscript": "...", 
               "quizzes": [
                  {
                     "title": "Quiz kiểm tra bài 1",
                     "question": "Java là ngôn ngữ lập trình gì?",
                     "options": ["Hướng đối tượng", "Hướng hàm", "Cả A và B"]
                  }
               ]
            }
         ]
       }
    ]
  }
  ```
- **Bước 4 (Gọi AI):** Gửi JSON cho `AiEvaluationService`. AI check thuần phong mỹ tục, từ ngữ, chất lượng text rác.
- **Bước 5 (Nhận Response & Lưu DB):**
  - Nếu Response trả về `isClean == true`: Cập nhật Course thành `WAITING_FOR_ADMIN_APPROVAL`.
  - Nếu `isClean == false`: Cập nhật Course thành `REJECTED`. Lưu mảng lỗi (`lessonViolations`) vào bảng Report.

---

### Workflow 2: Cập nhật Bài Học (Đã duyệt -> Sửa Lesson)

**1. Gửi Yêu Cầu (API)**
- **Giảng viên gọi API:** `PUT /api/v1/instructor/courses/{courseId}/lessons/{lessonId}` (Sửa video/text của bài học).
- **Controller** lưu nội dung mới vào DB. 
- **Hành động:** Đổi trạng thái riêng của `Lesson` này thành `PENDING_UPDATE` (Khóa học vẫn sống, bài này tạm ẩn).
- Bắn tin nhắn `{ "lessonId": 456, "type": "SINGLE_LESSON" }` vào RabbitMQ.

**2. Xử lý Ngầm (Background Worker)**
- `ModerationTaskConsumer` nhận tin nhắn.
- Lấy riêng thông tin Lesson đó. Gọi `VideoTranscriptionService` xử lý đúng 1 video đó.
- Gửi cho Gemini một Payload nhỏ gọn (Chỉ chứa Title, Theory, Transcript, và danh sách Quizzes gồm: title, question, options của bài đó).
- **Kết quả:**
  - `isClean == true`: Đổi Lesson thành `ACTIVE` (Hiển thị lại cho học viên).
  - `isClean == false`: Đổi Lesson thành `REJECTED` (Báo lỗi cho Giảng viên sửa, không ảnh hưởng cả khóa học).

---

## 3. Cấu Trúc Báo Cáo Mới (Lưu vào DB)

Bảng `CourseModerationReport` sẽ chứa 1 trường cấu trúc JSON như sau (để phục vụ vẽ giao diện Admin siêu dễ):
```json
{
  "status": "REJECTED",
  "courseLevelViolations": [
     "Khóa học chỉ có 20% video, không đạt chuẩn tối thiểu 50%."
  ],
  "lessonLevelViolations": [
     {
        "lessonId": 45,
        "lessonTitle": "Bài 2: Vòng lặp For",
        "violationType": "BAD_AUDIO",
        "reason": "Text trả về toàn từ vô nghĩa lặp lại 'Cảm ơn, subcribe'. Chất lượng âm thanh quá tệ."
     },
     {
        "lessonId": 46,
        "lessonTitle": "Bài 3: Cấu trúc If",
        "violationType": "PROFANITY",
        "reason": "Trong video có sử dụng ngôn từ thô tục."
     }
  ]
}
```

## 4. Các Class "Rác" sẽ bị xóa / Refactor mạnh
- `AiModerationClient.java` (Sẽ bị xé nhỏ ra thành cấu trúc Interface/Impl theo hướng Service).
- `CourseModerationListener.java` (Sẽ bị gỡ bỏ mớ logic code lộn xộn `for...if` và thay bằng Orchestrator).
- Xóa các logic tính điểm `qualityScore`, `riskScore`, `confidenceScore` vô nghĩa, thay bằng logic `isClean` và `violation_list` thực tế.
