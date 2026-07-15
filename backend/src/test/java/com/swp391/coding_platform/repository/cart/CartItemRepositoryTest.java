package com.swp391.coding_platform.repository.cart;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.cart.CartEntity;
import com.swp391.coding_platform.entity.cart.CartItemEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
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
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CartEntity cart;
    private CourseEntity course;
    private CartItemEntity cartItem;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .username("cartuser")
                .displayname("Cart User")
                .email("cartuser@example.com")
                .build();
        entityManager.persist(user);

        UserEntity instructorUser = UserEntity.builder()
                .username("instuser")
                .displayname("Instructor")
                .email("inst@example.com")
                .build();
        entityManager.persist(instructorUser);

        InstructorEntity instructor = InstructorEntity.builder()
                .user(instructorUser)
                .fullName("Inst Name")
                .major("IT")
                .build();
        entityManager.persist(instructor);

        cart = new CartEntity(user);
        entityManager.persist(cart);

        course = CourseEntity.builder()
                .instructor(instructor)
                .title("Course 1")
                .shortDescription("Short")
                .longDescription("Long")
                .type("FREE")
                .build();
        entityManager.persist(course);

        cartItem = new CartItemEntity(cart, course);
        entityManager.persist(cartItem);
        entityManager.flush();
    }

    @Test
    void findByCartIdAndCourseId_ShouldReturnCartItem() {
        Optional<CartItemEntity> found = cartItemRepository.findByCartIdAndCourseId(cart.getId(), course.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(cartItem.getId());
    }

    @Test
    void deleteByCartIdAndCourseId_ShouldDeleteCartItem() {
        cartItemRepository.deleteByCartIdAndCourseId(cart.getId(), course.getId());
        entityManager.flush();
        entityManager.clear();

        CartItemEntity found = entityManager.find(CartItemEntity.class, cartItem.getId());
        assertThat(found).isNull();
    }

    @Test
    void deleteByCartId_ShouldDeleteAllCartItemsInCart() {
        cartItemRepository.deleteByCartId(cart.getId());
        entityManager.flush();
        entityManager.clear();

        CartItemEntity found = entityManager.find(CartItemEntity.class, cartItem.getId());
        assertThat(found).isNull();
    }
}
