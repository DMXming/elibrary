package com.dmx.elibrary.repository;

import com.dmx.elibrary.domain.Loan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("""
            SELECT l FROM Loan l
            WHERE l.patron.id = :patronId AND l.book.id = :bookId AND l.returnedAt IS NULL
            """)
    Optional<Loan> findActiveLoanByPatronIdAndBookId(@Param("patronId") Long patronId, @Param("bookId") Long bookId);

    @Query("""
            SELECT l FROM Loan l
            JOIN FETCH l.book
            WHERE l.patron.id = :patronId AND l.returnedAt IS NULL
            ORDER BY l.dueAt ASC
            """)
    List<Loan> findActiveLoansByPatronIdOrderByDueAtAsc(@Param("patronId") Long patronId);
}
