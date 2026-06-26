package com.swp391.coding_platform.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.swp391.coding_platform.dto.response.CloudinaryResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {
    Cloudinary cloudinary;

    public CloudinaryResponse uploadFile(MultipartFile file, String folderName) throws IOException {
        Map<String , Object> uploadParams = ObjectUtils.asMap(
                "folder", folderName,
                "resource_type", "auto"
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        return CloudinaryResponse.builder()
                .publicId(uploadResult.get("public_id").toString())
                .secureUrl(uploadResult.get("secure_url").toString())
                .build();
    }

    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Đã xóa thành công file trên Cloudinary với publicId: {}", publicId);
        } catch (IOException e) {
            log.error("Lỗi khi xóa file trên Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Không thể xóa file cũ trên mây");
        }
    }
}
