package com.dmx.elibrary.service.impl;

import com.dmx.elibrary.domain.Book;
import com.dmx.elibrary.dto.BookDetailResponse;
import com.dmx.elibrary.dto.BookSummaryResponse;
import com.dmx.elibrary.dto.PageResponse;
import com.dmx.elibrary.exception.BusinessException;
import com.dmx.elibrary.exception.ErrorCode;
import com.dmx.elibrary.repository.BookRepository;
import com.dmx.elibrary.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public PageResponse<BookSummaryResponse> search(String q, Pageable pageable) {
        // Normalize the client-provided pageable: stable sort order, same page size
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by("id").ascending());
        Page<Book> books = bookRepository.search(q == null ? null : q.trim(), pageRequest);
        return PageResponse.from(books.map(BookSummaryResponse::from));
    }

    @Override
    public BookDetailResponse getDetail(Long id) {
        return bookRepository.findById(id)
                .map(BookDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
    }
}
