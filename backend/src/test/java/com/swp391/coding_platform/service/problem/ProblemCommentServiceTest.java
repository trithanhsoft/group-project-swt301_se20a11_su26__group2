package com.swp391.coding_platform.service.problem;

import com.swp391.coding_platform.dto.request.CreateCommentRequest;
import com.swp391.coding_platform.dto.response.ProblemCommentResponse;
import com.swp391.coding_platform.entity.problem.ProblemCommentEntity;
import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.repository.problem.ProblemCommentRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemCommentServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ProblemCommentRepository problemCommentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProblemCommentService problemCommentService;

    @Test
    void getComments_ProblemNotFound_ThrowsAppException() {
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> problemCommentService.getComments(1));
        assertEquals(ErrorCode.OJ_PROBLEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getComments_Success() {
        ProblemEntity problem = mock(ProblemEntity.class);
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(problem));

        ProblemCommentEntity comment = mock(ProblemCommentEntity.class);
        UserEntity user = mock(UserEntity.class);
        when(user.getUsername()).thenReturn("user1");
        when(comment.getUser()).thenReturn(user);
        when(comment.getContent()).thenReturn("Test comment");
        when(comment.getCreatedAt()).thenReturn(Instant.now());

        when(problemCommentRepository.findByProblemIdAndParentIsNullOrderByCreatedAtDesc(1)).thenReturn(List.of(comment));

        List<ProblemCommentResponse> result = problemCommentService.getComments(1);

        assertFalse(result.isEmpty());
        assertEquals("user1", result.get(0).getAuthor());
    }

    @Test
    void addComment_UserNull_ThrowsAppException() {
        CreateCommentRequest request = new CreateCommentRequest();
        
        AppException exception = assertThrows(AppException.class, () -> problemCommentService.addComment(1, null, request));
        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
    }

    @Test
    void addComment_Success() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("New comment");

        ProblemEntity problem = mock(ProblemEntity.class);
        when(problemRepository.findByIdAndIsActiveTrueAndIsPublicTrue(1)).thenReturn(Optional.of(problem));

        UserEntity user = mock(UserEntity.class);
        when(user.getUsername()).thenReturn("testUser");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ProblemCommentResponse response = problemCommentService.addComment(1, 1, request);

        assertNotNull(response);
        verify(problemCommentRepository, times(1)).save(any(ProblemCommentEntity.class));
    }
}
