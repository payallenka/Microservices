package com.bookstore.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.bookstore.review.dto.RatingSummary;
import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.exception.BookNotFoundException;
import com.bookstore.review.exception.BookServiceUnavailableException;
import com.bookstore.review.service.ReviewService;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

	private static final String VALID_BODY = """
			{"bookId":1,"reviewer":"Ada","rating":5,"comment":"Great"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReviewService reviewService;

	@Test
	void listReturnsReviewsForBook() throws Exception {
		when(reviewService.findByBook(1L)).thenReturn(List.of(
				new ReviewResponse(3L, 1L, "Ada", 5, "Great", Instant.parse("2026-01-01T00:00:00Z"))));

		mockMvc.perform(get("/api/reviews").param("bookId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].reviewer").value("Ada"));
	}

	@Test
	void listRequiresBookId() throws Exception {
		mockMvc.perform(get("/api/reviews"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void summaryReturnsAverage() throws Exception {
		when(reviewService.summarize(1L)).thenReturn(new RatingSummary(1L, 2, 4.5));

		mockMvc.perform(get("/api/reviews/summary").param("bookId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.averageRating").value(4.5));
	}

	@Test
	void createRejectsOutOfRangeRating() throws Exception {
		mockMvc.perform(post("/api/reviews").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"bookId":1,"reviewer":"Ada","rating":6}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.rating").exists());
	}

	@Test
	void createReturns404WhenBookDoesNotExist() throws Exception {
		when(reviewService.create(any(ReviewRequest.class))).thenThrow(new BookNotFoundException(1L));

		mockMvc.perform(post("/api/reviews").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isNotFound());
	}

	@Test
	void createReturns503WhenBookServiceIsDown() throws Exception {
		when(reviewService.create(any(ReviewRequest.class)))
				.thenThrow(new BookServiceUnavailableException(new RuntimeException("connection refused")));

		mockMvc.perform(post("/api/reviews").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isServiceUnavailable());
	}
}
