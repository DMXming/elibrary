package com.dmx.elibrary.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dmx.elibrary.domain.Book;
import com.dmx.elibrary.domain.Loan;
import com.dmx.elibrary.domain.Patron;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class LoanRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-10-26T10:00:00Z");

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    PatronRepository patronRepository;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    private int seq = 0;

    @Test
    void findActiveLoanByPatronIdAndBookIdFindsUnreturnedLoan() {
        Book book = bookRepository.save(book(2));
        Patron patron = patronRepository.save(patron());

        Loan loan = loanRepository.save(Loan.create(book, patron, NOW, Duration.ofDays(14)));

        assertThat(loanRepository.findActiveLoanByPatronIdAndBookId(patron.getId(), book.getId())).isPresent();

        loan.returnNow(NOW.plusSeconds(1));
        loanRepository.save(loan);

        assertThat(loanRepository.findActiveLoanByPatronIdAndBookId(patron.getId(), book.getId())).isEmpty();
    }

    @Test
    void findActiveLoansByPatronIdOrdersByDueAtAscending() {
        Book book = bookRepository.save(book(2));
        Patron patron = patronRepository.save(patron());

        Loan longLoan = loanRepository.save(Loan.create(book, patron, NOW, Duration.ofDays(14)));
        Loan shortLoan = loanRepository.save(Loan.create(book, patron, NOW, Duration.ofDays(7)));

        assertThat(loanRepository.findActiveLoansByPatronIdOrderByDueAtAsc(patron.getId()))
                .extracting(Loan::getId)
                .containsExactly(shortLoan.getId(), longLoan.getId());

        longLoan.returnNow(NOW.plusSeconds(1));
        loanRepository.save(longLoan);

        assertThat(loanRepository.findActiveLoansByPatronIdOrderByDueAtAsc(patron.getId()))
                .extracting(Loan::getId)
                .containsExactly(shortLoan.getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void optimisticLockingPreventsLostUpdateOnBook() {
        Book book = bookRepository.save(book(1));
        Long id = book.getId();

        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();
        try {
            em1.getTransaction().begin();
            Book b1 = em1.find(Book.class, id);
            em2.getTransaction().begin();
            Book b2 = em2.find(Book.class, id);

            b1.borrowCopy();
            em1.getTransaction().commit();

            b2.borrowCopy();
            // On a raw EntityManager, Hibernate wraps the optimistic lock failure
            // in a RollbackException with the OptimisticLockException as its cause
            assertThatThrownBy(() -> em2.getTransaction().commit())
                    .isInstanceOf(RollbackException.class)
                    .hasCauseInstanceOf(OptimisticLockException.class);
        } finally {
            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }
            if (em1.getTransaction().isActive()) {
                em1.getTransaction().rollback();
            }
            em2.close();
            em1.close();

            // This test runs outside the slice transaction, so clean up the committed row
            EntityManager em3 = entityManagerFactory.createEntityManager();
            em3.getTransaction().begin();
            em3.createQuery("DELETE FROM Book b WHERE b.id = :id").setParameter("id", id).executeUpdate();
            em3.getTransaction().commit();
            em3.close();
        }
    }

    private Book book(int totalCopies) {
        return Book.builder()
                .title("Test Book")
                .author("Test Author")
                .isbn("978-test-" + (++seq))
                .publisher("Test Publisher")
                .publishedDate(LocalDate.of(2020, 1, 1))
                .category("Test")
                .description("Test")
                .totalCopies(totalCopies)
                .availableCopies(totalCopies)
                .build();
    }

    private Patron patron() {
        return Patron.builder()
                .name("Test Patron")
                .email("test@example.com")
                .build();
    }
}
