package com.swp391.coding_platform.entity.course;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseModerationReportEntityTest {

    @Test
    void onUpdate() throws InterruptedException {
        CourseModerationReportEntity entity = new CourseModerationReportEntity();
        Instant oldTime = entity.getUpdatedAt();

        Thread.sleep(10); // Wait slightly to ensure a new Instant is generated

        entity.onUpdate();
        Instant newTime = entity.getUpdatedAt();

        assertTrue(newTime.isAfter(oldTime));
    }
}
