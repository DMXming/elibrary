package com.dmx.elibrary.repository;

import com.dmx.elibrary.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("""
            SELECT b FROM Book b
            WHERE :q IS NULL
               OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(b.category) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Book> search(@Param("q") String q, Pageable pageable);
}
