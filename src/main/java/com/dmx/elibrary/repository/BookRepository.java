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
            WHERE LOWER(b.title) LIKE LOWER(:pattern) ESCAPE '\\'
               OR LOWER(b.author) LIKE LOWER(:pattern) ESCAPE '\\'
               OR LOWER(b.category) LIKE LOWER(:pattern) ESCAPE '\\'
            """)
    Page<Book> searchEscaped(@Param("pattern") String pattern, Pageable pageable);

    /**
     * Searches title/author/category case-insensitively, treating {@code %},
     * {@code _}, and {@code \} in the query as literal characters rather than
     * LIKE wildcards.
     */
    default Page<Book> search(String q, Pageable pageable) {
        if (q == null || q.isEmpty()) {
            return searchEscaped("%%", pageable);
        }
        String escaped = q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return searchEscaped("%" + escaped + "%", pageable);
    }
}
