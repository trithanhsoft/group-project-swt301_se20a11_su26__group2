package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.entity.enums.OrderStatus;
import com.swp391.coding_platform.entity.payment.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Integer> {
    @Query("SELECT oi FROM OrderItemEntity oi " +
           "JOIN FETCH oi.order o " +
           "JOIN FETCH oi.course c " +
           "JOIN FETCH o.user u " +
           "WHERE c.instructor.id = :instructorId " +
           "AND o.status = 'COMPLETED'")
    List<OrderItemEntity> findCompletedItemsByInstructorId(@Param("instructorId") Integer instructorId);

    @Query("SELECT oi FROM OrderItemEntity oi " +
           "JOIN FETCH oi.order o " +
           "LEFT JOIN FETCH oi.course c " +
           "LEFT JOIN FETCH c.instructor i " +
           "LEFT JOIN FETCH o.user u " +
           "WHERE o.status = 'COMPLETED'")
    List<OrderItemEntity> findAllCompletedOrderItemsWithDetails();

    @Query("SELECT oi FROM OrderItemEntity oi " +
           "JOIN FETCH oi.order o " +
           "JOIN FETCH oi.course c " +
           "WHERE o.user.id = :userId AND o.status = :status")
    List<OrderItemEntity> findByOrderUserIdAndOrderStatus(
            @Param("userId") Integer userId,
            @Param("status") OrderStatus status);
}
