# AI Agent Prompt & Blueprint: Google OAuth2 Login Feature Integration

Tài liệu này đóng vai trò như một **bản thiết kế kỹ thuật (Blueprint) và Prompt** chi tiết dành cho AI Agent để xây dựng tính năng đăng nhập bằng Google (Google OAuth2 Login) cho một dự án Spring Boot + React tương tự.

---

## 🎯 GOAL / MỤC TIÊU
Tích hợp tính năng đăng nhập bằng Google (Google Login) cho hệ thống Spring Boot (Backend) và React (Frontend) sử dụng cơ chế bảo mật JWT lưu trữ trong HttpOnly Cookies.
* **Frontend**: Sử dụng Google Identity Services SDK để lấy `idToken` (Identity Token JWT của Google) từ Client và gửi lên Backend.
* **Backend**: Kiểm tra và xác thực chữ ký của `idToken` trực tiếp qua Google API Client. Sau đó liên kết hoặc đăng ký mới tài khoản người dùng, phát hành nội bộ Access Token & Refresh Token của hệ thống, và trả về HttpOnly Cookies.

---

## 🛠️ PHẦN 1: CẤU HÌNH & DEPENDENCY (BACKEND)

Thêm thư viện Google API Client vào file quản lý phụ thuộc (ví dụ Maven `pom.xml`):

```xml
<!-- Google API Client for OAuth2 Identity Verification -->
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.2.0</version> <!-- Hoặc phiên bản mới nhất -->
</dependency>
```

Cấu hình khóa ứng dụng Google Client ID trong file cấu hình (`application.yaml` hoặc `application.properties`):

```yaml
google:
  client-id: ${GOOGLE_CLIENT_ID:YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com}
```

---

## 💾 PHẦN 2: THIẾT KẾ CƠ SỞ DỮ LIỆU & ENTITIES

Cần có một bảng riêng biệt để liên kết các tài khoản mạng xã hội (OAuth) với tài khoản chính của hệ thống. Điều này cho phép một tài khoản hệ thống liên kết với nhiều nhà cung cấp khác nhau (Google, Facebook, Github) mà không phá vỡ cấu trúc cơ bản của bảng User.

### 1. Thực thể `UserOauthAccountEntity`
Tạo bảng liên kết OAuth với các trường: `id`, `user_id` (Khóa ngoại trỏ sang User), `provider` (ví dụ: "google"), `provider_user_id` (chứa `sub` - ID duy nhất của người dùng trên Google), và `created_at`.

```java
package com.swp391.coding_platform.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_oauth_accounts", schema = "public", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
})
public class UserOauthAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @Column(name = "provider", nullable = false, length = 50)
    String provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    String providerUserId;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt = Instant.now();
}
```

### 2. Repository `UserOauthAccountRepository`
Cung cấp truy vấn tìm kiếm tài khoản liên kết:

```java
package com.swp391.coding_platform.repository.user;

import com.swp391.coding_platform.entity.user.UserOauthAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserOauthAccountRepository extends JpaRepository<UserOauthAccountEntity, Integer> {
    Optional<UserOauthAccountEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
```

---

## 📩 PHẦN 3: DTO REQUEST & RESPONSE

### 1. `GoogleLoginRequest` (Nhận ID Token từ Client)
```java
package com.swp391.coding_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleLoginRequest {
    @NotBlank(message = "ID Token is required")
    String idToken;
}
```

### 2. `AuthenticationResponse` (Chứa thông tin đăng nhập thành công)
Chứa các trường cơ bản của User (id, username, displayName, email, avatarUrl, roles). Token JWT thực tế sẽ không trả về Body của response mà được tự động đóng gói vào Cookies phía Controller.

---

## 🧠 PHẦN 4: THỰC THI LOGIC NGHIỆP VỤ (SERVICE)

Trong `AuthenticationService.java`, cần thực hiện các công việc sau tại phương thức `googleLogin`:
1. Sử dụng `GoogleIdTokenVerifier` để gửi yêu cầu và xác thực chữ ký token.
2. Trích xuất thông tin người dùng từ payload (`sub`, `email`, `name`, `picture`).
3. Thực hiện kiểm tra cơ sở dữ liệu:
   * Nếu đã liên kết: Lấy người dùng liên kết và cập nhật ảnh đại diện mới nhất (nếu thay đổi).
   * Nếu chưa liên kết:
     * Tìm User theo email của Google. Nếu có, liên kết trực tiếp.
     * Nếu không có, tạo mới User với username dạng ngẫu nhiên không trùng lặp, cấp quyền mặc định `USER` và bắn sự kiện đăng ký thành công `UserRegisteredEvent`.
     * Tạo bản ghi liên kết mới lưu vào `UserOauthAccountEntity`.
4. Sinh Access Token và Refresh Token JWT để trả về.

