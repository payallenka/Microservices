package com.bookstore.webui.view;

import com.bookstore.webui.client.ApiException;
import com.bookstore.webui.client.ApiModels.Book;
import com.bookstore.webui.client.ApiModels.RatingSummary;
import com.bookstore.webui.client.ApiModels.Review;
import com.bookstore.webui.client.ApiModels.ReviewRequest;
import com.bookstore.webui.client.BookstoreClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Shows the rating summary and reviews of the selected book, with a small form to add a review.
 */
class ReviewsPanel extends VerticalLayout {

	private final BookstoreClient client;

	private final H3 heading = new H3("Reviews");
	private final Span summary = new Span();
	private final VerticalLayout reviewList = new VerticalLayout();
	private final TextField reviewer = new TextField("Your name");
	private final Select<Integer> rating = new Select<>();
	private final TextArea comment = new TextArea("Comment");

	private Book book;

	ReviewsPanel(BookstoreClient client) {
		this.client = client;
		setPadding(false);

		rating.setLabel("Rating");
		rating.setItems(5, 4, 3, 2, 1);
		rating.setItemLabelGenerator(stars -> "★".repeat(stars));
		rating.setValue(5);
		comment.setMaxLength(1000);
		comment.setWidthFull();
		reviewList.setPadding(false);
		reviewList.setSpacing(false);

		Button add = new Button("Add review", event -> addReview());
		add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		add(heading, summary, new HorizontalLayout(reviewer, rating), comment, add, reviewList);
		showBook(null);
	}

	void showBook(Book book) {
		this.book = book;
		setVisible(book != null);
		if (book != null) {
			heading.setText("Reviews of " + book.title());
			refresh();
		}
	}

	private void refresh() {
		try {
			RatingSummary rs = client.ratingFor(book.id());
			summary.setText(rs.reviewCount() == 0
					? "No reviews yet"
					: "★ " + rs.averageRating() + " average from " + rs.reviewCount() + " review(s)");

			reviewList.removeAll();
			for (Review review : client.reviewsFor(book.id())) {
				Paragraph line = new Paragraph("★".repeat(review.rating()) + "  " + review.reviewer()
						+ (review.comment() == null ? "" : ": " + review.comment()));
				reviewList.add(line);
			}
		} catch (ApiException ex) {
			summary.setText("Reviews are unavailable: " + ex.getMessage());
			reviewList.removeAll();
		}
	}

	private void addReview() {
		try {
			client.addReview(new ReviewRequest(book.id(), reviewer.getValue(), rating.getValue(), comment.getValue()));
			comment.clear();
			Notifications.success("Review added");
			refresh();
		} catch (ApiException ex) {
			Notifications.error(ex);
		}
	}
}
