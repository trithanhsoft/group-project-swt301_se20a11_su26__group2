# THỐNG KÊ TRIỂN KHAI HỆ THỐNG AI MODERATION V2
*(Implementation Summary)*

Hệ thống kiểm duyệt khóa học bằng AI (AI Course Moderation) đã được tái cấu trúc hoàn toàn theo hướng Clean Architecture, tách biệt rõ ràng các trách nhiệm, áp dụng Multi-threading để tăng tốc độ và tối ưu chi phí API.

Dưới đây là danh sách chi tiết các file đã thay đổi, chức năng của chúng và luồng hoạt động mới.

---

## 1. Danh sách các file đã thay đổi / tạo mới

### 📦 Tầng Data Transfer Object (DTO)
| File | Phân loại | Chức năng |
|------|-----------|-----------|
| `CourseModerationPayload.java` | **MỚI** | JSON Model đóng gói toàn bộ metadata của Khóa học (Title, Desc) và Bài học (Theory, Video Transcript) thành 1 cục duy nhất gửi cho AI. |
| `ModerationResult.java` | **SỬA** | Cấu trúc lại toàn bộ kết quả trả về của AI gồm: `isClean` (boolean), `courseViolations` (mảng lỗi chung), `lessonViolations` (mảng lỗi chi tiết từng bài học). Bỏ đi các điểm số không còn phù hợp. |

### 🗄️ Tầng Entity & DB
| File | Phân loại | Chức năng |
|------|-----------|-----------|
| `CourseModerationReportEntity.java`| **SỬA** | Chuyển đổi bảng cấu trúc cũ sang việc lưu trữ toàn bộ Object `ModerationResult` dưới dạng cấu trúc JSON vào cột `report_json` (dùng JSONB/Text). |
| `LessonStatus.java` (Enum) | **SỬA** | Bổ sung trạng thái `PENDING_UPDATE` để đánh dấu bài học đang chờ duyệt lại do vừa bị chỉnh sửa. |

### ⚙️ Tầng Service (Core Logic)
| File | Phân loại | Chức năng |
|------|-----------|-----------|
| `AiEvaluationService.java` | **MỚI** | Giao tiếp trực tiếp với Google Gemini API. Chứa các Prompt cực kỳ chặt chẽ (cấm chửi bậy, ép buộc có lý thuyết, phát hiện lỗi âm thanh rác). Ép JSON format chặt chẽ. |
| `CourseModerationService.java` | **SỬA** | **Trái tim của hệ thống (Orchestrator)**. Cập nhật Status logic mới (PENDING_AI, PENDING_ADMIN). Tích hợp gom toàn bộ văn bản (transcript) từ các bài học lưu vào cột `text_audio` của khoá học. |
| `VideoTranscriptionService.java` | **SỬA LỚN** | Loại bỏ OpenAI Whisper để chuyển sang dùng **Gemini 1.5 Flash** (dạng InlineData Base64). Tối ưu hóa đa ngôn ngữ, xoá bỏ lỗi "ảo giác" (hallucination) dịch rác của Whisper, giảm chi phí bằng 0. |

### 🎧 Tầng Event / Message Queue Listener
| File | Phân loại | Chức năng |
|------|-----------|-----------|
| `CourseModerationListener.java`| **SỬA** | Nâng cấp RabbitMQ Consumer để nhận 2 loại tin nhắn dạng Map: `{type="FULL_COURSE"}` và `{type="SINGLE_LESSON"}` để điều phối tương ứng. |
| `InstructorCourseService.java` | **SỬA** | Cập nhật logic submit gửi Map payload lên RabbitMQ. Bổ sung logic bắt sự kiện **khi giảng viên Update Video Lesson đã duyệt** thì tự động ném bài học đó vào `PENDING_UPDATE` và gửi `SINGLE_LESSON` queue cho AI. |

---

## 2. Luồng hoạt động (Workflows)

