package com.dmx.elibrary.service.impl;

import com.dmx.elibrary.domain.Book;
import com.dmx.elibrary.domain.Loan;
import com.dmx.elibrary.domain.Patron;
import com.dmx.elibrary.dto.LoanResponse;
import com.dmx.elibrary.exception.BusinessException;
import com.dmx.elibrary.exception.ErrorCode;
import com.dmx.elibrary.repository.BookRepository;
import com.dmx.elibrary.repository.LoanRepository;
import com.dmx.elibrary.repository.PatronRepository;
import com.dmx.elibrary.service.LoanService;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;
    private final Clock clock;
    private final Duration loanPeriod;

    public LoanServiceImpl(LoanRepository loanRepository, BookRepository bookRepository,
            PatronRepository patronRepository, Clock clock,
            @Value("${elibrary.loan-period-days}") long loanPeriodDays) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.patronRepository = patronRepository;
        this.clock = clock;
        this.loanPeriod = Duration.ofDays(loanPeriodDays);
    }

    @Override
    @Transactional
    public LoanResponse borrow(Long patronId, Long bookId) {
        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATRON_NOT_FOUND));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException(ErrorCode.NO_COPIES_AVAILABLE);
        }
        if (loanRepository.findActiveLoanByPatronIdAndBookId(patronId, bookId).isPresent()) {
            throw new BusinessException(ErrorCode.BOOK_ALREADY_BORROWED);
        }
        // The availability check above is only a fast path: two concurrent borrows
        // of the last copy both pass it, but the loser fails on Book's @Version
        // at commit and the client gets 409 CONCURRENT_UPDATE (see GlobalExceptionHandler).
        Loan loan = loanRepository.save(Loan.create(book, patron, clock.instant(), loanPeriod));
        return LoanResponse.from(loan, clock.instant());
    }

    @Override
    @Transactional
    public LoanResponse returnLoan(Long patronId, Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_NOT_FOUND));
        // Another patron's loan is indistinguishable from a missing one: looking
        // up the patron would leak whether the loan exists at all.
        if (!patronId.equals(loan.getPatron().getId())) {
            throw new BusinessException(ErrorCode.LOAN_NOT_FOUND);
        }
        if (loan.getReturnedAt() != null) {
            throw new BusinessException(ErrorCode.LOAN_ALREADY_RETURNED);
        }
        loan.returnNow(clock.instant());
        return LoanResponse.from(loan, clock.instant());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> listCurrentLoans(Long patronId) {
        if (patronRepository.findById(patronId).isEmpty()) {
            throw new BusinessException(ErrorCode.PATRON_NOT_FOUND);
        }
        return loanRepository.findActiveLoansByPatronIdOrderByDueAtAsc(patronId).stream()
                .map(loan -> LoanResponse.from(loan, clock.instant()))
                .toList();
    }
}
