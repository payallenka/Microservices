package com.bookstore.webui.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.bookstore.webui.client.ApiModels.Book;
import com.bookstore.webui.client.ApiModels.BookPage;
import com.bookstore.webui.client.ApiModels.BookRequest;
import com.bookstore.webui.client.ApiModels.RatingSummary;
import com.bookstore.webui.client.ApiModels.Review;
import com.bookstore.webui.client.ApiModels.ReviewRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Talks to book-service and review-service through the API gateway. Every failure, including
 * the gateway being unreachable, surfaces as an {@link ApiException} with a user-friendly message.
 */
@Component
public class BookstoreClient {

	private static final int PAGE_SIZE = 100;

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public BookstoreClient(RestClient.Builder builder, @Value("${bookstore.api-url}") String apiUrl,
			ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.restClient = builder
				.baseUrl(apiUrl)
				.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
					throw toApiException(response);
				})
				.build();
	}

	public List<Book> searchBooks(String query) {
		BookPage page = call(() -> restClient.get()
				.uri(uri -> uri.path("/api/books")
						.queryParam("search", query == null ? "" : query)
						.queryParam("size", PAGE_SIZE)
						.build())
				.retrieve()
				.body(BookPage.class));
		return page == null ? List.of() : page.content();
	}

	public Book createBook(BookRequest request) {
		return call(() -> restClient.post().uri("/api/books").body(request).retrieve().body(Book.class));
	}

	public Book updateBook(Long id, BookRequest request) {
		return call(() -> restClient.put().uri("/api/books/{id}", id).body(request).retrieve().body(Book.class));
	}

	public void deleteBook(Long id) {
		call(() -> restClient.delete().uri("/api/books/{id}", id).retrieve().toBodilessEntity());
	}

	public List<Review> reviewsFor(Long bookId) {
		List<Review> reviews = call(() -> restClient.get()
				.uri("/api/reviews?bookId={bookId}", bookId)
				.retrieve()
				.body(new ParameterizedTypeReference<List<Review>>() {
				}));
		return reviews == null ? List.of() : reviews;
	}

	public RatingSummary ratingFor(Long bookId) {
		return call(() -> restClient.get()
				.uri("/api/reviews/summary?bookId={bookId}", bookId)
				.retrieve()
				.body(RatingSummary.class));
	}

	public Review addReview(ReviewRequest request) {
		return call(() -> restClient.post().uri("/api/reviews").body(request).retrieve().body(Review.class));
	}

	private <T> T call(Supplier<T> request) {
		try {
			return request.get();
		} catch (ResourceAccessException ex) {
			throw new ApiException(503, "The bookstore API is unreachable, please try again shortly", Map.of());
		}
	}

	private ApiException toApiException(ClientHttpResponse response) throws IOException {
		int status = response.getStatusCode().value();
		String message = "Request failed with status " + status;
		Map<String, String> fieldErrors = new HashMap<>();
		try {
			JsonNode body = objectMapper.readTree(response.getBody());
			if (body != null && body.hasNonNull("detail")) {
				message = body.get("detail").asText();
			}
			if (body != null && body.has("errors")) {
				body.get("errors").fields()
						.forEachRemaining(entry -> fieldErrors.put(entry.getKey(), entry.getValue().asText()));
			}
		} catch (IOException ignored) {
			// body was not JSON (e.g. a gateway error page); keep the generic message
		}
		return new ApiException(status, message, fieldErrors);
	}
}
