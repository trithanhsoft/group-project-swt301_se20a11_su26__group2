package com.swp391.coding_platform.repository.problem;

import com.swp391.coding_platform.TestcontainersConfiguration;
import com.swp391.coding_platform.entity.problem.ProblemCommentEntity;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class ProblemCommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProblemCommentRepository repository;

    private ProblemEntity problem;
    private UserEntity user;
    private ProblemCommentEntity parentComment;
    private ProblemCommentEntity childComment;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("testuser_cmt")
                .email("test_cmt@example.com")
                .displayname("Test User")
                .build();
        entityManager.persist(user);

        problem = ProblemEntity.builder()
                .createdBy(user)
                .build();
        entityManager.persist(problem);

        parentComment = ProblemCommentEntity.builder()
                .problem(problem)
                .user(user)
                .content("Parent comment")
                .build();
        entityManager.persist(parentComment);

        childComment = ProblemCommentEntity.builder()
                .problem(problem)
                .user(user)
                .content("Child comment")
                .parent(parentComment)
                .build();
        entityManager.persist(childComment);
        entityManager.flush();
    }

    @Test
    void testFindByProblemIdAndParentIsNullOrderByCreatedAtDesc() {
        List<ProblemCommentEntity> comments = repository.findByProblemIdAndParentIsNullOrderByCreatedAtDesc(problem.getId());

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getId()).isEqualTo(parentComment.getId());
    }

    @Test
    void testDeleteByProblemId() {
        repository.deleteByProblemId(problem.getId());
        entityManager.flush();

        List<ProblemCommentEntity> remainingComments = repository.findAll();
        assertThat(remainingComments).isEmpty();
    }
}
