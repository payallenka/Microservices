package com.bookstore.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.exception.BookNotFoundException;
import com.bookstore.book.exception.DuplicateIsbnException;
import com.bookstore.book.service.BookService;

@WebMvcTest(BookController.class)
class BookControllerTest {

	private static final String VALID_BODY = """
			{"title":"Effective Java","author":"Joshua Bloch","publisher":"Addison-Wesley","isbn":"9780134685991"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BookService bookService;

	@Test
	void listReturnsPagedBooks() throws Exception {
		when(bookService.search(eq("java"), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(response(1L)), PageRequest.of(0, 20), 1));

		mockMvc.perform(get("/api/books").param("search", "java"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.page").value(0));
	}

	@Test
	void getReturns404ProblemDetailWhenMissing() throws Exception {
		when(bookService.getById(99L)).thenThrow(new BookNotFoundException(99L));

		mockMvc.perform(get("/api/books/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("Book with id 99 was not found"));
	}

	@Test
	void createReturns201WithLocation() throws Exception {
		when(bookService.create(any(BookRequest.class))).thenReturn(response(5L));

		mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/books/5"))
				.andExpect(jsonPath("$.id").value(5));
	}

	@Test
	void createReturns400WithFieldErrorsForInvalidBody() throws Exception {
		String invalid = """
				{"title":"","author":"Joshua Bloch","publisher":"Addison-Wesley","isbn":"not-an-isbn"}
				""";

		mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(invalid))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.title").exists())
				.andExpect(jsonPath("$.errors.isbn").exists());
	}

	@Test
	void createReturns409ForDuplicateIsbn() throws Exception {
		when(bookService.create(any(BookRequest.class))).thenThrow(new DuplicateIsbnException("9780134685991"));

		mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isConflict());
	}

	@Test
	void deleteReturns204() throws Exception {
		mockMvc.perform(delete("/api/books/1"))
				.andExpect(status().isNoContent());
	}

	@Test
	void deleteReturns404WhenMissing() throws Exception {
		doThrow(new BookNotFoundException(3L)).when(bookService).delete(3L);

		mockMvc.perform(delete("/api/books/3"))
				.andExpect(status().isNotFound());
	}

	private static BookResponse response(Long id) {
		Instant now = Instant.parse("2026-01-01T00:00:00Z");
		return new BookResponse(id, "Effective Java", "Joshua Bloch", "Addison-Wesley", "9780134685991", now, now);
	}
}
