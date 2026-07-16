# TÀI LIỆU GROUND TRUTH: HỆ THỐNG KIỂM THỬ TỰ ĐỘNG UI (PLAYWRIGHT)

Tài liệu này đóng vai trò là **Ground Truth Knowledge** (Dữ liệu gốc), chứa đựng toàn bộ phân tích kỹ thuật được dịch ngược trực tiếp từ mã nguồn bộ test tự động (Playwright) của dự án **SWP391 AI-Assisted Coding Audit Platform**. Tài liệu được biên soạn nhằm cung cấp ngữ cảnh chính xác tuyệt đối phục vụ cho các công cụ AI Generative biên soạn tài liệu kiểm thử phần mềm chuyên nghiệp.

---

## 1. Tổng quan Kiến trúc Framework (Framework Architecture)

### Công nghệ cốt lõi
*   **Công cụ kiểm thử chính:** [Playwright Test](https://playwright.dev/) phiên bản `^1.61.1` (thư viện `@playwright/test`).
*   **Môi trường thực thi:** Node.js với gói hỗ trợ định kiểu `@types/node` phiên bản `^26.1.1`.
*   **Ngôn ngữ lập trình:** JavaScript (sử dụng định dạng module hệ thống CommonJS được thiết lập trong `package.json`, nhưng viết mã nguồn theo cú pháp ES Modules hiện đại `import/export`).

### Cấu hình Playwright (`playwright.config.js`)
*   **Thư mục chứa mã nguồn kiểm thử (`testDir`):** `./tests` (quét toàn bộ các file đuôi `.spec.js`).
*   **Trình duyệt kiểm thử (Browser Projects):** Chạy duy nhất trên trình duyệt mã nguồn mở **Chromium** mô phỏng môi trường máy tính (`Desktop Chrome`).
*   **Tốc độ và Hiệu năng thực thi:**
    *   `workers: 1`: Giới hạn số lượng tiến trình chạy đồng thời bằng 1 để đảm bảo các ca kiểm thử chạy tuần tự, tránh xung đột tài khoản đăng nhập hoặc dữ liệu trên cơ sở dữ liệu dùng chung.
    *   `fullyParallel: true`: Cấu hình cho phép chạy song song các file, nhưng thực tế bị khống chế bởi cấu hình worker nêu trên.
    *   `launchOptions.slowMo: 2000`: Cơ chế làm chậm tất cả hành động giao diện (`click`, `fill`, `select`) thêm **2 giây** mỗi bước để người kiểm thử có thể theo dõi trực tiếp dòng chảy nghiệp vụ trên màn hình.
    *   `timeout`: Một số kịch bản phức tạp hoặc tương tác với môi trường bên ngoài tự nâng giới hạn thời gian chờ lên **90 giây** (`test.setTimeout(90000)`) để chống hiện tượng đứt gãy kết nối (flakiness).
*   **Môi trường chạy mặc định (`baseURL`):** `http://localhost:5173` (Frontend chạy trên React/Vite).
*   **Báo cáo kiểm thử (`reporter`):** Sử dụng trình kết xuất báo cáo HTML (`html` reporter), tạo ra một thư mục `playwright-report` chứa trang web tương tác hiển thị chi tiết kết quả chạy, ảnh chụp màn hình, video và dấu vết kiểm thử nếu có.
*   **Ghi dấu vết kiểm thử (`trace`):** Cấu hình `'on-first-retry'` (chỉ ghi nhận và xuất file trace viewer khi một kịch bản bị lỗi ở lần chạy đầu tiên và được kích hoạt cơ chế chạy lại).

### Mô hình thiết kế (Design Pattern)
*   **Tình trạng áp dụng POM:** Hệ thống **KHÔNG** áp dụng mô hình Page Object Model (POM). Không có bất kỳ lớp đối tượng giao diện (Page Object Class) nào được định nghĩa trong thư mục kiểm thử.
*   **Cách thức triển khai:** Toàn bộ các bộ định vị phần tử (Locators) như CSS selectors, text-matching, xpath và các hành động tương tác (`fill`, `click`, `selectOption`, `hover`) được **viết trực tiếp (inline)** bên trong từng khối mã kịch bản `test(...)`. 
*   **Lợi ích & Hạn chế thực tế:** Giúp mã kiểm thử ngắn gọn, trực diện, dễ đọc hiểu nhanh cho từng file riêng lẻ; tuy nhiên, sẽ gặp khó khăn khi bảo trì nếu giao diện thay đổi các class CSS hoặc thuộc tính HTML.

### Cấu trúc thư mục (Directory Tree)
```
automation-tests/
├── package.json               # Định nghĩa thư viện phụ thuộc và các lệnh chạy kiểm thử
├── playwright.config.js       # File cấu hình cấu hình trình duyệt, timeout, report của Playwright
└── tests/                     # Thư mục chứa toàn bộ 17 kịch bản kiểm thử (.spec.js)
    ├── admin.spec.js          # Điều hướng và kiểm tra bảng điều khiển admin
    ├── admin_problems.spec.js # Tạo bài tập lập trình mới từ tài khoản admin
    ├── ai_tutor.spec.js       # Tương tác với trợ lý học tập AI mô phỏng thuật toán
    ├── auth.spec.js           # Kịch bản đăng nhập, đăng xuất và kiểm tra xác thực sai
    ├── checkout.spec.js       # Quy trình nạp tiền giả lập và thanh toán giỏ hàng
    ├── contest_solving.spec.js# Tương tác phòng thi đang diễn ra và nộp bài làm
    ├── contests.spec.js       # Xem danh sách cuộc thi, đăng ký và xem bảng xếp hạng cuộc thi
    ├── courses.spec.js        # Xem danh sách khóa học và thêm khóa học trả phí vào giỏ hàng
    ├── instructor.spec.js     # Đăng ký làm giảng viên, kiểm tra bảng điều khiển giảng viên
    ├── instructor_course_builder.spec.js # Tạo bản thảo khóa học mới kèm tải ảnh thumbnail
    ├── legal.spec.js          # Xác minh các trang thông tin pháp lý và liên hệ hỗ trợ
    ├── problems.spec.js       # Duyệt danh sách bài tập, viết code và nộp bài trên Playground
    ├── profile_settings.spec.js # Cập nhật thông tin cá nhân (Display Name) của học viên
    ├── rankings.spec.js       # Xem bảng xếp hạng toàn cầu và tìm kiếm lập trình viên
    ├── register.spec.js       # Quy trình đăng ký tài khoản mới và xử lý lỗi trùng/khớp mật khẩu
    ├── search_filters.spec.js # Lọc bài tập theo độ khó và tìm kiếm theo từ khóa
    └── security.spec.js       # Kiểm tra bảo mật phân quyền (RBAC) đối với các trang được bảo vệ
```

---

## 2. Ma trận Kịch bản Kiểm thử Tự động (Automation Test Matrix)

Dưới đây là bảng chi tiết của toàn bộ **28 kịch bản kiểm thử** được trích xuất và dịch ngược logic từ 17 file kiểm thử trong thư mục `tests/`:

| ID | Tên Kịch Bản (Test Case Title) | Module/Tính năng | Các Bước Thực Hiện (Test Steps) | Điểm Kiểm Chứng (Expected Results / Assertions) | Data Test Mẫu |
|---|---|---|---|---|---|
| **TC_01** | `should fail to login with invalid credentials` | Đăng nhập (`auth.spec.js`) | 1. Điều hướng tới trang `/login`.<br>2. Nhập username `"wrong_user"` vào trường đầu vào.<br>3. Nhập password `"wrong_password"` vào trường mật khẩu.<br>4. Click vào nút đăng nhập (`button[type="submit"]`). | • Xuất hiện hộp thông báo lỗi màu đỏ (`div.bg-red-50`).<br>• Nội dung thông báo lỗi chứa chính xác chuỗi: `"Invalid username or password"`. | • Username: `wrong_user`<br>• Password: `wrong_password` |
| **TC_02** | `should login and logout successfully with valid credentials` | Đăng nhập & Đăng xuất (`auth.spec.js`) | 1. Điều hướng tới trang `/login`.<br>2. Nhập tài khoản học viên hợp lệ.<br>3. Click nút đăng nhập.<br>4. Xác minh hệ thống chuyển hướng thành công.<br>5. Truy cập trực tiếp trang `/dashboard`.<br>6. Di chuột qua vùng chứa ảnh đại diện (`header div.group`).<br>7. Click vào nút "Logout" xuất hiện trong menu dropdown. | • Đăng nhập xong phải chuyển hướng đến URL hợp lệ (chứa `/dashboard` hoặc `/instructor`).<br>• Sau khi bấm Logout, hệ thống phải chuyển hướng người dùng quay lại trang đăng nhập `/login`. | • Username: `user1`<br>• Password: `user1` |
| **TC_03** | `should show validation error when passwords do not match` | Đăng ký tài khoản (`register.spec.js`) | 1. Điều hướng tới trang `/register`.<br>2. Nhập các trường thông tin đăng ký nhưng điền hai mật khẩu khác nhau.<br>3. Tích chọn ô đồng ý điều khoản (`input[name="terms"]`).<br>4. Click nút đăng ký (`button[type="submit"]`). | • Xuất hiện cảnh báo lỗi màu đỏ (`div.bg-red-50`).<br>• Thông điệp hiển thị đúng nội dung: `"Password and confirm password do not match!"`. | • Fullname: `Mismatched User`<br>• Username: `mismatch_user`<br>• Email: `mismatch@example.com`<br>• Password: `password123`<br>• Confirm: `different123` |
| **TC_04** | `should register a new account successfully with unique username` | Đăng ký tài khoản (`register.spec.js`) | 1. Điều hướng tới trang `/register`.<br>2. Sinh tên đăng nhập và email duy nhất bằng cách đính kèm timestamp hiện tại.<br>3. Điền đầy đủ thông tin đăng ký hợp lệ và khớp mật khẩu.<br>4. Tích chọn ô đồng ý điều khoản.<br>5. Click nút đăng ký. | • Tài khoản được đăng ký thành công mà không gặp lỗi trùng lặp.<br>• Hệ thống tự động chuyển hướng người dùng sang trang `/dashboard`. | • Fullname: `Fullname [timestamp]`<br>• Username: `user_[timestamp]`<br>• Email: `user_[timestamp]@example.com`<br>• Password: `password123` |
| **TC_05** | `should redirect unauthenticated guest to login page when accessing protected admin route` | Bảo mật phân quyền (`security.spec.js`) | 1. Trình duyệt chưa đăng nhập cố gắng truy cập trực tiếp vào URL quản trị viên `/admin`. | • Hệ thống chặn quyền truy cập và tự động chuyển hướng khách về trang `/login`. | • Khách vô danh (Unauthenticated Guest) |
| **TC_06** | `should redirect unauthenticated guest to home page when accessing protected instructor route` | Bảo mật phân quyền (`security.spec.js`) | 1. Trình duyệt chưa đăng nhập cố gắng truy cập trực tiếp vào URL giảng viên `/instructor`. | • Hệ thống chặn quyền truy cập và tự động chuyển hướng khách về trang chủ (`/`). | • Khách vô danh (Unauthenticated Guest) |
| **TC_07** | `should redirect unauthenticated guest to home page when accessing protected dashboard route` | Bảo mật phân quyền (`security.spec.js`) | 1. Trình duyệt chưa đăng nhập cố gắng truy cập trực tiếp vào URL học viên `/dashboard`. | • Hệ thống chặn quyền truy cập và tự động chuyển hướng khách về trang chủ (`/`). | • Khách vô danh (Unauthenticated Guest) |
| **TC_08** | `should render Access Denied page when a normal student attempts to access admin route` | Bảo mật phân quyền (`security.spec.js`) | 1. Đăng nhập hệ thống với tài khoản học viên thông thường.<br>2. Đảm bảo chuyển hướng thành công đến trang học viên.<br>3. Truy cập trực tiếp đường dẫn quản trị `/admin`. | • Hệ thống không chuyển hướng về `/login` mà hiển thị một giao diện từ chối truy cập.<br>• Tiêu đề hiển thị `"Access Denied"` (`h3`).<br>• Có dòng chữ thông báo người dùng không đủ quyền hạn. | • Username: `user1`<br>• Password: `user1` |
| **TC_09** | `should navigate to profile settings, update Display Name, and verify header update` | Cấu hình cá nhân (`profile_settings.spec.js`) | 1. Đăng nhập hệ thống bằng tài khoản học viên.<br>2. Truy cập trực tiếp tab thiết lập cá nhân qua hash URL `/dashboard#my-profile`.<br>3. Tìm trường nhập tên hiển thị và điền tên mới kèm mã timestamp.<br>4. Click nút "Save Changes".<br>5. Chờ thông báo thành công xuất hiện.<br>6. Di chuột vào phần tử avatar ở header. | • Thông báo `"Profile details updated successfully!"` phải hiển thị.<br>• Trong phần hiển thị thông tin ở menu header phải cập nhật và hiển thị chính xác Display Name mới. | • Tên mới: `Playwright User - [timestamp]`<br>• Tài khoản: `user1` / `user1` |
| **TC_10** | `should browse problems list and select a problem` | Luyện tập lập trình (`problems.spec.js`) | 1. Đăng nhập hệ thống bằng tài khoản học viên.<br>2. Điều hướng tới danh sách bài tập `/problems`.<br>3. Kiểm tra tiêu đề trang "Problems" có hiển thị.<br>4. Tìm và click vào liên kết của phần tử bài tập đầu tiên trong bảng (`tbody tr td a`). | • Hệ thống chuyển hướng thành công sang trang giải bài tập `/problems/:id`.<br>• Tab mô tả ("Description") hiển thị đầy đủ.<br>• Trình soạn thảo Monaco Code Editor tải lên thành công (chờ tối đa 45 giây). | • Tài khoản: `user1` / `user1` |
| **TC_11** | `should choose language and submit code solution` | Luyện tập lập trình (`problems.spec.js`) | 1. Đăng nhập hệ thống và truy cập thẳng trang giải bài tập số 1 `/problems/1`.<br>2. Chờ Monaco Editor sẵn sàng.<br>3. Chọn ngôn ngữ lập trình **Python** (value `'71'`) từ dropdown.<br>4. Click vào khung soạn thảo Monaco và gõ ký tự bình luận `#`.<br>5. Khởi tạo lắng nghe hộp thoại để bỏ qua hội thoại lỗi hệ thống.<br>6. Click nút "Submit". | • Trình duyệt thực hiện gửi mã nguồn.<br>• Trong vòng tối đa 30 giây, kết quả kiểm thử (Verdict như Accepted, Wrong Answer) hiển thị tại tab `#tab-result h3` HOẶC thông báo bảo trì hệ thống chấm bài ("currently under maintenance") được hiển thị trơn tru. | • Ngôn ngữ: `Python` (ID: 71)<br>• Mã nguồn nộp: `#`<br>• Tài khoản: `user1` / `user1` |
| **TC_12** | `should filter problems by difficulty correctly` | Tìm kiếm & Bộ lọc (`search_filters.spec.js`) | 1. Điều hướng tới trang danh sách bài tập `/problems`.<br>2. Chọn giá trị độ khó `'easy'` tại dropdown bộ lọc thứ hai.<br>3. Đợi giao diện cập nhật trạng thái lọc (500ms).<br>4. Đếm số lượng nhãn độ khó "Easy" màu xanh lục trong bảng. | • Số lượng bài tập tìm thấy phải lớn hơn 0.<br>• Tất cả nhãn độ khó của các hàng hiển thị trong bảng đều phải là chữ `"Easy"`. | • Bộ lọc độ khó: `easy` |
| **TC_13** | `should search for problems using the search input` | Tìm kiếm & Bộ lọc (`search_filters.spec.js`) | 1. Điều hướng tới trang danh sách bài tập `/problems`.<br>2. Nhập từ khóa `"Sum"` vào ô nhập liệu tìm kiếm.<br>3. Nhấn phím `Enter` để thực thi tìm kiếm.<br>4. Đợi giao diện lọc (500ms) và quét qua các liên kết bài tập. | • Số lượng kết quả hiển thị lớn hơn 0.<br>• Tất cả các tiêu đề bài tập tìm được đều chứa chuỗi chữ `"sum"` (không phân biệt hoa thường). | • Từ khóa tìm kiếm: `Sum` |
| **TC_14** | `should view rankings page and search for a coder` | Bảng xếp hạng (`rankings.spec.js`) | 1. Điều hướng tới trang `/rankings`.<br>2. Kiểm tra tiêu đề trang `"Global Rankings"` và ô tìm kiếm `#rank-search` có hiển thị.<br>3. Kiểm tra các tab chu kỳ xếp hạng (Weekly, Monthly, All-Time) có hiển thị.<br>4. Nhập từ khóa tìm kiếm `"admin"` vào ô tìm kiếm. | • Các cấu phần giao diện bảng xếp hạng hoạt động ổn định.<br>• Việc nhập từ khóa tìm kiếm thực hiện bình thường không phát sinh lỗi. | • Từ khóa tìm kiếm: `admin` |
| **TC_15** | `should navigate to contact us page and verify elements` | Hỗ trợ & Điều khoản (`legal.spec.js`) | 1. Điều hướng trực tiếp tới trang liên hệ `/contact`. | • Tiêu đề trang chứa chữ "Contact" hoặc "Support" hiển thị.<br>• Các trường nhập tên, email và ô nhập nội dung mô tả hỗ trợ đều hiển thị đầy đủ trên màn hình. | • Không có |
| **TC_16** | `should navigate to terms of service page and verify` | Hỗ trợ & Điều khoản (`legal.spec.js`) | 1. Điều hướng trực tiếp tới trang điều khoản `/terms`. | • Tiêu đề trang "Terms of Service" hoặc "Terms and Conditions" hiển thị đầy đủ. | • Không có |
| **TC_17** | `should navigate to privacy policy page and verify` | Hỗ trợ & Điều khoản (`legal.spec.js`) | 1. Điều hướng trực tiếp tới trang chính sách bảo mật `/privacy`. | • Tiêu đề trang "Privacy Policy" hiển thị đầy đủ. | • Không có |
| **TC_18** | `should navigate to cookies policy page and verify` | Hỗ trợ & Điều khoản (`legal.spec.js`) | 1. Điều hướng trực tiếp tới trang chính sách cookies `/cookies`. | • Tiêu đề trang "Cookie Policy" hoặc "Cookies Policy" hiển thị đầy đủ. | • Không có |
| **TC_19** | `should ask AI Tutor to simulate algorithm and time complexity` | Trợ lý học tập AI (`ai_tutor.spec.js`) | 1. Giả lập (Mock) API: Chặn API `/api/v1/ai/visualizer/generate` và trả về thuật toán `"Binary Search"`, độ phức tạp `"O(log n)"`, nội dung mô phỏng HTML dạng iframe.<br>2. Đăng nhập hệ thống bằng tài khoản học viên.<br>3. Truy cập trang giải bài tập số 1 `/problems/1`.<br>4. Chờ Monaco Editor hiển thị.<br>5. Click chọn tab "AI Tutor".<br>6. Click nút "Ask AI". | • Các thành phần giao diện của AI Tutor hiển thị đầy đủ.<br>• Trình hiển thị trực quan iframe (`iframe[title="AI Visualizer"]`) xuất hiện.<br>• Các huy hiệu hiển thị độ phức tạp thời gian (`⏱ O-Big:`) và tên thuật toán (`🚀 Algorithm:`) hiển thị dữ liệu giả lập thành công. | • Dữ liệu mock: `Binary Search`, `O(log n)`<br>• Tài khoản: `user1` / `user1` |
| **TC_20** | `should browse courses catalog and view course detail` | Mua khóa học (`courses.spec.js`) | 1. Đăng nhập hệ thống bằng tài khoản học viên.<br>2. Điều hướng tới trang khóa học `/courses`.<br>3. Kiểm tra tiêu đề trang chứa chữ "Courses" hiển thị.<br>4. Xác định thẻ khóa học đầu tiên (`a.course-card`) và click. | • Hệ thống chuyển hướng thành công sang trang chi tiết khóa học `/courses/:id`.<br>• Hiển thị nút hành động như "Add to Cart", "Continue Learning" hoặc "Go to Cart". | • Tài khoản: `user1` / `user1` |
| **TC_21** | `should add a paid course to cart from list` | Mua khóa học (`courses.spec.js`) | 1. Đăng nhập hệ thống.<br>2. Vào `/shopping-cart` và click "Remove All" để dọn sạch giỏ hàng nếu có.<br>3. Quay lại trang `/courses`.<br>4. Tìm nút "Add to cart" (`button[title="Add to cart"]`) đầu tiên của khóa học có phí và click. | • Huy hiệu số lượng ở biểu tượng giỏ hàng trên thanh header (`header a[href="/shopping-cart"] span.bg-primary`) hiển thị.<br>• Số lượng khóa học trong giỏ hàng lớn hơn 0. | • Tài khoản: `user1` / `user1` |
| **TC_22** | `should mock wallet balance and complete checkout successfully` | Thanh toán giỏ hàng (`checkout.spec.js`) | 1. Đăng nhập hệ thống.<br>2. Vào `/shopping-cart`, bấm "Remove All" để dọn giỏ hàng.<br>3. Vào `/courses`, click nút thêm giỏ hàng của khóa học đầu tiên.<br>4. Giả lập (Mock) API: Chặn API ví tiền `/payment/balance` trả về số dư ví `2000000` VNĐ; chặn API `/orders/checkout` trả về kết quả thanh toán thành công `true`.<br>5. Vào trang `/shopping-cart`.<br>6. Xác minh số dư ví giả lập hiển thị trên màn hình.<br>7. Click nút "Checkout". | • Số dư ví hiển thị số tiền giả lập chứa chuỗi `"2.000.000"`.<br>• Xuất hiện thông điệp thông báo thành công `"Checkout successful!"`.<br>• Hệ thống tự động chuyển hướng người dùng về trang `/dashboard`. | • Giả lập số dư ví: `2000000`<br>• Tài khoản: `user1` / `user1` |
| **TC_23** | `should browse contests list and enter a contest arena` | Đấu trường thi đấu (`contests.spec.js`) | 1. Đăng nhập hệ thống.<br>2. Điều hướng tới trang `/contests`.<br>3. Đợi spinner tải dữ liệu biến mất.<br>4. Click nút "Enter Arena" đầu tiên.<br>5. Xác minh chuyển hướng đến trang đấu trường `/contests/:id`.<br>6. Kiểm tra tiêu đề đấu trường ở sidebar (`aside h2:has-text("Contest #")`).<br>7. Nếu nút "Register Now" xuất hiện, click đăng ký và xác minh hiển thị badge xanh "Registered".<br>8. Nếu phòng thi đang diễn ra hoặc đã kết thúc, tiến hành click tab "Problems" và "Rankings" để kiểm tra. | • Vào chi tiết phòng thi đấu thành công.<br>• Thực hiện đăng ký tham gia thi đấu thành công.<br>• Nếu kỳ thi đang diễn ra hoặc đã kết thúc, bảng xếp hạng thi đấu (`table`) và danh sách bài tập phải hiển thị thành công. | • Tài khoản: `user1` / `user1` |
| **TC_24** | `should enter ongoing contest, select problem and submit code successfully` | Đấu trường thi đấu (`contest_solving.spec.js`) | 1. Đăng nhập hệ thống.<br>2. Giả lập (Mock) API phòng thi số `99` (ONGOING) và bài tập số `9901` kèm mã nguồn mẫu Python.<br>3. Điều hướng tới `/contests`.<br>4. Click nút "Enter Arena" của cuộc thi giả lập.<br>5. Click liên kết "Problems" ở sidebar để mở danh sách bài tập.<br>6. Click chọn bài tập `"Contest Sum Problem"`.<br>7. Chờ Monaco Editor hiển thị.<br>8. Click nút "Submit". | • Chuyển hướng thành công đến chi tiết bài thi có URL định dạng `/contests/99/problems/9901`.<br>• Khi click Submit, nút chuyển sang trạng thái đang nộp bài ("Submitting") hoặc nút "Submit" hiển thị bình thường mà không bị lỗi giao diện. | • Cuộc thi giả lập ID: `99`<br>• Bài thi giả lập ID: `9901`<br>• Tài khoản: `user1` / `user1` |
| **TC_25** | `should navigate through dashboard statistics, my courses and revenue tabs` | Giảng viên quản lý (`instructor.spec.js`) | 1. Đăng nhập hệ thống.<br>2. Điều hướng tới trang `/apply-instructor`.<br>3. Đăng ký nếu chưa là giảng viên (nhập tên, chọn major, điền bio và click Đăng ký) rồi vào bảng điều khiển. Nếu đã là giảng viên, click nút chuyển sang bảng giảng viên `/instructor`.<br>4. Tại tab Overview (`#dashboard`), xác minh hiển thị các thông số thống kê chính.<br>5. Tại tab Authored Courses (`#my-courses`), click "Create Course" để mở modal nhập liệu. Nhập tiêu đề khóa học `"Automated Playwright Course"`, chọn danh mục, sau đó click nút đóng để đóng modal.<br>6. Tại tab Doanh thu (`#revenue`), xác minh hiển thị doanh thu. | • Điều hướng thành công tới `/instructor`.<br>• Các thẻ "Total Students", "Active Courses", và "Total Revenue"/"Revenue Wallet" hiển thị.<br>• Modal tạo khóa học mở ra đúng tiêu đề, đóng lại bình thường.<br>• Tab doanh thu hiển thị mục "Earnings Overview". | • Tài khoản: `user1` / `user1`<br>• Khóa học nháp: `Automated Playwright Course` |
| **TC_26** | `should open creation modal, fill inputs, and save a draft course` | Giảng viên quản lý (`instructor_course_builder.spec.js`) | 1. Đăng nhập hệ thống.<br>2. Điều hướng tới `/instructor#my-courses`.<br>3. Click nút "Create Course".<br>4. Giả lập (Mock) API: Chặn API upload media trả về ảnh CDN; chặn API tạo khóa học trả về bản thảo thành công.<br>5. Nhập tiêu đề khóa học chứa timestamp.<br>6. Mở dropdown chọn topic và chọn chủ đề đầu tiên.<br>7. Nhập giá tiền `100000`.<br>8. Chọn ảnh tải lên giả định.<br>9. Điền mô tả ngắn và mô tả chi tiết của khóa học.<br>10. Click nút "Submit Course". | • Hệ thống gửi thông tin tạo khóa học thành công.<br>• Hiển thị thông báo Toast chứa thông điệp thành công `"successfully created"`. | • Tiêu đề: `Automated React Course - [timestamp]`<br>• Giá tiền: `100000`<br>• Tài khoản: `user1` / `user1` |
| **TC_27** | `should navigate through admin dashboard panels and verify grids` | Quản trị viên (`admin.spec.js`) | 1. Đăng nhập hệ thống bằng tài khoản admin quản trị.<br>2. Kiểm tra chuyển hướng thành công đến trang `/admin`.<br>3. Xác minh hiển thị thẻ "Active Users" và "Total Courses".<br>4. Tìm và click liên kết "Courses Management" trong sidebar.<br>5. Tìm và click liên kết "Problems Management" trong sidebar.<br>6. Tìm và click liên kết "Contest Management" trong sidebar.<br>7. Tìm và click liên kết "Instructor Management" trong sidebar.<br>8. Tìm và click liên kết "Users Directory" trong sidebar.<br>9. Tìm và click liên kết "Financial Dashboard" trong sidebar. | • Chuyển hướng đến các trang con thành công:<br>• `/admin/courses` (tiêu đề Courses Management)<br>• `/admin/problems` (tiêu đề Programming Problems)<br>• `/admin/contests` (tiêu đề Competitive Contests)<br>• `/admin/instructors` (tiêu đề Platform Instructors)<br>• `/admin/users` (tiêu đề Platform Users)<br>• `/admin/financial` (tiêu đề Financial Statistics hoặc Financial Overview) | • Username: `admin`<br>• Password: `admin` |
| **TC_28** | `should navigate to creation page and submit a new coding problem` | Quản trị viên (`admin_problems.spec.js`) | 1. Đăng nhập hệ thống bằng tài khoản admin quản trị.<br>2. Điều hướng tới `/admin/problems`.<br>3. Click nút "Create Problem".<br>4. Nhập tiêu đề bài tập chứa timestamp.<br>5. Nhập mô tả bài tập, mô tả đầu vào/đầu ra, ràng buộc, ví dụ dữ liệu mẫu.<br>6. Nhập giới hạn số học: Điểm (`100`), Time Limit (`1000`), Memory Limit (`64000`).<br>7. Click nhãn độ khó "EASY".<br>8. Click nút "Create Problem & Testcases". | • Chuyển hướng thành công sang trang tạo bài tập `/admin/problems-create`.<br>• Tạo bài tập thành công và tự động chuyển hướng người dùng quay lại trang danh sách bài tập của admin `/admin/problems` trong vòng tối đa 15 giây. | • Tiêu đề: `Automated Code Problem - [timestamp]`<br>• Điểm: 100, Time: 1000ms, Memory: 64MB<br>• Username: `admin`<br>• Password: `admin` |

---

## 3. Chiến lược Quản lý Dữ liệu Kiểm thử (Test Data Strategy)

Bộ test tự động của hệ thống SWP391 áp dụng linh hoạt ba chiến lược quản lý dữ liệu chính để đảm bảo tính cô lập, tốc độ và độ tin cậy khi thực thi:

### A. Dữ liệu tài khoản cố định (Hardcoded Credentials)
Hệ thống sử dụng các cặp tài khoản được gán cứng đại diện cho các vai trò chính trong ứng dụng để tiến hành xác thực trước khi kiểm tra các chức năng cần quyền hạn:
*   **Học viên & Giảng viên:** Sử dụng tài khoản đăng nhập `user1` / mật khẩu `user1`. Đây là tài khoản đóng vai trò học viên thông thường, đồng thời có kịch bản nộp đơn và được duyệt nâng cấp lên quyền Giảng viên (Instructor).
*   **Quản trị viên hệ thống (Super Admin):** Sử dụng tài khoản đăng nhập `admin` / mật khẩu `admin`. Tài khoản này nắm quyền lực tối cao, có khả năng duyệt xem báo cáo tài chính, quản lý danh sách người dùng, phê duyệt giảng viên và quản trị ngân hàng đề bài tập/cuộc thi.

### B. Dữ liệu ngẫu nhiên theo thời gian thực (Dynamic & Time-based Data)
Để tránh xung đột trùng lặp khóa chính hoặc ràng buộc dữ liệu duy nhất (Unique Constraints) trong cơ sở dữ liệu khi chạy nhiều lần trên cùng một môi trường dev, các trường thông tin mang tính định danh được sinh ngẫu nhiên bằng cách nối thêm giá trị timestamp của hệ thống tại thời điểm chạy (`Date.now()`):
*   **Đăng ký tài khoản (`register.spec.js`):** Username được sinh tự động dưới dạng `user_[timestamp]` và Email là `user_[timestamp]@example.com`.
*   **Cập nhật Display Name (`profile_settings.spec.js`):** Tên hiển thị mới được cập nhật dưới dạng `Playwright User - [timestamp]`.
*   **Tạo khóa học mới (`instructor_course_builder.spec.js`):** Tiêu đề khóa học mới có dạng `Automated React Course - [timestamp]`.
*   **Tạo bài tập lập trình mới (`admin_problems.spec.js`):** Tiêu đề bài tập có định dạng `Automated Code Problem - [timestamp]`.

### C. Cơ chế Giả lập API phía Client (Client-side API Mocking & Interception)
Đối với các tính năng phụ thuộc nhiều vào các dịch vụ bên ngoài, thời gian xử lý của compiler lâu, hoặc cần số dư tài khoản lớn, bộ test sử dụng tính năng `page.route()` của Playwright để chặn các yêu cầu HTTP (intercept) và trả về kết quả giả lập (mocking) ngay lập tức:
*   **Giả lập phản hồi AI Tutor (`ai_tutor.spec.js`):** Chặn API `/api/v1/ai/visualizer/**` để giả lập dữ liệu phản hồi nhanh từ hệ thống trợ lý học tập bao gồm thuật toán Binary Search và Big-O complexity giúp giao diện dựng khung hình nhanh chóng mà không cần chờ AI thực tế xử lý.
*   **Giả lập Số dư ví & Đơn hàng thanh toán (`checkout.spec.js`):** Chặn API ví tiền `/payment/balance` để trả về số dư mặc định là `2.000.000` VNĐ và chặn API `/orders/checkout` trả về trạng thái `true` (thành công) để kiểm thử luồng mua hàng mà không phát sinh giao dịch tiền tệ thực tế.
*   **Giả lập Đấu trường thi đấu (`contest_solving.spec.js`):** Chặn toàn bộ các API liên quan đến phòng thi đấu có ID `99` và bài tập `9901` để chạy trơn tru kịch bản nộp bài trong phòng thi mà không cần tạo trước phòng thi thật trên cơ sở dữ liệu.
*   **Giả lập Tải tệp tin (File Upload):** Khi tạo khóa học, bộ test truyền dữ liệu ảnh giả lập trực tiếp thông qua API dạng Buffer (`Buffer.from('fake-image-content')`) và mock API upload media `/instructor/upload` để lấy URL CDN giả, giúp tăng tốc quá trình nộp biểu mẫu.

---

## 4. Hướng dẫn Thực thi & Cấu hình Báo cáo (Execution & Reporting)

### Lệnh thực thi trong tệp `package.json`
Các câu lệnh CLI sau được cấu hình sẵn trong phần `scripts` để thực thi bộ test:

*   **Chạy toàn bộ kiểm thử ở chế độ ẩn danh (Headless Mode):**
    ```bash
    npm run test
    ```
    *Lệnh này sẽ gọi trình thi hành `playwright test` để chạy toàn bộ các kịch bản trong thư mục `tests`.*

*   **Xem báo cáo kết quả kiểm thử sau khi chạy:**
    ```bash
    npm run report
    ```
    *Lệnh này gọi `playwright show-report` để dựng một máy chủ cục bộ và hiển thị kết quả trực quan trên trình duyệt.*

### Lệnh chạy nâng cao và Debug trực quan
Bên cạnh các lệnh đóng gói sẵn, lập trình viên và QA có thể sử dụng các lệnh trực tiếp của Playwright để tăng khả năng kiểm soát:

*   **Chạy ở chế độ giao diện tương tác (Interactive UI Mode):**
    ```bash
    npx playwright test --ui
    ```
    *Khuyên dùng trong quá trình viết code test hoặc gỡ lỗi, cho phép chạy từng test case lẻ, xem lại timeline giao diện, xem trước locators và kiểm tra các API Mocking.*

*   **Chạy gỡ lỗi từng bước (Debug Mode):**
    ```bash
    npx playwright test --debug
    ```
    *Mở công cụ Playwright Inspector giúp đi từng dòng code test và kiểm tra sự thay đổi của DOM.*
