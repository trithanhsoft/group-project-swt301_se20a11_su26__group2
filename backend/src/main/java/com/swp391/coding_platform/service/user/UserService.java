package com.swp391.coding_platform.service.user;

import com.swp391.coding_platform.dto.request.ChangePasswordRequest;
import com.swp391.coding_platform.dto.request.LockUserRequest;
import com.swp391.coding_platform.dto.response.AdminUserResponse;
import com.swp391.coding_platform.dto.response.UserResponse;
import com.swp391.coding_platform.entity.enums.OrderStatus;
import com.swp391.coding_platform.entity.enums.StatusTransaction;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.entity.enums.UserStatus;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.UserMapper;
import com.swp391.coding_platform.repository.payment.OrderItemRepository;
import com.swp391.coding_platform.repository.payment.WalletTransactionRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    WalletTransactionRepository walletTransactionRepository;
    OrderItemRepository orderItemRepository;

    @Transactional
    public void changePassword(String username, ChangePasswordRequest changePasswordRequest){
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userEntity.validateStatus();

        if(!passwordEncoder.matches(changePasswordRequest.getOldPassword(), userEntity.getPasswordHash())){
            throw new AppException(ErrorCode.OLD_PASSWORD_NOT_MATCH);
        }

        if(!Objects.equals(changePasswordRequest.getNewPassword(), changePasswordRequest.getConfirmNewPassword())){
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), userEntity.getPasswordHash())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_SAME_AS_OLD_PASSWORD);
        }

        userEntity.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(userEntity);
    }

    public UserResponse getMyInfo(String username){
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(userEntity);
    }

    public List<AdminUserResponse> getAllUsersForAdmin() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminUserResponse setUserLockStatus(Integer userId, LockUserRequest request) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        try {
            UserStatus newStatus = UserStatus.valueOf(request.getStatus().toUpperCase());
            userEntity.setStatus(newStatus);
            if (newStatus == UserStatus.LOCKED) {
                userEntity.setLockReason(request.getReason() != null && !request.getReason().isBlank()
                        ? request.getReason() : "Violation of platform terms of service.");
                userEntity.setLockAppeal(null);
            } else {
                userEntity.setLockReason(null);
                userEntity.setLockAppeal(null);
            }
            userRepository.save(userEntity);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return mapToAdminUserResponse(userEntity);
    }

    @Transactional
    public void submitAppeal(String username, com.swp391.coding_platform.dto.request.AppealRequest request) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (userEntity.getStatus() != UserStatus.LOCKED) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        userEntity.setLockAppeal(request.getAppealReason());
        userRepository.save(userEntity);
    }

    private AdminUserResponse mapToAdminUserResponse(UserEntity user) {
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal totalDeposited = BigDecimal.ZERO;
        BigDecimal totalPurchased = BigDecimal.ZERO;

        if (user.getWallet() != null) {
            balance = user.getWallet().getBalance();
            totalDeposited = walletTransactionRepository.sumAmountByWalletIdAndTypeAndStatus(
                    user.getWallet().getId(),
                    TransactionType.DEPOSIT,
                    StatusTransaction.SUCCESS
            );
            totalPurchased = walletTransactionRepository.sumAmountByWalletIdAndTypeAndStatus(
                    user.getWallet().getId(),
                    TransactionType.BUY_COURSE,
                    StatusTransaction.SUCCESS
            );
        }

        List<AdminUserResponse.PurchasedCourseDto> purchasedCourses = orderItemRepository
                .findByOrderUserIdAndOrderStatus(user.getId(), OrderStatus.COMPLETED)
                .stream()
                .map(oi -> AdminUserResponse.PurchasedCourseDto.builder()
                        .id(oi.getCourse().getId().toString())
                        .title(oi.getCourse().getTitle())
                        .price(oi.getPrice())
                        .date(oi.getOrder().getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());

        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getDisplayname())
                .email(user.getEmail())
                .registerDate(user.getCreatedAt().toString())
                .status(user.getStatus().name())
                .balance(balance)
                .totalDeposited(totalDeposited)
                .totalPurchased(totalPurchased)
                .purchasedCourses(purchasedCourses)
                .isOnline(false) // Default to offline/false
                .lockReason(user.getLockReason())
                .lockAppeal(user.getLockAppeal())
                .build();
    }
}
