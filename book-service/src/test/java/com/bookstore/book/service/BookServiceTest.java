package com.bookstore.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.entity.Book;
import com.bookstore.book.exception.BookNotFoundException;
import com.bookstore.book.exception.DuplicateIsbnException;
import com.bookstore.book.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

	private static final String ISBN = "9780134685991";

	@Mock
	private BookRepository bookRepository;

	@InjectMocks
	private BookService bookService;

	@Test
	void searchWithoutQueryReturnsAllBooks() {
		Pageable pageable = PageRequest.of(0, 20);
		when(bookRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(book(1L))));

		Page<BookResponse> result = bookService.search("  ", pageable);

		assertThat(result.getContent()).extracting(BookResponse::id).containsExactly(1L);
		verify(bookRepository, never()).search(any(), any());
	}

	@Test
	void searchWithQueryTrimsAndDelegatesToRepository() {
		Pageable pageable = PageRequest.of(0, 20);
		when(bookRepository.search("bloch", pageable)).thenReturn(new PageImpl<>(List.of(book(1L))));

		Page<BookResponse> result = bookService.search("  bloch ", pageable);

		assertThat(result.getTotalElements()).isEqualTo(1);
	}

	@Test
	void getByIdThrowsWhenBookIsMissing() {
		when(bookRepository.findById(42L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookService.getById(42L))
				.isInstanceOf(BookNotFoundException.class)
				.hasMessageContaining("42");
	}

	@Test
	void createSavesTrimmedBook() {
		when(bookRepository.existsByIsbn(ISBN)).thenReturn(false);
		when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 7L));

		BookResponse created = bookService.create(new BookRequest(" Effective Java ", "Joshua Bloch", "Addison-Wesley", ISBN));

		assertThat(created.id()).isEqualTo(7L);
		assertThat(created.title()).isEqualTo("Effective Java");
	}

	@Test
	void createRejectsDuplicateIsbn() {
		when(bookRepository.existsByIsbn(ISBN)).thenReturn(true);

		assertThatThrownBy(() -> bookService.create(request()))
				.isInstanceOf(DuplicateIsbnException.class);
		verify(bookRepository, never()).save(any());
	}

	@Test
	void updateChangesExistingBook() {
		Book existing = book(1L);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(bookRepository.existsByIsbnAndIdNot(ISBN, 1L)).thenReturn(false);
		when(bookRepository.saveAndFlush(existing)).thenReturn(existing);

		BookResponse updated = bookService.update(1L,
				new BookRequest("Effective Java, 3rd Edition", "Joshua Bloch", "Addison-Wesley", ISBN));

		assertThat(updated.title()).isEqualTo("Effective Java, 3rd Edition");
	}

	@Test
	void updateRejectsIsbnOwnedByAnotherBook() {
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book(1L)));
		when(bookRepository.existsByIsbnAndIdNot(ISBN, 1L)).thenReturn(true);

		assertThatThrownBy(() -> bookService.update(1L, request()))
				.isInstanceOf(DuplicateIsbnException.class);
	}

	@Test
	void deleteThrowsWhenBookIsMissing() {
		when(bookRepository.existsById(9L)).thenReturn(false);

		assertThatThrownBy(() -> bookService.delete(9L)).isInstanceOf(BookNotFoundException.class);
		verify(bookRepository, never()).deleteById(any());
	}

	@Test
	void deleteRemovesExistingBook() {
		when(bookRepository.existsById(1L)).thenReturn(true);

		bookService.delete(1L);

		verify(bookRepository).deleteById(1L);
	}

	private static BookRequest request() {
		return new BookRequest("Effective Java", "Joshua Bloch", "Addison-Wesley", ISBN);
	}

	private static Book book(Long id) {
		return withId(new Book("Effective Java", "Joshua Bloch", "Addison-Wesley", ISBN), id);
	}

	private static Book withId(Book book, Long id) {
		ReflectionTestUtils.setField(book, "id", id);
		return book;
	}
}
