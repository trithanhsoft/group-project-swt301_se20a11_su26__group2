package com.swp391.coding_platform.entity.problem;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ProblemEntityTest {

    @Test
    void getCurrentVersion_NullVersions_ReturnsNull() {
        ProblemEntity entity = new ProblemEntity();
        entity.setVersions(null);
        assertNull(entity.getCurrentVersion());
    }

    @Test
    void getCurrentVersion_NoActiveVersion_ReturnsNull() {
        ProblemEntity entity = new ProblemEntity();
        ProblemVersionEntity v1 = new ProblemVersionEntity();
        v1.setIsActive(false);
        entity.setVersions(new ArrayList<>(java.util.List.of(v1)));

        assertNull(entity.getCurrentVersion());
    }

    @Test
    void getCurrentVersion_HasActiveVersion_ReturnsActive() {
        ProblemEntity entity = new ProblemEntity();
        ProblemVersionEntity v1 = new ProblemVersionEntity();
        v1.setIsActive(false);
        ProblemVersionEntity v2 = new ProblemVersionEntity();
        v2.setIsActive(true);

        entity.setVersions(new ArrayList<>(java.util.List.of(v1, v2)));

        assertEquals(v2, entity.getCurrentVersion());
    }

    @Test
    void setCurrentVersion_NullVersionsInit_AddsAndActivates() {
        ProblemEntity entity = new ProblemEntity();
        entity.setVersions(null);

        ProblemVersionEntity v1 = new ProblemVersionEntity();
        v1.setIsActive(false);

        entity.setCurrentVersion(v1);

        assertNotNull(entity.getVersions());
        assertEquals(1, entity.getVersions().size());
        assertTrue(entity.getVersions().get(0).getIsActive());
        assertEquals(v1, entity.getCurrentVersion());
    }

    @Test
    void setCurrentVersion_ExistingVersions_DeactivatesOldAndActivatesNew() {
        ProblemEntity entity = new ProblemEntity();
        ProblemVersionEntity oldV = new ProblemVersionEntity();
        oldV.setIsActive(true);
        entity.getVersions().add(oldV);

        ProblemVersionEntity newV = new ProblemVersionEntity();
        newV.setIsActive(false);

        entity.setCurrentVersion(newV);

        assertFalse(oldV.getIsActive());
        assertTrue(newV.getIsActive());
        assertTrue(entity.getVersions().contains(oldV));
        assertTrue(entity.getVersions().contains(newV));
        assertEquals(2, entity.getVersions().size());
        assertEquals(newV, entity.getCurrentVersion());
    }

    @Test
    void setCurrentVersion_NullVersion_DeactivatesAll() {
        ProblemEntity entity = new ProblemEntity();
        ProblemVersionEntity oldV = new ProblemVersionEntity();
        oldV.setIsActive(true);
        entity.getVersions().add(oldV);

        entity.setCurrentVersion(null);

        assertFalse(oldV.getIsActive());
        assertNull(entity.getCurrentVersion());
    }
}
