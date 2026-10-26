package com.dmx.elibrary.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.dmx.elibrary.domain.Book;
import com.dmx.elibrary.domain.Loan;
import com.dmx.elibrary.domain.LoanStatus;
import com.dmx.elibrary.domain.Patron;
import com.dmx.elibrary.dto.LoanResponse;
import com.dmx.elibrary.exception.BusinessException;
import com.dmx.elibrary.exception.ErrorCode;
import com.dmx.elibrary.repository.BookRepository;
import com.dmx.elibrary.repository.LoanRepository;
import com.dmx.elibrary.repository.PatronRepository;
import com.dmx.elibrary.support.MutableClock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-26T10:00:00Z");

    @Mock
    LoanRepository loanRepository;

    @Mock
    BookRepository bookRepository;

    @Mock
    PatronRepository patronRepository;

    private final MutableClock clock = new MutableClock(NOW);

    private LoanServiceImpl loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanServiceImpl(loanRepository, bookRepository, patronRepository, clock, 14);
    }

    @Test
    void borrowCreatesActiveLoanWithDueDateIn14Days() {
        given(patronRepository.findById(1L)).willReturn(Optional.of(patron()));
        Book book = book(2);
        given(bookRepository.findById(10L)).willReturn(Optional.of(book));
        given(loanRepository.findActiveLoanByPatronIdAndBookId(1L, 10L)).willReturn(Optional.empty());
        given(loanRepository.save(any(Loan.class))).willAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.borrow(1L, 10L);

        assertThat(response.status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(response.dueAt()).isEqualTo(NOW.plus(14, ChronoUnit.DAYS));
        assertThat(response.borrowedAt()).isEqualTo(NOW);
        assertThat(response.returnedAt()).isNull();
        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void borrowFailsWhenNoCopiesAvailable() {
        given(patronRepository.findById(1L)).willReturn(Optional.of(patron()));
        given(bookRepository.findById(10L)).willReturn(Optional.of(book(0)));

        assertThatThrownBy(() -> loanService.borrow(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NO_COPIES_AVAILABLE);
        then(loanRepository).should(never()).save(any(Loan.class));
    }

    @Test
    void borrowFailsOnDuplicateActiveLoan() {
        given(patronRepository.findById(1L)).willReturn(Optional.of(patron()));
        given(bookRepository.findById(10L)).willReturn(Optional.of(book(2)));
        given(loanRepository.findActiveLoanByPatronIdAndBookId(1L, 10L))
                .willReturn(Optional.of(Loan.create(book(2), patron(), NOW, java.time.Duration.ofDays(14))));

        assertThatThrownBy(() -> loanService.borrow(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOK_ALREADY_BORROWED);
        then(loanRepository).should(never()).save(any(Loan.class));
    }

    @Test
    void borrowFailsWhenBookMissing() {
        given(patronRepository.findById(1L)).willReturn(Optional.of(patron()));
        given(bookRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrow(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    void borrowFailsWhenPatronMissing() {
        given(patronRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrow(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PATRON_NOT_FOUND);
        then(bookRepository).shouldHaveNoInteractions();
    }

    private Book book(int availableCopies) {
        return Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-test")
                .publisher("Prentice Hall")
                .publishedDate(LocalDate.of(2008, 8, 11))
                .category("Software Engineering")
                .description("A handbook.")
                .totalCopies(availableCopies)
                .availableCopies(availableCopies)
                .build();
    }

    private Patron patron() {
        return Patron.builder()
                .name("Alice Chen")
                .email("alice@example.com")
                .build();
    }
}
