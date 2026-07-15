package com.swp391.coding_platform.repository.auth;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.auth.RoleEntity;
import com.swp391.coding_platform.entity.enums.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testFindByName_whenRoleExists_shouldReturnRole() {
        // Arrange
        RoleEntity role = RoleEntity.builder()
                .name(RoleName.USER)
                .build();
        entityManager.persist(role);
        entityManager.flush();

        // Act
        Optional<RoleEntity> result = roleRepository.findByName(RoleName.USER);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(RoleName.USER);
    }

    @Test
    void testFindByName_whenRoleDoesNotExist_shouldReturnEmpty() {
        // Act
        Optional<RoleEntity> result = roleRepository.findByName(RoleName.INSTRUCTOR);

        // Assert
        assertThat(result).isNotPresent();
    }
}
