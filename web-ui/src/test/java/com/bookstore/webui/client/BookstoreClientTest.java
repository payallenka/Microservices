package com.bookstore.webui.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.bookstore.webui.client.ApiModels.Book;
import com.bookstore.webui.client.ApiModels.BookRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class BookstoreClientTest {

	private MockRestServiceServer server;
	private BookstoreClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		client = new BookstoreClient(builder, "http://api-gateway", new ObjectMapper().registerModule(new JavaTimeModule()));
	}

	@Test
	void searchBooksReadsPageContent() {
		server.expect(requestTo("http://api-gateway/api/books?search=java&size=100"))
				.andRespond(withSuccess("""
						{"content":[{"id":1,"title":"Effective Java","author":"Joshua Bloch",
						  "publisher":"Addison-Wesley","isbn":"9780134685991"}],
						 "page":0,"size":100,"totalElements":1,"totalPages":1}
						""", MediaType.APPLICATION_JSON));

		List<Book> books = client.searchBooks("java");

		assertThat(books).extracting(Book::title).containsExactly("Effective Java");
		server.verify();
	}

	@Test
	void createBookPostsJson() {
		server.expect(requestTo("http://api-gateway/api/books"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json("""
						{"title":"Clean Code","author":"Robert C. Martin","publisher":"Prentice Hall","isbn":"9780132350884"}
						"""))
				.andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("""
						{"id":2,"title":"Clean Code","author":"Robert C. Martin","publisher":"Prentice Hall","isbn":"9780132350884"}
						"""));

		Book created = client.createBook(new BookRequest("Clean Code", "Robert C. Martin", "Prentice Hall", "9780132350884"));

		assertThat(created.id()).isEqualTo(2L);
	}

	@Test
	void problemDetailBecomesApiExceptionWithBackendMessage() {
		server.expect(requestTo("http://api-gateway/api/books/9"))
				.andExpect(method(HttpMethod.DELETE))
				.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body("""
						{"status":404,"detail":"Book with id 9 was not found"}
						"""));

		assertThatThrownBy(() -> client.deleteBook(9L))
				.isInstanceOf(ApiException.class)
				.hasMessage("Book with id 9 was not found")
				.extracting(ex -> ((ApiException) ex).getStatus()).isEqualTo(404);
	}

	@Test
	void validationErrorsAreExposedPerField() {
		server.expect(requestTo("http://api-gateway/api/books"))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_PROBLEM_JSON).body("""
						{"status":400,"detail":"Validation failed","errors":{"isbn":"must be a valid ISBN"}}
						"""));

		assertThatThrownBy(() -> client.createBook(new BookRequest("t", "a", "p", "bad")))
				.isInstanceOf(ApiException.class)
				.satisfies(ex -> assertThat(((ApiException) ex).getFieldErrors())
						.containsEntry("isbn", "must be a valid ISBN"));
	}
}
