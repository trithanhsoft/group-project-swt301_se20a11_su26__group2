package com.swp391.coding_platform.repository.payment;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.entity.enums.OrderStatus;
import com.swp391.coding_platform.entity.enums.UserStatus;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity user;
    private CourseEntity course;
    private OrderEntity order;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setUsername("orderuser");
        user.setEmail("orderuser@example.com");
        user.setPasswordHash("hash");
        user.setDisplayname("Order User");
        user.setAvatarurl("url");
        user.setScore(0);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user = entityManager.persist(user);

        UserEntity instUser = new UserEntity();
        instUser.setUsername("instructor5");
        instUser.setEmail("inst5@example.com");
        instUser.setPasswordHash("hash");
        instUser.setDisplayname("Instructor");
        instUser.setAvatarurl("url");
        instUser.setScore(0);
        instUser.setStatus(UserStatus.ACTIVE);
        instUser.setCreatedAt(Instant.now());
        instUser = entityManager.persist(instUser);

        InstructorEntity instructor = new InstructorEntity();
        instructor.setUser(instUser);
        instructor.setFullName("Instructor Name");
        instructor.setMajor("CS");
        instructor = entityManager.persist(instructor);

        course = new CourseEntity();
        course.setInstructor(instructor);
        course.setTitle("Java Basics");
        course.setShortDescription("Short desc");
        course.setLongDescription("Long desc");
        course.setType("PAID");
        course.setPrice(BigDecimal.valueOf(100.0));
        course.setStatus(CourseStatus.APPROVED);
        course = entityManager.persist(course);

        order = new OrderEntity();
        order.setUser(user);
        order.setTotalAmount(BigDecimal.valueOf(100.0));
        order.setStatus(OrderStatus.COMPLETED);
        order.setCreatedAt(Instant.now());
        order = entityManager.persist(order);

        OrderItemEntity item = new OrderItemEntity();
        item.setOrder(order);
        item.setCourse(course);
        item.setPrice(BigDecimal.valueOf(100.0));
        entityManager.persist(item);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testFindAllByStatusAndCreatedAtAfter() {
        List<OrderEntity> orders = orderRepository.findAllByStatusAndCreatedAtAfter(
                OrderStatus.COMPLETED, Instant.now().minus(1, ChronoUnit.DAYS));
        assertFalse(orders.isEmpty());
        assertEquals(order.getId(), orders.get(0).getId());
    }

    @Test
    void testFindByUserIdAndStatusOrderByCreatedAtDesc() {
        Page<OrderEntity> result = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                user.getId(), OrderStatus.COMPLETED, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals(order.getId(), result.getContent().get(0).getId());
    }

    @Test
    void testFindAllByStatus() {
        List<OrderEntity> orders = orderRepository.findAllByStatus(OrderStatus.COMPLETED);
        assertFalse(orders.isEmpty());
        assertEquals(order.getId(), orders.get(0).getId());
    }

    @Test
    void testFindByStatusAndCreatedAtBetweenOrderByCreatedAtDesc() {
        Instant start = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant end = Instant.now().plus(1, ChronoUnit.DAYS);
        Page<OrderEntity> result = orderRepository.findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                OrderStatus.COMPLETED, start, end, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindAllByStatusWithDetails() {
        List<OrderEntity> orders = orderRepository.findAllByStatusWithDetails(OrderStatus.COMPLETED);
        assertFalse(orders.isEmpty());
        OrderEntity fetchedOrder = orders.get(0);
        assertEquals(user.getId(), fetchedOrder.getUser().getId());
        assertFalse(fetchedOrder.getOrderItems().isEmpty());
        assertEquals(course.getId(), fetchedOrder.getOrderItems().get(0).getCourse().getId());
    }

    @Test
    void testFindByStatusOrderByCreatedAtDesc() {
        Page<OrderEntity> result = orderRepository.findByStatusOrderByCreatedAtDesc(
                OrderStatus.COMPLETED, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }
}
