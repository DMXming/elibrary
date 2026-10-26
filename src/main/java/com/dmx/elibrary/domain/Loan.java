package com.dmx.elibrary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patron_id", nullable = false)
    private Patron patron;

    @Column(nullable = false)
    private Instant borrowedAt;

    @Column(nullable = false)
    private Instant dueAt;

    private Instant returnedAt;

    @Version
    private int version;

    private Loan(Book book, Patron patron, Instant borrowedAt, Instant dueAt) {
        this.book = book;
        this.patron = patron;
        this.borrowedAt = borrowedAt;
        this.dueAt = dueAt;
    }

    /**
     * Creates an active loan and takes one copy of the book.
     * Throws {@link IllegalStateException} when no copy is left.
     */
    public static Loan create(Book book, Patron patron, Instant now, Duration loanPeriod) {
        book.borrowCopy();
        return new Loan(book, patron, now, now.plus(loanPeriod));
    }

    /** Marks the loan returned and gives the copy back. */
    public void returnNow(Instant now) {
        if (returnedAt != null) {
            throw new IllegalStateException("Loan " + id + " is already returned");
        }
        returnedAt = now;
        book.returnCopy();
    }

    public LoanStatus status(Instant now) {
        if (returnedAt != null) {
            return LoanStatus.RETURNED;
        }
        return now.isAfter(dueAt) ? LoanStatus.OVERDUE : LoanStatus.ACTIVE;
    }
}
