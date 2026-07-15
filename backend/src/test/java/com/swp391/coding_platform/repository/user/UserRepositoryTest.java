package com.swp391.coding_platform.repository.user;

import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.swp391.coding_platform.TestcontainersConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_Success() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setDisplayname("Test User");
        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void existsByUsername_True() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setDisplayname("Test User");
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("testuser");

        assertTrue(exists);
    }

    @Test
    void existsByUsername_False() {
        boolean exists = userRepository.existsByUsername("nonexistent");
        assertFalse(exists);
    }

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    @Test
    void incrementUserScore_Success() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser_score");
        user.setEmail("testscore@test.com");
        user.setDisplayname("Test Score User");
        user.setScore(10);
        UserEntity savedUser = userRepository.save(user);
        
        entityManager.flush();

        userRepository.incrementUserScore(savedUser.getId(), 5);

        // We need to flush and clear to see the updated value in db since it's a modifying query
        entityManager.clear();
        
        // Find again
        UserEntity updatedUser = userRepository.findById(savedUser.getId()).get();
        assertEquals(15, updatedUser.getScore());
    }
}

