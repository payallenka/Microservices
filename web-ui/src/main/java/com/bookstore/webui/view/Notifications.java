package com.bookstore.webui.view;

import java.util.stream.Collectors;

import com.bookstore.webui.client.ApiException;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

final class Notifications {

	private Notifications() {
	}

	static void success(String message) {
		Notification.show(message, 3000, Notification.Position.BOTTOM_START)
				.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	}

	static void error(ApiException ex) {
		String message = ex.getFieldErrors().isEmpty()
				? ex.getMessage()
				: ex.getFieldErrors().entrySet().stream()
						.map(error -> error.getKey() + " " + error.getValue())
						.collect(Collectors.joining("; "));
		Notification.show(message, 5000, Notification.Position.BOTTOM_START)
				.addThemeVariants(NotificationVariant.LUMO_ERROR);
	}
}
