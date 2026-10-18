package com.bookstore.webui.view;

import java.util.List;

import com.bookstore.webui.client.ApiException;
import com.bookstore.webui.client.ApiModels.Book;
import com.bookstore.webui.client.BookstoreClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Book catalogue screen: searchable grid on the left; edit form and reviews for the selected book on the right.
 */
@Route("")
@PageTitle("Bookstore")
public class BooksView extends VerticalLayout {

	private final BookstoreClient client;

	private final TextField search = new TextField();
	private final Grid<Book> grid = new Grid<>(Book.class, false);

	private final H3 formHeading = new H3();
	private final TextField title = new TextField("Title");
	private final TextField author = new TextField("Author");
	private final TextField publisher = new TextField("Publisher");
	private final TextField isbn = new TextField("ISBN");
	private final Binder<BookForm> binder = new Binder<>(BookForm.class);
	private final Button save = new Button("Save");
	private final Button delete = new Button("Delete");

	private final ReviewsPanel reviews;

	public BooksView(BookstoreClient client) {
		this.client = client;
		this.reviews = new ReviewsPanel(client);
		setSizeFull();

		configureSearch();
		configureGrid();
		configureForm();

		Button newBook = new Button("New book", VaadinIcon.PLUS.create(), event -> grid.asSingleSelect().clear());
		HorizontalLayout toolbar = new HorizontalLayout(search, newBook);

		VerticalLayout left = new VerticalLayout(toolbar, grid);
		left.setPadding(false);
		left.setSizeFull();

		VerticalLayout right = new VerticalLayout(formHeading, title, author, publisher, isbn,
				new HorizontalLayout(save, delete), reviews);
		right.setPadding(false);
		right.setWidth("28rem");
		right.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);

		HorizontalLayout content = new HorizontalLayout(left, right);
		content.setSizeFull();
		content.setFlexGrow(1, left);

		add(new H2("Bookstore"), content);
		editBook(null);
		refreshGrid();
	}

	private void configureSearch() {
		search.setPlaceholder("Search title, author or publisher");
		search.setPrefixComponent(VaadinIcon.SEARCH.create());
		search.setClearButtonVisible(true);
		search.setWidth("22rem");
		search.setValueChangeMode(ValueChangeMode.LAZY);
		search.addValueChangeListener(event -> refreshGrid());
	}

	private void configureGrid() {
		grid.addColumn(Book::title).setHeader("Title").setFlexGrow(2).setSortable(true);
		grid.addColumn(Book::author).setHeader("Author").setFlexGrow(1).setSortable(true);
		grid.addColumn(Book::publisher).setHeader("Publisher").setFlexGrow(1).setSortable(true);
		grid.addColumn(Book::isbn).setHeader("ISBN").setAutoWidth(true);
		grid.setSizeFull();
		grid.asSingleSelect().addValueChangeListener(event -> editBook(event.getValue()));
	}

	private void configureForm() {
		binder.forField(title).asRequired("Title is required").bind(BookForm::getTitle, BookForm::setTitle);
		binder.forField(author).asRequired("Author is required").bind(BookForm::getAuthor, BookForm::setAuthor);
		binder.forField(publisher).asRequired("Publisher is required")
				.bind(BookForm::getPublisher, BookForm::setPublisher);
		binder.forField(isbn).asRequired("ISBN is required")
				.withValidator(value -> value.matches("\\d{9}[\\dX]|97[89]\\d{10}"), "10 or 13 digits, no dashes")
				.bind(BookForm::getIsbn, BookForm::setIsbn);
		isbn.setHelperText("ISBN-10 or ISBN-13, digits only");

		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		save.addClickListener(event -> saveBook());
		delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		delete.addClickListener(event -> deleteBook());
	}

	private void editBook(Book book) {
		binder.setBean(book == null ? new BookForm() : BookForm.from(book));
		formHeading.setText(book == null ? "Add a book" : "Edit book");
		delete.setVisible(book != null);
		reviews.showBook(book);
	}

	private void saveBook() {
		if (!binder.validate().isOk()) {
			return;
		}
		Book selected = grid.asSingleSelect().getValue();
		try {
			Book saved = selected == null
					? client.createBook(binder.getBean().toRequest())
					: client.updateBook(selected.id(), binder.getBean().toRequest());
			Notifications.success("Saved \"" + saved.title() + "\"");
			refreshGrid();
			grid.select(saved);
		} catch (ApiException ex) {
			Notifications.error(ex);
		}
	}

	private void deleteBook() {
		Book selected = grid.asSingleSelect().getValue();
		if (selected == null) {
			return;
		}
		try {
			client.deleteBook(selected.id());
			Notifications.success("Deleted \"" + selected.title() + "\"");
			grid.asSingleSelect().clear();
			refreshGrid();
		} catch (ApiException ex) {
			Notifications.error(ex);
		}
	}

	private void refreshGrid() {
		try {
			List<Book> books = client.searchBooks(search.getValue());
			grid.setItems(books);
		} catch (ApiException ex) {
			grid.setItems(List.of());
			Notifications.error(ex);
		}
	}
}
