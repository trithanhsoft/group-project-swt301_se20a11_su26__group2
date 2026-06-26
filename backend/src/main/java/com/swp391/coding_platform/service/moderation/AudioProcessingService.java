package com.swp391.coding_platform.service.moderation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
@Slf4j
public class AudioProcessingService {

    private final WebClient webClient;

    public AudioProcessingService(@Value("${ffmpeg.base-url:http://localhost:9000}") String ffmpegServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(ffmpegServiceUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(500 * 1024 * 1024))
                .build();
    }

    public boolean isFfmpegAvailable() {
        try {
            Map<?, ?> response = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null && Boolean.TRUE.equals(response.get("ffmpeg"));
        } catch (Exception e) {
            log.warn("Dịch vụ FFmpeg Docker không khả dụng tại URL cấu hình. Lỗi: {}", e.getMessage());
            return false;
        }
    }

    public File extractAudioFromVideo(String videoUrl, String targetAudioPath) throws IOException, InterruptedException {
        log.info("Bắt đầu trích xuất âm thanh từ Video URL: {} thông qua Docker FFmpeg Service", videoUrl);

        // Tạo thư mục tạm nếu chưa tồn tại
        File targetFile = new File(targetAudioPath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            byte[] audioBytes = webClient.post()
                    .uri("/extract-audio")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("videoUrl", videoUrl))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (audioBytes == null || audioBytes.length == 0) {
                throw new IOException("Dữ liệu âm thanh trả về từ FFmpeg service rỗng.");
            }

            Files.write(Paths.get(targetAudioPath), audioBytes);
            log.info("Trích xuất và lưu âm thanh thành công tại: {}", targetAudioPath);
            return targetFile;
        } catch (Exception e) {
            log.error("Tách âm thanh bằng dịch vụ FFmpeg Docker thất bại: {}", e.getMessage());
            throw new IOException("Lệnh trích xuất âm thanh từ FFmpeg service thất bại.", e);
        }
    }
}
