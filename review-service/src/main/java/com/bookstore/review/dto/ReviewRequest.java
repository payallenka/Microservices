package com.bookstore.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
		@NotNull @Positive Long bookId,
		@NotBlank @Size(max = 100) String reviewer,
		@Min(1) @Max(5) int rating,
		@Size(max = 1000) String comment) {
}
