package com.bookstore.webui.client;

import java.util.Map;

/**
 * A backend call failed. The message is the backend's ProblemDetail {@code detail}, safe to show to users.
 */
public class ApiException extends RuntimeException {

	private final int status;
	private final Map<String, String> fieldErrors;

	public ApiException(int status, String message, Map<String, String> fieldErrors) {
		super(message);
		this.status = status;
		this.fieldErrors = Map.copyOf(fieldErrors);
	}

	public int getStatus() {
		return status;
	}

	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}
}
