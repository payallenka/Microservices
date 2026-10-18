package com.bookstore.book;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.dto.BookResponse;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * End-to-end test against a real PostgreSQL started by Testcontainers, so the Flyway migrations,
 * JPA mappings and unique constraints are exercised exactly as in production.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "eureka.client.enabled=false")
class BookApiIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private TestRestTemplate rest;

	@Test
	void seedDataIsLoadedByFlyway() {
		ResponseEntity<JsonNode> response = rest.getForEntity("/api/books?search=kleppmann", JsonNode.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().get("content").get(0).get("title").asText())
				.isEqualTo("Designing Data-Intensive Applications");
	}

	@Test
	void fullCrudLifecycle() {
		BookRequest request = new BookRequest("Java Concurrency in Practice", "Brian Goetz", "Addison-Wesley",
				"9780321349606");

		ResponseEntity<BookResponse> created = rest.postForEntity("/api/books", request, BookResponse.class);
		assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Long id = created.getBody().id();

		ResponseEntity<String> duplicate = rest.postForEntity("/api/books", request, String.class);
		assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

		BookRequest changed = new BookRequest("Java Concurrency in Practice", "Brian Goetz et al.",
				"Addison-Wesley", "9780321349606");
		ResponseEntity<BookResponse> updated = rest.exchange("/api/books/" + id, HttpMethod.PUT,
				new HttpEntity<>(changed), BookResponse.class);
		assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(updated.getBody().author()).isEqualTo("Brian Goetz et al.");

		ResponseEntity<Void> deleted = rest.exchange("/api/books/" + id, HttpMethod.DELETE, null, Void.class);
		assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> afterDelete = rest.getForEntity("/api/books/" + id, String.class);
		assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
