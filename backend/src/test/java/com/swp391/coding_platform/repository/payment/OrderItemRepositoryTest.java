package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.OrderStatus;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.payment.OrderEntity;
import com.swp391.coding_platform.entity.payment.OrderItemEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private InstructorEntity instructor;
    private UserEntity student;
    private OrderEntity orderCompleted;
    private OrderEntity orderPending;
    private CourseEntity course;
    private OrderItemEntity completedItem;

    @BeforeEach
    void setUp() {
        student = UserEntity.builder()
                .username("student_oi")
                .displayname("Student")
                .email("student_oi@example.com")
                .build();
        entityManager.persist(student);

        UserEntity instructorUser = UserEntity.builder()
                .username("instructor_oi")
                .displayname("Instructor")
                .email("instructor_oi@example.com")
                .build();
        entityManager.persist(instructorUser);

        instructor = InstructorEntity.builder()
                .user(instructorUser)
                .fullName("Prof. Oak")
                .major("Science")
                .build();
        entityManager.persist(instructor);

        course = CourseEntity.builder()
                .instructor(instructor)
                .title("Java 101")
                .shortDescription("Desc")
                .longDescription("Long Desc")
                .type("PAID")
                .price(new BigDecimal("100.00"))
                .build();
        entityManager.persist(course);

        orderCompleted = OrderEntity.builder()
                .user(student)
                .status(OrderStatus.COMPLETED)
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        entityManager.persist(orderCompleted);

        orderPending = OrderEntity.builder()
                .user(student)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .build();
        entityManager.persist(orderPending);

        completedItem = OrderItemEntity.builder()
                .order(orderCompleted)
                .course(course)
                .price(new BigDecimal("100.00"))
                .build();
        entityManager.persist(completedItem);

        OrderItemEntity pendingItem = OrderItemEntity.builder()
                .order(orderPending)
                .course(course)
                .price(new BigDecimal("100.00"))
                .build();
        entityManager.persist(pendingItem);

        entityManager.flush();
    }

    @Test
    void findCompletedItemsByInstructorId_ShouldReturnItems() {
        List<OrderItemEntity> items = orderItemRepository.findCompletedItemsByInstructorId(instructor.getId());
        
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(completedItem.getId());
    }

    @Test
    void findAllCompletedOrderItemsWithDetails_ShouldReturnItems() {
        List<OrderItemEntity> items = orderItemRepository.findAllCompletedOrderItemsWithDetails();
        
        assertThat(items).isNotEmpty();
        assertThat(items).extracting("id").contains(completedItem.getId());
    }

    @Test
    void findByOrderStatusOrderByOrderCreatedAtDesc_ShouldReturnPagedItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItemEntity> page = orderItemRepository.findByOrderStatusOrderByOrderCreatedAtDesc(OrderStatus.COMPLETED, pageable);
        
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(completedItem.getId());
    }

    @Test
    void findByOrderStatusAndOrderCreatedAtBetweenOrderByOrderCreatedAtDesc_ShouldReturnItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Instant start = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant end = Instant.now();
        
        Page<OrderItemEntity> page = orderItemRepository.findByOrderStatusAndOrderCreatedAtBetweenOrderByOrderCreatedAtDesc(
                OrderStatus.COMPLETED, start, end, pageable);
        
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(completedItem.getId());
    }

    @Test
    void findByOrderUserIdAndOrderStatus_ShouldReturnItems() {
        List<OrderItemEntity> items = orderItemRepository.findByOrderUserIdAndOrderStatus(student.getId(), OrderStatus.COMPLETED);
        
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getId()).isEqualTo(completedItem.getId());
    }
}
