package com.bookstore.review.dto;

/**
 * @param averageRating mean rating rounded to one decimal place, or {@code null} when there are no reviews
 */
public record RatingSummary(Long bookId, long reviewCount, Double averageRating) {
}
