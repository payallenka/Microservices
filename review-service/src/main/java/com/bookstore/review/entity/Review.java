package com.bookstore.review.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Id of the book in book-service. Not a foreign key: each service owns its own database. */
	@Column(name = "book_id", nullable = false)
	private Long bookId;

	@Column(nullable = false, length = 100)
	private String reviewer;

	@Column(nullable = false)
	private int rating;

	@Column(length = 1000)
	private String comment;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Review() {
		// required by JPA
	}

	public Review(Long bookId, String reviewer, int rating, String comment) {
		this.bookId = bookId;
		this.reviewer = reviewer;
		this.rating = rating;
		this.comment = comment;
	}

	public Long getId() {
		return id;
	}

	public Long getBookId() {
		return bookId;
	}

	public String getReviewer() {
		return reviewer;
	}

	public int getRating() {
		return rating;
	}

	public String getComment() {
		return comment;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
