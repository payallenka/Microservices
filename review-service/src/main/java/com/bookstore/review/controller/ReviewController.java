package com.bookstore.review.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bookstore.review.dto.RatingSummary;
import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Reader reviews and ratings for books")
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@GetMapping
	@Operation(summary = "List reviews for a book, newest first")
	public List<ReviewResponse> list(@RequestParam Long bookId) {
		return reviewService.findByBook(bookId);
	}

	@GetMapping("/summary")
	@Operation(summary = "Review count and average rating for a book")
	public RatingSummary summary(@RequestParam Long bookId) {
		return reviewService.summarize(bookId);
	}

	@PostMapping
	@Operation(summary = "Add a review", description = "Returns 404 if the book does not exist in book-service")
	public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
		ReviewResponse created = reviewService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a review")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		reviewService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
