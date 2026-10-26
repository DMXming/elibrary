package com.dmx.elibrary.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionBecomesProblemDetailWithCode() {
        ProblemDetail problem = handler.handleBusiness(new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        assertThat(problem.getStatus()).isEqualTo(404);
        assertThat(problem.getProperties()).containsEntry("code", "BOOK_NOT_FOUND");
        assertThat(problem.getDetail()).isEqualTo(ErrorCode.BOOK_NOT_FOUND.getDefaultMessage());
    }

    @Test
    void optimisticLockExceptionBecomes409ConcurrentUpdate() {
        ProblemDetail problem = handler.handleOptimisticLock(
                new ObjectOptimisticLockingFailureException("test", new OptimisticLockException()));

        assertThat(problem.getStatus()).isEqualTo(409);
        assertThat(problem.getProperties()).containsEntry("code", "CONCURRENT_UPDATE");
    }
}
