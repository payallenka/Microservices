package com.bookstore.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank @Size(max = 150) String author,
		@NotBlank @Size(max = 150) String publisher,
		@Schema(description = "ISBN-10 or ISBN-13, digits only", example = "9780134685991")
		@NotBlank @Pattern(regexp = "^(\\d{9}[\\dX]|97[89]\\d{10})$", message = "must be a valid ISBN-10 or ISBN-13 (digits only)")
		String isbn) {
}