```java
@Value("${google.client-id:}")
String GOOGLE_CLIENT_ID;

@Transactional
public AuthenticationResponse googleLogin(GoogleLoginRequest request) {
    try {
        // 1. Xác thực ID Token với Google Client Library
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(request.getIdToken());
        if (idToken == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 2. Lấy thông tin từ payload
        GoogleIdToken.Payload payload = idToken.getPayload();
        String subjectId = payload.getSubject(); // Google User ID
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        // 3. Kiểm tra xem tài khoản liên kết OAuth đã tồn tại chưa
        Optional<UserOauthAccountEntity> oauthAccountOpt = userOauthAccountRepository.findByProviderAndProviderUserId("google", subjectId);
        UserEntity userEntity;

        if (oauthAccountOpt.isPresent()) {
            userEntity = oauthAccountOpt.get().getUser();
            // Cập nhật ảnh đại diện nếu có thay đổi từ phía Google
            if (userEntity.getAvatarurl() == null || !userEntity.getAvatarurl().equals(pictureUrl)) {
                userEntity.setAvatarurl(pictureUrl);
                userRepository.save(userEntity);
            }
        } else {
            // Trường hợp tài khoản chưa từng liên kết Google
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                userEntity = userOpt.get(); // Nếu đã trùng email hệ thống thì liên kết trực tiếp
            } else {
                // Tạo mới tài khoản người dùng
                String baseUsername = email.contains("@") ? email.split("@")[0] : email;
                String username = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 5);
                while(userRepository.existsByUsername(username)) {
                    username = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 5);
                }

                userEntity = UserEntity.builder()
                        .username(username)
                        .email(email)
                        .displayname(name != null ? name : baseUsername)
                        .avatarurl(pictureUrl)
                        .build();

                // Gán quyền USER mặc định cho người dùng mới
                var userRole = roleRepository.findByName(RoleName.USER)
                        .orElseGet(() -> roleRepository.save(RoleEntity.builder().name(RoleName.USER).build()));
                userEntity.setRoles(Set.of(userRole));
                userEntity = userRepository.save(userEntity);

                // Publish Event chào mừng/đăng ký nếu cần thiết
                applicationEventPublisher.publishEvent(UserRegisteredEvent.builder().userEntity(userEntity).build());
            }

            // Tạo liên kết OAuth Account
            UserOauthAccountEntity oauthAccount = UserOauthAccountEntity.builder()
                    .user(userEntity)
                    .provider("google")
                    .providerUserId(subjectId)
                    .build();
            userOauthAccountRepository.save(oauthAccount);
        }

        // 4. Sinh và trả về JWT Tokens
        String accessToken = generateToken(userEntity, false);
        String refreshToken = generateToken(userEntity, true);

        return buildAuthResponse(userEntity, accessToken, refreshToken);

    } catch (Exception e) {
        log.error("Google verify error", e);
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
```

---

## 🚦 PHẦN 5: CONTROLLER & SECURITY CONFIG

### 1. `AuthenticationController.java`
Tạo endpoint nhận `POST /auth/google`. Hàm này nhận payload và trả về dạng HttpOnly Cookie bảo mật.

```java
@PostMapping("/google")
public ResponseEntity<ApiResponse<AuthenticationResponse>> googleLogin(
        @RequestBody @Valid GoogleLoginRequest googleLoginRequest, HttpServletResponse response){

    AuthenticationResponse result = authenticationService.googleLogin(googleLoginRequest);
    addAuthCookies(response, result); // Thiết lập HttpOnly Cookie

    return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
            .status(200)
            .code(1000)
            .message("Google login successfully")
            .result(result)
            .timestamp(Instant.now().toString())
            .build());
}

private void addAuthCookies(HttpServletResponse response, AuthenticationResponse result) {
    ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", result.getAccessToken())
            .httpOnly(true)
            .secure(true) // Set true khi chạy HTTPS
            .path("/")
            .maxAge(accessTokenMaxAge)
            .sameSite("Lax")
            .build();

    ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", result.getRefreshToken())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(refreshTokenMaxAge)
            .sameSite("Lax")
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    // Xoá token thô trong JSON response body để bảo mật cao hơn
    result.setAccessToken(null);
    result.setRefreshToken(null);
}
```

### 2. `SecurityConfig.java`
Cho phép endpoint `/auth/google` bỏ qua kiểm tra đăng nhập bằng phương thức cấu hình bảo mật Spring Security:

```java
.requestMatchers("/auth/login", "/auth/register", "/auth/refresh", "/auth/google").permitAll()
```

---

## 💻 PHẦN 6: LUỒNG HOẠT ĐỘNG PHÍA CLIENT (FRONTEND REFERENCE)

### 1. Cài đặt thư viện React
```bash
npm install @react-oauth/google
```

### 2. Bao bọc ứng dụng bằng `GoogleOAuthProvider` (trong `App.tsx` hoặc `main.tsx`)
```tsx
import { GoogleOAuthProvider } from '@react-oauth/google';

const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

ReactDOM.createRoot(document.getElementById('root')!).render(
  <GoogleOAuthProvider clientId={googleClientId}>
    <App />
  </GoogleOAuthProvider>
);
```

### 3. Nút bấm Đăng nhập bằng Google (`Login.tsx`)
```tsx
import { GoogleLogin } from '@react-oauth/google';

<GoogleLogin
  onSuccess={async (credentialResponse) => {
    if (credentialResponse.credential) {
      // credentialResponse.credential chính là idToken từ Google
      const user = await googleLogin(credentialResponse.credential);
      // Xử lý chuyển hướng trang sau khi đăng nhập thành công
    }
  }}
  onError={() => {
    console.error("Đăng nhập Google thất bại");
  }}
/>
```

### 4. Gọi API Backend (`authService.ts`)
Gửi ID Token lên endpoint `/auth/google` với tùy chọn `credentials: 'include'` để trình duyệt tự động lưu và gửi cookie an toàn.

```typescript
async googleLogin(idToken: string): Promise<any> {
  const response = await fetch(`${BASE_URL}/auth/google`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include', // Bắt buộc để nhận HttpOnly Cookies
    body: JSON.stringify({ idToken }),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Đăng nhập Google thất bại');
  }

  const data = await response.json();
  return data.result;
}
```