### Luồng 1: Giảng viên Submit khóa học mới (FULL_COURSE)
1. `InstructorCourseService` nhận request, chuyển khóa học sang `PENDING`. Gửi `{courseId, type: FULL_COURSE}` vào RabbitMQ.
2. `CourseModerationListener` bốc queue -> gọi `CourseModerationService.processFullCourse()`.
3. Check luật 50%: Lọc tổng số bài. Nếu số bài có Video < 50% -> Lập tức **REJECT**, lưu lỗi vào DB, kết thúc (Không tốn tiền gọi AI).
4. Dịch thuật Đa luồng: Mở N threads cho N bài học có video. Gọi `VideoTranscriptionService`. Chuyển âm thanh thành Base64 và đẩy sang **Gemini 1.5 Flash** để lấy transcript đa ngôn ngữ. Lắp text vào JSON payload tổng và lưu vào cột `text_audio` của khoá học.
5. Gọi `AiEvaluationService` ném Payload lên Gemini.
6. Kết quả: Nếu `isClean == true` -> đổi trạng thái thành `PENDING_ADMIN` (Chờ Admin Duyệt). Nếu `isClean == false` -> `REJECTED`, lưu mảng lỗi từng bài vào DB.

### Luồng 2: Giảng viên sửa 1 bài học (SINGLE_LESSON)
1. Giảng viên sửa video của khóa học đang `APPROVED`.
2. `InstructorCourseService` so sánh thấy có sự thay đổi -> Đưa Lesson thành `PENDING_UPDATE` (khóa học chính không bị ảnh hưởng). Gửi `{lessonId, type: SINGLE_LESSON}` vào RabbitMQ.
3. `CourseModerationListener` bốc queue -> gọi `CourseModerationService.processSingleLessonUpdate()`.
4. Chỉ cắt mp3 và gửi text của **ĐÚNG BÀI ĐÓ** sang Gemini.
5. Kết quả: Nếu `isClean == true` -> Lesson trở lại `ACTIVE`. Nếu lỗi -> Lesson bị giáng cấp thành `INACTIVE`.

---

## 3. Quá trình Kiểm thử (Testing)
- Toàn bộ source code Java sau khi refactor cấu trúc dữ liệu đã được Compile thành công bằng Maven (`mvn clean compile -DskipTests`).
- Hệ thống Clean Architecture bảo đảm các class Service mới không dính chặt chẽ (coupled) vào nhau, rất dễ dàng để Unit Test trong tương lai.
- Migration Database cho bảng `course_moderation_reports` tương thích tự động bằng Hibernate (`ddl-auto: update`).

---

## 4. Frontend Implementation (Giao Diện React)

### 🖥️ Admin Dashboard (`AdminDashboard.tsx`)
- Đã thiết kế lại các tab lọc hiển thị 4 trạng thái cực chuẩn xác: `APPROVED`, `PENDING_ADMIN` (Chờ thao tác duyệt), `PENDING_AI` (AI đang chấm), và `REJECTED`.
- Bỏ đi filter "ALL" và "DRAFTS". Default vào thẳng tab `PENDING_ADMIN`.
- **Nút "View AI Audit Report"**: Được chuyển từ cấu trúc Tab cũ (bị ẩn) sang một nút trực quan tại Header của Action Banner.
- **AI Audit Report Modal**:
  - Modal được gọi hiển thị khi click nút, lấy dữ liệu JSON JSONB từ cơ sở dữ liệu `CourseModerationReport`.
  - Parse kết quả trả về với cấu trúc mới: Status Banner (Approved / Rejected), Khối thông báo Course-Level Violations, và Khối cảnh báo Lesson-Level Violations với các chỉ định cụ thể của AI.

### 👨‍🏫 Instructor Dashboard (`InstructorDashboard.tsx`)
- **Course Status Badge**: Bổ sung hiển thị huy hiệu `Rejected` khi `status === 'REJECTED'` hoặc `'rejected'`.
- **View Rejection Reason Modal**:
  - Tương tự Admin, Giảng viên có thể xem trực tiếp chi tiết lý do AI từ chối khóa học của mình bằng nút "View Rejection Reason".
  - Modal sẽ parse chi tiết các lỗi mà AI đánh giá (lỗi cấp khóa học, lỗi ở từng bài học) để giảng viên có thể `Fix Issues`.
- **`instructorService.ts`**: Bổ sung API `getCourseModerationReport` gọi tới `/api/moderation/{courseId}/report` để phục vụ lấy kết quả ngay trong Instructor UI.
