package com.dmx.elibrary.controller;

import com.dmx.elibrary.dto.BookDetailResponse;
import com.dmx.elibrary.dto.BookSummaryResponse;
import com.dmx.elibrary.dto.PageResponse;
import com.dmx.elibrary.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/books")
    public PageResponse<BookSummaryResponse> listBooks(@RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return bookService.search(q, pageable);
    }

    @GetMapping("/books/{id}")
    public BookDetailResponse getBook(@PathVariable Long id) {
        return bookService.getDetail(id);
    }
}
