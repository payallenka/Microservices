package com.bookstore.webui.view;

import com.bookstore.webui.client.ApiModels.Book;
import com.bookstore.webui.client.ApiModels.BookRequest;

/**
 * Mutable form bean for Vaadin's {@code Binder}; converted to/from the immutable API records.
 */
public class BookForm {

	private String title = "";
	private String author = "";
	private String publisher = "";
	private String isbn = "";

	public static BookForm from(Book book) {
		BookForm form = new BookForm();
		form.title = book.title();
		form.author = book.author();
		form.publisher = book.publisher();
		form.isbn = book.isbn();
		return form;
	}

	public BookRequest toRequest() {
		return new BookRequest(title, author, publisher, isbn);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
}
