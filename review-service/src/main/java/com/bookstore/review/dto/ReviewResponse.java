package com.bookstore.review.dto;

import java.time.Instant;

import com.bookstore.review.entity.Review;

public record ReviewResponse(Long id, Long bookId, String reviewer, int rating, String comment, Instant createdAt) {

	public static ReviewResponse from(Review review) {
		return new ReviewResponse(review.getId(), review.getBookId(), review.getReviewer(), review.getRating(),
				review.getComment(), review.getCreatedAt());
	}
}
