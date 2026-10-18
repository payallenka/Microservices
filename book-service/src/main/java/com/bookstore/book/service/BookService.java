package com.bookstore.book.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.entity.Book;
import com.bookstore.book.exception.BookNotFoundException;
import com.bookstore.book.exception.DuplicateIsbnException;
import com.bookstore.book.repository.BookRepository;

@Service
@Transactional(readOnly = true)
public class BookService {

	private final BookRepository bookRepository;

	public BookService(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	public Page<BookResponse> search(String query, Pageable pageable) {
		Page<Book> books = StringUtils.hasText(query)
				? bookRepository.search(query.trim(), pageable)
				: bookRepository.findAll(pageable);
		return books.map(BookResponse::from);
	}

	public BookResponse getById(Long id) {
		return BookResponse.from(findBook(id));
	}

	@Transactional
	public BookResponse create(BookRequest request) {
		if (bookRepository.existsByIsbn(request.isbn())) {
			throw new DuplicateIsbnException(request.isbn());
		}
		Book book = new Book(request.title().trim(), request.author().trim(), request.publisher().trim(),
				request.isbn());
		return BookResponse.from(bookRepository.save(book));
	}

	@Transactional
	public BookResponse update(Long id, BookRequest request) {
		Book book = findBook(id);
		if (bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
			throw new DuplicateIsbnException(request.isbn());
		}
		book.update(request.title().trim(), request.author().trim(), request.publisher().trim(), request.isbn());
		// saveAndFlush so the response carries the new updatedAt timestamp
		return BookResponse.from(bookRepository.saveAndFlush(book));
	}

	@Transactional
	public void delete(Long id) {
		if (!bookRepository.existsById(id)) {
			throw new BookNotFoundException(id);
		}
		bookRepository.deleteById(id);
	}

	private Book findBook(Long id) {
		return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
	}
}
