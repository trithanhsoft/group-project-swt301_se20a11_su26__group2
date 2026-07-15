package com.swp391.coding_platform.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class PromptSanitizerService {

    private static final int MAX_LENGTH = 3000;

    public String sanitizeAndTruncate(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }

        // 1. Strip HTML tags
        String sanitized = input.replaceAll("<[^>]*>", " ");

        // 2. Remove common prompt injection phrases (basic neutralization)
        sanitized = sanitized.replaceAll("(?i)(ignore previous instructions|system prompt|you are a)", " ");

        // 3. Truncate if exceeds MAX_LENGTH
        if (sanitized.length() > MAX_LENGTH) {
            log.warn("Input description exceeds {} characters. Truncating.", MAX_LENGTH);
            sanitized = sanitized.substring(0, MAX_LENGTH) + "... (truncated)";
        }

        return sanitized.trim();
    }
}
