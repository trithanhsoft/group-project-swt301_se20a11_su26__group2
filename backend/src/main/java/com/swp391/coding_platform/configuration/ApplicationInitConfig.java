package com.swp391.coding_platform.configuration;

import com.swp391.coding_platform.entity.auth.RoleEntity;
import com.swp391.coding_platform.entity.enums.RoleName;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.repository.auth.RoleRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

@Slf4j
@Configuration
@Profile("dev") // Đã bỏ @RequiredArgsConstructor và @FieldDefaults
public class ApplicationInitConfig {

    // Đã bỏ khai báo biến PasswordEncoder ở đây

    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, // <-- Inject trực tiếp thẳng vào parameter của Bean
            JdbcTemplate jdbcTemplate
    ) {
        log.info("Init application for dev environment...");

        return args -> {
            // Đảm bảo extension vector, bảng course_embeddings và kiểu vector(768) đã được thiết lập đúng đắn
            try {
                jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
                jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS public.course_embeddings (" +
                    "    course_id BIGINT PRIMARY KEY," +
                    "    embedding vector(768) NOT NULL," +
                    "    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP," +
                    "    CONSTRAINT fk_embedding_course FOREIGN KEY (course_id) REFERENCES public.courses(id) ON DELETE CASCADE" +
                    ")"
                );
                try {
                    jdbcTemplate.execute(
                        "ALTER TABLE public.course_embeddings ALTER COLUMN embedding TYPE vector(768)"
                    );
                } catch (Exception alterEx) {
                    log.info("Phát hiện chiều vector cũ không tương thích, tiến hành truncate dữ liệu và đưa về vector(768)...");
                    jdbcTemplate.execute("TRUNCATE TABLE public.course_embeddings");
                    jdbcTemplate.execute(
                        "ALTER TABLE public.course_embeddings ALTER COLUMN embedding TYPE vector(768)"
                    );
                }
                log.info("Auto-initialized or verified course_embeddings table with vector(768) successfully.");
            } catch (Exception e) {
                log.warn("Không thể hoàn tất khởi tạo hoặc đồng bộ bảng course_embeddings: {}", e.getMessage());
            }

            // Setup role ADMIN first
            RoleEntity adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(RoleEntity.builder().name(RoleName.ADMIN).build()));

            // 1. Process "admin" user
            var adminUserOpt = userRepository.findByUsername("admin");
            boolean emailExists = userRepository.findByEmail("admin@gmail.com").isPresent();

            if (adminUserOpt.isEmpty() && !emailExists) {
                UserEntity adminUser = UserEntity.builder()
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .displayname("admin")
                        .email("admin@gmail.com")
                        .status(UserStatus.ACTIVE)
                        .roles(java.util.Collections.singleton(adminRole))
                        .build();

                userRepository.save(adminUser);
                log.warn("Dev admin user has been created. Please change the default password if needed.");
            } else {
                log.info("Admin user already exists. Forcing reset password to 'admin' and status to ACTIVE for dev profile...");
                adminUserOpt.ifPresent(adminUser -> {
                    adminUser.setPasswordHash(passwordEncoder.encode("admin"));
                    adminUser.setStatus(UserStatus.ACTIVE);
                    userRepository.save(adminUser);
                    log.warn("Dev admin user password has been reset to 'admin' and status set to ACTIVE.");
                });
            }

            // 2. Process "admin1" user
            var admin1UserOpt = userRepository.findByUsername("admin1");
            boolean admin1EmailExists = userRepository.findByEmail("admin1@gmail.com").isPresent();

            if (admin1UserOpt.isEmpty() && !admin1EmailExists) {
                UserEntity admin1User = UserEntity.builder()
                        .username("admin1")
                        .passwordHash(passwordEncoder.encode("admin"))
                        .displayname("admin1")
                        .email("admin1@gmail.com")
                        .status(UserStatus.ACTIVE)
                        .roles(java.util.Collections.singleton(adminRole))
                        .build();

                userRepository.save(admin1User);
                log.warn("Dev admin1 user has been created with password 'admin'.");
            } else {
                log.info("Admin1 user already exists. Forcing reset password to 'admin' and status to ACTIVE...");
                admin1UserOpt.ifPresent(admin1User -> {
                    admin1User.setPasswordHash(passwordEncoder.encode("admin"));
                    admin1User.setStatus(UserStatus.ACTIVE);
                    userRepository.save(admin1User);
                    log.warn("Dev admin1 user password has been reset to 'admin' and status set to ACTIVE.");
                });
            }
        };
    }
}