package com.dmx.elibrary.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.dmx.elibrary.domain.Book;
import com.dmx.elibrary.dto.BookDetailResponse;
import com.dmx.elibrary.dto.BookSummaryResponse;
import com.dmx.elibrary.dto.PageResponse;
import com.dmx.elibrary.exception.BusinessException;
import com.dmx.elibrary.exception.ErrorCode;
import com.dmx.elibrary.repository.BookRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    BookRepository bookRepository;

    @InjectMocks
    BookServiceImpl bookService;

    @Test
    void searchMapsPageToPageResponse() {
        Page<Book> page = new PageImpl<>(
                List.of(book("Clean Code", "Robert C. Martin", "Software Engineering"),
                        book("Domain-Driven Design", "Eric Evans", "Software Design")),
                PageRequest.of(0, 20), 2);
        given(bookRepository.search(isNull(), any(PageRequest.class))).willReturn(page);

        PageResponse<BookSummaryResponse> result = bookService.search(null, PageRequest.of(0, 20));

        assertThat(result.content())
                .extracting(BookSummaryResponse::title)
                .containsExactly("Clean Code", "Domain-Driven Design");
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(2);

        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        then(bookRepository).should().search(isNull(), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("id").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getDetailReturnsMappedBook() {
        Book book = book("Clean Code", "Robert C. Martin", "Software Engineering");
        given(bookRepository.findById(1L)).willReturn(Optional.of(book));

        BookDetailResponse detail = bookService.getDetail(1L);

        assertThat(detail.title()).isEqualTo("Clean Code");
        assertThat(detail.isbn()).isEqualTo(book.getIsbn());
        assertThat(detail.availableCopies()).isEqualTo(book.getAvailableCopies());
    }

    @Test
    void getDetailThrowsWhenBookMissing() {
        given(bookRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getDetail(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    private Book book(String title, String author, String category) {
        return Book.builder()
                .title(title)
                .author(author)
                .isbn("978-" + title.hashCode())
                .publisher("Test Publisher")
                .publishedDate(LocalDate.of(2020, 1, 1))
                .category(category)
                .description("Test description")
                .totalCopies(2)
                .availableCopies(2)
                .build();
    }
}
