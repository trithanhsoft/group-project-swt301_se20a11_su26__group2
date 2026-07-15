package com.swp391.coding_platform.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.swp391.coding_platform.dto.response.CloudinaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void testUploadFile_Success() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        String folderName = "test-folder";

        Map<String, Object> uploadResult = Map.of(
                "public_id", "test-folder/test-public-id",
                "secure_url", "https://res.cloudinary.com/test-url"
        );

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        CloudinaryResponse response = cloudinaryService.uploadFile(file, folderName);

        assertNotNull(response);
        assertEquals("test-folder/test-public-id", response.getPublicId());
        assertEquals("https://res.cloudinary.com/test-url", response.getSecureUrl());
        verify(uploader, times(1)).upload(any(byte[].class), anyMap());
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        String publicId = "test-folder/test-public-id";

        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(Map.of("result", "ok"));

        assertDoesNotThrow(() -> cloudinaryService.deleteFile(publicId));

        verify(uploader, times(1)).destroy(eq(publicId), anyMap());
    }

    @Test
    void testDeleteFile_Failure() throws IOException {
        String publicId = "test-folder/test-public-id";

        when(uploader.destroy(eq(publicId), anyMap())).thenThrow(new IOException("Cloudinary error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cloudinaryService.deleteFile(publicId));
        assertEquals("Không thể xóa file cũ trên mây", exception.getMessage());
        verify(uploader, times(1)).destroy(eq(publicId), anyMap());
    }
}
