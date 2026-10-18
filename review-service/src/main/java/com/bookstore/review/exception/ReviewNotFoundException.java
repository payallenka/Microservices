package com.bookstore.review.exception;

public class ReviewNotFoundException extends RuntimeException {

	public ReviewNotFoundException(Long id) {
		super("Review with id " + id + " was not found");
	}
}
