package com.bookstore.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.bookstore.review.client.BookClient;
import com.bookstore.review.dto.RatingSummary;
import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.entity.Review;
import com.bookstore.review.exception.BookNotFoundException;
import com.bookstore.review.exception.BookServiceUnavailableException;
import com.bookstore.review.exception.ReviewNotFoundException;
import com.bookstore.review.repository.ReviewRepository;

import feign.FeignException;

@Service
@Transactional(readOnly = true)
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final BookClient bookClient;

	public ReviewService(ReviewRepository reviewRepository, BookClient bookClient) {
		this.reviewRepository = reviewRepository;
		this.bookClient = bookClient;
	}

	public List<ReviewResponse> findByBook(Long bookId) {
		return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId).stream()
				.map(ReviewResponse::from)
				.toList();
	}

	public RatingSummary summarize(Long bookId) {
		long count = reviewRepository.countByBookId(bookId);
		Double average = reviewRepository.averageRatingForBook(bookId);
		Double rounded = average == null ? null : Math.round(average * 10) / 10.0;
		return new RatingSummary(bookId, count, rounded);
	}

	@Transactional
	public ReviewResponse create(ReviewRequest request) {
		ensureBookExists(request.bookId());
		String comment = StringUtils.hasText(request.comment()) ? request.comment().trim() : null;
		Review review = new Review(request.bookId(), request.reviewer().trim(), request.rating(), comment);
		return ReviewResponse.from(reviewRepository.save(review));
	}

	@Transactional
	public void delete(Long id) {
		if (!reviewRepository.existsById(id)) {
			throw new ReviewNotFoundException(id);
		}
		reviewRepository.deleteById(id);
	}

	/** Synchronous check against book-service so reviews can never point at a book that does not exist. */
	private void ensureBookExists(Long bookId) {
		try {
			bookClient.getBook(bookId);
		} catch (FeignException.NotFound ex) {
			throw new BookNotFoundException(bookId);
		} catch (FeignException ex) {
			throw new BookServiceUnavailableException(ex);
		}
	}
}
