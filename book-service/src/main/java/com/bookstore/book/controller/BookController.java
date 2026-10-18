package com.bookstore.book.controller;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.dto.PageResponse;
import com.bookstore.book.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Manage the book catalogue")
public class BookController {

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping
	@Operation(summary = "List books", description = "Optionally filter by a case-insensitive match on title, author or publisher")
	public PageResponse<BookResponse> list(@RequestParam(required = false) String search,
			@ParameterObject @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
		return PageResponse.from(bookService.search(search, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a book by id")
	public BookResponse get(@PathVariable Long id) {
		return bookService.getById(id);
	}

	@PostMapping
	@Operation(summary = "Add a book")
	public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
		BookResponse created = bookService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Replace a book's details")
	public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
		return bookService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a book")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		bookService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
