# Phân tích Thiết kế và Luồng hoạt động (Workflow) của Hệ thống Kiểm duyệt Khóa học bằng AI

Tài liệu này cung cấp cái nhìn chi tiết về kiến trúc, luồng xử lý dữ liệu (workflow) và các công nghệ cốt lõi được áp dụng trong tính năng kiểm duyệt khóa học tự động sử dụng trí tuệ nhân tạo (AI Course Moderation) dựa trên các commit hiện tại.

---

## 🏗️ Tổng quan Kiến trúc & Công nghệ sử dụng

Hệ thống được thiết kế theo dạng hướng sự kiện (Event-Driven Architecture) với các công nghệ chính:

1. **RabbitMQ**: Đảm nhận vai trò truyền tin không đồng bộ. Khi giảng viên nộp hoặc cập nhật khóa học, hệ thống đẩy một tin nhắn chứa `courseId` vào hàng đợi `MODERATION_QUEUE` để xử lý ngầm (background worker), tránh gây nghẽn UI của giảng viên.
2. **Spring Boot (Java)**: Đóng vai trò Backend chính điều phối toàn bộ vòng đời kiểm duyệt: trích xuất nội dung từ Database, gọi các service con, phân tích trùng lặp, gửi yêu cầu tới AI và áp dụng luật đưa ra quyết định cuối cùng.
3. **Dockerized FFmpeg Service (Node.js/Express)**: Một microservice chạy độc lập chịu trách nhiệm tải video (`.mp4`), xử lý tín hiệu âm thanh và chuyển đổi định dạng sang `.mp3` chất lượng tối ưu cho STT (Speech-to-Text).
4. **OpenAI Whisper API (`whisper-1`)**: Dịch vụ chuyển giọng nói thành văn bản (STT) chất lượng cao nhất hiện nay, cấu hình riêng cho tiếng Việt (`vi`).
5. **Google Gemini 2.5 Flash API**:
   - **Dự phòng (Fallback) cho STT**: Nhờ tính năng multimodal (đa phương thức), Gemini có thể nhận trực tiếp dữ liệu âm thanh dưới dạng Base64 và dịch sang text một cách miễn phí/chi phí thấp khi key OpenAI gặp sự cố.
   - **Kiểm định Nội dung (Content Moderation)**: Phân tích toàn bộ ngữ cảnh văn bản (metadata, lý thuyết, quiz, bản dịch video) theo định dạng JSON đầu ra nghiêm ngặt.
6. **Vector Embeddings (Plagiarism Checker)**: Đo lường độ tương đồng ngữ nghĩa của Metadata khóa học với các khóa học hiện có để phát hiện đạo văn/trùng lặp sớm.

---

## 🔄 Luồng hoạt động chi tiết (Workflow)

Dưới đây là sơ đồ luồng hoạt động từ lúc giảng viên cập nhật/nộp khóa học cho đến khi hệ thống tự động phê duyệt hoặc từ chối:

