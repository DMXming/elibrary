package com.dmx.elibrary.service;

import com.dmx.elibrary.dto.BookDetailResponse;
import com.dmx.elibrary.dto.BookSummaryResponse;
import com.dmx.elibrary.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface BookService {

    /**
     * @param q optional keyword matched case-insensitively against title,
     *          author and category; {@code null} or blank lists everything
     */
    PageResponse<BookSummaryResponse> search(String q, Pageable pageable);

    BookDetailResponse getDetail(Long id);
}
