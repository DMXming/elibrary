package com.dmx.elibrary.dto;

import com.dmx.elibrary.domain.Book;
import java.time.LocalDate;

public record BookDetailResponse(Long id, String title, String author, String isbn, String publisher,
        LocalDate publishedDate, String category, String description, int totalCopies, int availableCopies) {

    public static BookDetailResponse from(Book book) {
        return new BookDetailResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getPublisher(), book.getPublishedDate(), book.getCategory(), book.getDescription(),
                book.getTotalCopies(), book.getAvailableCopies());
    }
}