```text
Quy trình Kiểm duyệt Khóa học bằng AI (AI Course Moderation Workflow):

Bước 1: Giảng viên thực hiện Cập nhật hoặc Nộp khóa học.
Bước 2: Hệ thống Spring Boot tiếp nhận và đẩy `courseId` vào hàng đợi RabbitMQ (MODERATION_QUEUE).
Bước 3: Hàng đợi kích hoạt RabbitMQ Worker (CourseModerationListener) để xử lý không đồng bộ.
Bước 4: Trích xuất nội dung học thuật từ Database bao gồm Metadata (Tiêu đề, Mô tả), tài liệu lý thuyết của bài học và Bộ câu hỏi Quiz.
Bước 5: Kiểm tra Đạo văn / Trùng lặp (Plagiarism Check) bằng cách so khớp Vector Embeddings của Metadata với các khóa học đã tồn tại:
       ├─► NẾU TRÙNG LẶP (> Ngưỡng tương đồng): 
       │   └─► Đánh dấu lỗi "DUPLICATE_COURSE_DETECTED".
       │   └─► Cập nhật trạng thái khóa học là PENDING và cắm cờ needsAdminReview = true.
       │   └─► Chuyển sang Bước 10 (Chờ Admin xem xét thủ công).
       └─► NẾU HỢP LỆ (Không trùng lặp): Tiếp tục Bước 6.
Bước 6: Duyệt qua từng bài học (Lesson) trong chương để thu thập và xử lý tài liệu lý thuyết và video:
       ├─► NẾU BÀI HỌC CÓ VIDEO URL:
       │   ├─► Lệnh FFmpeg Service: Gọi HTTP POST sang ffmpeg-service để tải video và trích xuất tệp âm thanh dạng mono .mp3 (tần số 16kHz, bitrate 32kbps).
       │   ├─► Chuyển giọng nói thành văn bản (Speech-to-Text): Gửi tệp .mp3 lên OpenAI Whisper API (hoặc fallback tự động sang Gemini 2.5 Flash).
       │   └─► Ghép nội dung bản dịch (Transcript) vào bộ văn bản tổng hợp chung.
       └─► NẾU BÀI HỌC CHỈ CÓ LÝ THUYẾT:
           └─► Thu thập và ghép nội dung lý thuyết định dạng text thông thường vào bộ văn bản tổng hợp.
Bước 7: Tổng hợp toàn bộ dữ liệu văn bản đã tích lũy (Metadata + Docs + Quiz + Transcript video) thành một payload và gửi lên Google Gemini 2.5 Flash API.
Bước 8: Gemini API phân tích ngữ cảnh và trả về kết quả dưới định dạng JSON bao gồm: Điểm chất lượng (Quality Score), Điểm rủi ro (Risk Score), Điểm tin cậy (Confidence Score), Danh mục vi phạm được gắn cờ (Flagged Categories) và Lý do chi tiết bằng tiếng Việt (Reasons).
Bước 9: Áp dụng Ma trận quyết định (Decision Matrix) để cập nhật trạng thái thực tế của khóa học trong Database:
       ├─► TỰ ĐỘNG PHÊ DUYỆT (APPROVED): Nếu Risk < 0.15 AND Quality >= 0.75 AND Confidence >= 0.80. (Đặt needsAdminReview = false)
       ├─► TỰ ĐỘNG TỪ CHỐI (REJECTED): Nếu Risk >= 0.60 AND Confidence >= 0.80. (Đặt needsAdminReview = false)
       └─► CHỜ DUYỆT THỦ CÔNG (PENDING): Các trường hợp rủi ro trung bình, chất lượng chưa đạt, độ tự tin của AI thấp hoặc hệ thống gặp sự cố trong quá trình chạy. (Đặt needsAdminReview = true)
Bước 10: Lưu báo cáo kiểm duyệt (CourseModerationReport) vào Database và tự động tạo/lưu Vector Embedding mới của khóa học vừa được duyệt để phục vụ đối chiếu trùng lặp cho các khóa học nộp sau.
```

---

## 🛠️ Phân tích Kỹ thuật các chức năng Core

