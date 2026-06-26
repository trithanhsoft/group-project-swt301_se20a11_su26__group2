package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.entity.payment.OrderEntity;
import com.swp391.coding_platform.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Integer> {
    List<OrderEntity> findAllByStatusAndCreatedAtAfter(OrderStatus status, Instant after);
    Page<OrderEntity> findByUserIdAndStatusOrderByCreatedAtDesc(Integer userId, OrderStatus status, Pageable pageable);

    List<OrderEntity> findAllByStatus(OrderStatus status);

    @Query("SELECT DISTINCT o FROM OrderEntity o " +
           "LEFT JOIN FETCH o.user u " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.course c " +
           "WHERE o.status = :status")
    List<OrderEntity> findAllByStatusWithDetails(@Param("status") OrderStatus status);
}
