package com.bookstore.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.bookstore.review.client.BookClient;
import com.bookstore.review.client.BookClient.BookSummary;
import com.bookstore.review.dto.RatingSummary;
import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.entity.Review;
import com.bookstore.review.exception.BookNotFoundException;
import com.bookstore.review.exception.BookServiceUnavailableException;
import com.bookstore.review.exception.ReviewNotFoundException;
import com.bookstore.review.repository.ReviewRepository;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private BookClient bookClient;

	@InjectMocks
	private ReviewService reviewService;

	@Test
	void createSavesReviewWhenBookExists() {
		when(bookClient.getBook(1L)).thenReturn(new BookSummary(1L, "Effective Java"));
		when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
			Review review = invocation.getArgument(0);
			ReflectionTestUtils.setField(review, "id", 10L);
			return review;
		});

		ReviewResponse created = reviewService.create(new ReviewRequest(1L, " Ada ", 5, "  "));

		assertThat(created.id()).isEqualTo(10L);
		assertThat(created.reviewer()).isEqualTo("Ada");
		assertThat(created.comment()).isNull();
	}

	@Test
	void createFailsWithNotFoundWhenBookServiceReturns404() {
		when(bookClient.getBook(99L)).thenThrow(mock(FeignException.NotFound.class));

		assertThatThrownBy(() -> reviewService.create(new ReviewRequest(99L, "Ada", 4, null)))
				.isInstanceOf(BookNotFoundException.class);
		verify(reviewRepository, never()).save(any());
	}

	@Test
	void createFailsWithUnavailableWhenBookServiceErrors() {
		when(bookClient.getBook(1L)).thenThrow(mock(FeignException.ServiceUnavailable.class));

		assertThatThrownBy(() -> reviewService.create(new ReviewRequest(1L, "Ada", 4, null)))
				.isInstanceOf(BookServiceUnavailableException.class);
	}

	@Test
	void summarizeRoundsAverageToOneDecimal() {
		when(reviewRepository.countByBookId(1L)).thenReturn(3L);
		when(reviewRepository.averageRatingForBook(1L)).thenReturn(4.3333);

		RatingSummary summary = reviewService.summarize(1L);

		assertThat(summary).isEqualTo(new RatingSummary(1L, 3, 4.3));
	}

	@Test
	void summarizeHasNullAverageWhenThereAreNoReviews() {
		when(reviewRepository.countByBookId(1L)).thenReturn(0L);
		when(reviewRepository.averageRatingForBook(1L)).thenReturn(null);

		assertThat(reviewService.summarize(1L).averageRating()).isNull();
	}

	@Test
	void deleteThrowsWhenReviewIsMissing() {
		when(reviewRepository.existsById(5L)).thenReturn(false);

		assertThatThrownBy(() -> reviewService.delete(5L)).isInstanceOf(ReviewNotFoundException.class);
	}
}