### 1. Trích xuất âm thanh từ Video (`.mp4` -> `.mp3`)
* **Kiến trúc**: Spring Boot gọi Node.js qua HTTP REST (sử dụng Spring `WebClient`).
* **Chi tiết FFmpeg Command**:
  Tại [index.js](../../ffmpeg-service/index.js) (hoặc xem tuyệt đối: [index.js](file:///Users/ngocthanh/Documents/Material%20SU26/SWP391/swp391-su26-ai-audit-project-swp391_se20a11_group-02/ffmpeg-service/index.js)), lệnh FFmpeg được chạy dưới dạng một tiến trình con (`child_process.spawn`):
  ```javascript
  ffmpeg -y -i <videoUrl> -vn -acodec libmp3lame -ac 1 -ar 16000 -ab 32k <outputAudioPath>
  ```
  * `-y`: Tự động ghi đè tệp đầu ra nếu tồn tại.
  * `-i <videoUrl>`: URL luồng video đầu vào (ví dụ: `.mp4`).
  * `-vn`: **Video None** - Bỏ qua luồng hình ảnh, chỉ lấy luồng âm thanh để tối ưu hóa hiệu năng xử lý.
  * `-acodec libmp3lame`: Sử dụng bộ mã hóa (encoder) âm thanh MP3 LAME chất lượng cao.
  * `-ac 1`: Chuyển đổi âm thanh thành kênh đơn (**Mono channel**) giúp giảm 1/2 dung lượng file.
  * `-ar 16000`: Thiết lập tần số lấy mẫu (**Sample Rate**) về 16kHz - đây là thông số chuẩn tối ưu cho các mô hình nhận diện giọng nói (STT).
  * `-ab 32k`: Thiết lập băng thông âm thanh (**Audio Bitrate**) ở mức 32kbps nhằm thu nhỏ tối đa kích thước tệp âm thanh gửi đi mà vẫn giữ rõ tiếng nói.

### 2. Dịch âm thanh thành văn bản (`.mp3` -> text)
Được triển khai trong [AiModerationClient.java](../src/main/java/com/swp391/coding_platform/service/moderation/AiModerationClient.java#L39-L131) (hoặc xem tuyệt đối: [AiModerationClient.java](file:///Users/ngocthanh/Documents/Material%20SU26/SWP391/swp391-su26-ai-audit-project-swp391_se20a11_group-02/backend/src/main/java/com/swp391/coding_platform/service/moderation/AiModerationClient.java#L39-L131)):
* **Lớp dịch chính (Whisper API)**: Gửi tệp tin `.mp3` dạng Multipart Form Data qua API OpenAI (`https://api.openai.com/v1/audio/transcriptions`).
  * Sử dụng model `whisper-1`.
  * Cấu hình tham số ngôn ngữ cố định là `vi` (tiếng Việt).
* **Lớp dịch dự phòng (Gemini Fallback)**:
  Nếu việc gọi Whisper thất bại (lỗi kết nối, hết hạn mức API, hết tiền tài khoản...), hệ thống tự động chuyển sang gọi Gemini 2.5 Flash thông qua Base64 Encoding:
  * Đọc toàn bộ bytes của file `.mp3` và mã hóa thành chuỗi Base64.
  * Đóng gói vào chuẩn định dạng inlineData (`mimeType: "audio/mp3"`).
  * Prompt chỉ dẫn cụ thể cho Gemini:
    > *"Bạn là chuyên gia chuyển giọng nói thành văn bản. Hãy dịch toàn bộ nội dung âm thanh này sang văn bản tiếng Việt chính xác. Không giải thích thêm, không thêm thắt bình luận, chỉ trả về nội dung đã nói trong âm thanh."*

### 3. Đánh giá chất lượng và kiểm duyệt nội dung bằng AI (AI Evaluation)
Sau khi thu thập đầy đủ văn bản từ: Metadata + Docs + Quiz + Video Transcripts, hệ thống gọi Gemini API bằng phương thức sinh nội dung có cấu trúc:

#### **System Prompt và Nhiệm vụ:**
```text
Bạn là Chuyên gia Kiểm định Chất lượng Giáo dục. Bạn được giao nhiệm vụ duyệt khóa học.
Hãy phân tích: Metadata (Tiêu đề, Mô tả), tài liệu đi kèm (Docs), bộ câu hỏi (Quiz), và bản ghi âm video bài giảng (Transcripts).
Lưu ý: Nếu transcript chứa [VIDEO_PRESENT_BUT_TRANSCRIPT_UNAVAILABLE], hãy đánh giá dựa trên các nội dung khác và đặt confidence thấp hơn.
Đánh giá và phản hồi kết quả kiểm duyệt dạng JSON khớp chính xác với cấu trúc Java DTO sau:
{
  "qualityScore": float (0.00 -> 1.00 - Điểm chất lượng sư phạm),
  "riskScore": float (0.00 -> 1.00 - Điểm rủi ro vi phạm chính sách),
  "confidenceScore": float (0.00 -> 1.00 - Điểm tin cậy của AI),
  "flaggedCategories": ["danh mục vi phạm (violence, adult_content, hate_speech, disintermediation)"],
  "reasons": "chuỗi lý do phân tích bằng tiếng Việt",
  "recommendedAction": "APPROVE" hoặc "REJECT" hoặc "REVIEW"
}
```

#### **Các tiêu chí đánh giá cốt lõi (Criteria):**
1. **Quality Score (Chất lượng)**: Đánh giá dựa trên độ rõ ràng của cấu trúc bài học, tính sư phạm của lý thuyết, độ chính xác và đa dạng của bộ câu hỏi trắc nghiệm (quiz), nội dung video có chứa kiến thức giảng dạy thật hay không.
2. **Risk Score (Rủi ro)**: Rà soát các vi phạm chính sách bao gồm:
   - *Violence*: Bạo lực, từ ngữ kích động.
   - *Adult Content*: Nội dung nhạy cảm, không phù hợp môi trường giáo dục.
   - *Hate Speech*: Kỳ thị, thù hằn.
   - *Disintermediation*: Giảng viên cố tình đưa số điện thoại, link ngoài, thông tin giao dịch riêng tư để lôi kéo học viên giao dịch trực tiếp không qua nền tảng.
3. **Confidence Score (Mức độ tin cậy)**: Cho biết mức độ chắc chắn của AI đối với quyết định. Sẽ bị kéo thấp nếu tài liệu bị thiếu hoặc video không thể dịch được âm thanh.

#### **Ma trận quyết định kiểm duyệt (Decision Matrix):**
Áp dụng tại Java listener:
```java
// Luật tự động Phê duyệt (APPROVED)
if (aiResult.getRiskScore() < 0.15 && aiResult.getQualityScore() >= 0.75 && aiResult.getConfidenceScore() >= 0.80) {
    finalStatus = CourseStatus.APPROVED;
    needsAdminReview = false;
}
// Luật tự động Từ chối (REJECTED)
else if (aiResult.getRiskScore() >= 0.60 && aiResult.getConfidenceScore() >= 0.80) {
    finalStatus = CourseStatus.REJECTED;
    needsAdminReview = false;
}
// Các trường hợp nghi ngờ / rủi ro trung bình / tự tin thấp: Chuyển duyệt thủ công (PENDING)
else {
    finalStatus = CourseStatus.PENDING;
    needsAdminReview = true;
}
```
* **Chế độ tự bảo vệ (Fail-Safe)**: Bất kỳ ngoại lệ nào xảy ra trong quá trình chạy Pipeline (mạng lỗi, API sập, lỗi code...) đều tự động bắt ngoại lệ (catch block), gán trạng thái khóa học về `PENDING`, cắm cờ `needsAdminReview = true` và ghi nhật ký lỗi chi tiết để quản trị viên có thể xem xét thủ công mà không làm gián đoạn tiến trình của người dùng.

---

## 📂 Liên kết mã nguồn liên quan
Bạn có thể tham khảo trực tiếp mã nguồn của các thành phần này tại các đường dẫn sau:
* [index.js (Dịch vụ FFmpeg)](../../ffmpeg-service/index.js) (hoặc [tuyệt đối](file:///Users/ngocthanh/Documents/Material%20SU26/SWP391/swp391-su26-ai-audit-project-swp391_se20a11_group-02/ffmpeg-service/index.js))
* [AudioProcessingService.java (Tích hợp FFmpeg)](../src/main/java/com/swp391/coding_platform/service/moderation/AudioProcessingService.java) (hoặc [tuyệt đối](file:///Users/ngocthanh/Documents/Material%20SU26/SWP391/swp391-su26-ai-audit-project-swp391_se20a11_group-02/backend/src/main/java/com/swp391/coding_platform/service/moderation/AudioProcessingService.java))
* [AiModerationClient.java (Gọi API OpenAI & Gemini)](../src/main/java/com/swp391/coding_platform/service/moderation/AiModerationClient.java) (hoặc [tuyệt đối](file:///Users/ngocthanh/Documents/Material%20SU26/SWP391/swp391-su26-ai-audit-project-swp391_se20a11_group-02/backend/src/main/java/com/swp391/coding_platform/service/moderation/AiModerationClient.java))
* [CourseModerationListener.java (RabbitMQ Listener & Decision Matrix)](../src/main/java/com/swp391/coding_platform/service/moderation/CourseModerationListener.java) (hoặc [tuyệt đối](file:///Users/ngocthanh/Documents/Material%20SU26/SWP391/swp391-su26-ai-audit-project-swp391_se20a11_group-02/backend/src/main/java/com/swp391/coding_platform/service/moderation/CourseModerationListener.java))
* [CourseDuplicateDetectorService.java (Chống trùng lặp)](../src/main/java/com/swp391/coding_platform/service/moderation/CourseDuplicateDetectorService.java) (hoặc [tuyệt đối](file:///Users/ngocthanh/Documents/Material%20SU26/SWP391/swp391-su26-ai-audit-project-swp391_se20a11_group-02/backend/src/main/java/com/swp391/coding_platform/service/moderation/CourseDuplicateDetectorService.java))
* [CourseContentExtractorService.java (Trích xuất nội dung)](../src/main/java/com/swp391/coding_platform/service/moderation/CourseContentExtractorService.java) (hoặc [tuyệt đối](file:///Users/ngocthanh/Documents/Material%20SU26/SWP391/swp391-su26-ai-audit-project-swp391_se20a11_group-02/backend/src/main/java/com/swp391/coding_platform/service/moderation/CourseContentExtractorService.java))
