package com.swp391.coding_platform.service.instructor;

import com.swp391.coding_platform.dto.request.ApproveApplicationRequest;
import com.swp391.coding_platform.dto.request.InstructorApplyRequest;
import com.swp391.coding_platform.dto.response.InstructorApplicationResponse;
import com.swp391.coding_platform.entity.auth.RoleEntity;
import com.swp391.coding_platform.entity.enums.InstructorAppStatus;
import com.swp391.coding_platform.entity.enums.InstructorStatus;
import com.swp391.coding_platform.entity.enums.RoleName;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.instructor.InstructorApplicationEntity;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.auth.RoleRepository;
import com.swp391.coding_platform.repository.instructor.InstructorApplicationRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.payment.WalletRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InstructorApplicationService {

    InstructorApplicationRepository applicationRepository;
    UserRepository userRepository;
    RoleRepository roleRepository;
    InstructorRepository instructorRepository;
    WalletRepository walletRepository;

    @Transactional
    public InstructorApplicationResponse apply(Integer userId, InstructorApplyRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check if user is already an instructor
        boolean isAlreadyInstructor = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.INSTRUCTOR);
        if (isAlreadyInstructor || instructorRepository.findByUserId(userId).isPresent()) {
            throw new AppException(ErrorCode.ALREADY_INSTRUCTOR);
        }

        // We automatically approve and create the Instructor Profile!
        // 1. Add INSTRUCTOR role to user
        RoleEntity instructorRole = roleRepository.findByName(RoleName.INSTRUCTOR)
                .orElseGet(() -> roleRepository.save(RoleEntity.builder().name(RoleName.INSTRUCTOR).build()));

        Set<RoleEntity> roles = new HashSet<>(user.getRoles());
        roles.add(instructorRole);
        user.setRoles(roles);
        userRepository.save(user);

        // 2. Create Instructor Profile
        InstructorEntity instructor = instructorRepository.findByUserId(user.getId())
                .orElseGet(() -> instructorRepository.save(InstructorEntity.builder()
                        .user(user)
                        .fullName(request.getFullName())
                        .major(request.getMajor())
                        .bio(request.getBio())
                        .status(InstructorStatus.ACTIVE)
                        .hiredByAdmin(false)
                        .build()));

        // 3. Create Wallet if not exists
        if (user.getWallet() == null) {
            WalletEntity wallet = WalletEntity.builder()
                    .user(user)
                    .balance(BigDecimal.ZERO)
                    .status(UserStatus.ACTIVE)
                    .build();
            walletRepository.save(wallet);
        }

        // 4. Also save an application record with status APPROVED as a registration history log
        InstructorApplicationEntity entity = InstructorApplicationEntity.builder()
                .user(user)
                .cvUrl("self_registered")
                .introduction(request.getBio())
                .status(InstructorAppStatus.APPROVED)
                .adminNote("Auto-approved upon self-registration.")
                .aiScore(null)
                .aiSpecialization(request.getMajor())
                .build();

        entity = applicationRepository.save(entity);

        return mapToResponse(entity);
    }


    @Transactional(readOnly = true)
    public List<InstructorApplicationResponse> getApplications() {
        return applicationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InstructorApplicationResponse getMyApplicationStatus(Integer userId) {
        List<InstructorApplicationEntity> list = applicationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (list.isEmpty()) {
            return null;
        }
        return mapToResponse(list.get(0));
    }

    @Transactional
    public InstructorApplicationResponse approveApplication(Integer appId, ApproveApplicationRequest request) {
        InstructorApplicationEntity application = applicationRepository.findById(appId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (application.getStatus() != InstructorAppStatus.PENDING) {
            throw new RuntimeException("Đơn đăng ký này đã được xử lý trước đó.");
        }

        InstructorAppStatus targetStatus;
        try {
            targetStatus = InstructorAppStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái duyệt không hợp lệ. Chỉ chấp nhận APPROVED hoặc REJECTED.");
        }

        application.setStatus(targetStatus);
        application.setAdminNote(request.getAdminNote());
        application = applicationRepository.save(application);

        if (targetStatus == InstructorAppStatus.APPROVED) {
            UserEntity user = application.getUser();

            // Add INSTRUCTOR role to user
            RoleEntity instructorRole = roleRepository.findByName(RoleName.INSTRUCTOR)
                    .orElseGet(() -> roleRepository.save(RoleEntity.builder().name(RoleName.INSTRUCTOR).build()));

            Set<RoleEntity> roles = new HashSet<>(user.getRoles());
            roles.add(instructorRole);
            user.setRoles(roles);
            userRepository.save(user);

            // Create Instructor Profile
            if (instructorRepository.findByUserId(user.getId()).isEmpty()) {
                InstructorEntity instructor = InstructorEntity.builder()
                        .user(user)
                        .fullName(user.getDisplayname() != null ? user.getDisplayname() : user.getUsername())
                        .major("Software Engineering")
                        .bio(application.getIntroduction())
                        .status(InstructorStatus.ACTIVE)
                        .hiredByAdmin(true)
                        .build();
                instructorRepository.save(instructor);
            }

            // Create Wallet if not exists
            if (user.getWallet() == null) {
                WalletEntity wallet = WalletEntity.builder()
                        .user(user)
                        .balance(BigDecimal.ZERO)
                        .status(UserStatus.ACTIVE)
                        .build();
                walletRepository.save(wallet);
            }
            log.info("User {} has been successfully approved to be an INSTRUCTOR.", user.getUsername());
        }

        return mapToResponse(application);
    }

    private InstructorApplicationResponse mapToResponse(InstructorApplicationEntity entity) {
        return InstructorApplicationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .fullName(entity.getUser().getDisplayname() != null ? entity.getUser().getDisplayname() : entity.getUser().getUsername())
                .email(entity.getUser().getEmail())
                .cvUrl(entity.getCvUrl())
                .introduction(entity.getIntroduction())
                .status(entity.getStatus().name())
                .adminNote(entity.getAdminNote())
                .aiScore(entity.getAiScore())
                .aiSummary(entity.getAiSummary())
                .aiSpecialization(entity.getAiSpecialization())
                .aiTechnologies(entity.getAiTechnologies())
                .aiExperienceYears(entity.getAiExperienceYears())
                .aiStrengths(entity.getAiStrengths())
                .aiWeaknesses(entity.getAiWeaknesses())
                .aiRecommendation(entity.getAiRecommendation())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    @Transactional
    public void cleanupRejectedApplications() {
        log.info("Starting automatic cleanup of rejected instructor applications older than 7 days...");
        Instant cutoff = Instant.now().minus(java.time.Duration.ofDays(7));
        
        List<InstructorApplicationEntity> oldRejectedApps = applicationRepository
                .findByStatusAndUpdatedAtBefore(InstructorAppStatus.REJECTED, cutoff);
                
        if (oldRejectedApps.isEmpty()) {
            log.info("No rejected instructor applications older than 7 days found.");
            return;
        }

        // Resolve target base directory to identify local CV files
        java.io.File rootDir = new java.io.File(".").getAbsoluteFile();
        java.io.File backendDir = new java.io.File(rootDir, "backend");
        java.io.File baseDir = backendDir.exists() && backendDir.isDirectory() ? backendDir : rootDir;

        for (InstructorApplicationEntity app : oldRejectedApps) {
            try {
                String cvUrl = app.getCvUrl();
                if (cvUrl != null && cvUrl.contains("/uploads/cvs/")) {
                    String filename = cvUrl.substring(cvUrl.lastIndexOf("/") + 1);
                    java.io.File localFile = new java.io.File(baseDir, "uploads/cvs/" + filename);
                    if (localFile.exists()) {
                        boolean deleted = localFile.delete();
                        if (deleted) {
                            log.info("Deleted CV file from disk: {}", localFile.getAbsolutePath());
                        } else {
                            log.warn("Failed to delete CV file from disk: {}", localFile.getAbsolutePath());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error deleting CV file for application ID {}: {}", app.getId(), e.getMessage());
            }
        }

        applicationRepository.deleteAll(oldRejectedApps);
        log.info("Successfully cleaned up {} rejected instructor applications.", oldRejectedApps.size());
    }
}
