package com.dmx.elibrary.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dmx.elibrary.domain.Book;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class BookRepositoryTest {

    @Autowired
    BookRepository bookRepository;

    private int seq = 0;

    @Test
    void searchMatchesTitleAuthorAndCategoryCaseInsensitively() {
        bookRepository.save(book("Clean Code", "Robert C. Martin", "Software Engineering"));
        bookRepository.save(book("Domain-Driven Design", "Eric Evans", "Software Design"));

        assertThat(bookRepository.search("CLEAN", PageRequest.of(0, 20)).getContent())
                .extracting(Book::getTitle)
                .containsExactly("Clean Code");
        assertThat(bookRepository.search("eric evans", PageRequest.of(0, 20)).getContent())
                .extracting(Book::getTitle)
                .containsExactly("Domain-Driven Design");
    }

    @Test
    void searchWithNullQueryReturnsAll() {
        bookRepository.save(book("Clean Code", "Robert C. Martin", "Software Engineering"));
        bookRepository.save(book("Domain-Driven Design", "Eric Evans", "Software Design"));

        Page<Book> result = bookRepository.search(null, PageRequest.of(0, 20, Sort.by("id").ascending()));

        assertThat(result.getContent())
                .extracting(Book::getTitle)
                .containsExactly("Clean Code", "Domain-Driven Design");
    }

    private Book book(String title, String author, String category) {
        return Book.builder()
                .title(title)
                .author(author)
                .isbn("978-" + (++seq))
                .publisher("Test Publisher")
                .publishedDate(LocalDate.of(2020, 1, 1))
                .category(category)
                .description("Test description")
                .totalCopies(2)
                .availableCopies(2)
                .build();
    }
}
