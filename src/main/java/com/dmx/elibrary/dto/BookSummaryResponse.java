package com.dmx.elibrary.dto;

import com.dmx.elibrary.domain.Book;

public record BookSummaryResponse(Long id, String title, String author, String category, int availableCopies) {

    public static BookSummaryResponse from(Book book) {
        return new BookSummaryResponse(book.getId(), book.getTitle(), book.getAuthor(),
                book.getCategory(), book.getAvailableCopies());
    }
}
