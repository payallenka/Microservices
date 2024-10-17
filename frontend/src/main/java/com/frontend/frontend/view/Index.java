package com.frontend.frontend.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import com.frontend.frontend.model.BookDataModel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("/") // This annotation maps this class to the root route
public class Index extends VerticalLayout { // Extends VerticalLayout for stacking UI components

    private Grid<BookDataModel> grid = new Grid<>(); // Grid to display book data

    private final RestTemplate restTemplate; // REST client for API calls
    private final String url = "http://localhost:8080"; // Base URL for API requests

    @Autowired // Dependency injection for RestTemplate
    public Index(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        // Section to add a book
        H5 headingToAddBook = new H5("Add a Book"); // Heading for the add book section
        TextField bookNameTextField = new TextField("Book Name"); // Input field for book name
        TextField bookAuthorTextField = new TextField("Book Author"); // Input field for book author
        TextField publisherNameTextField = new TextField("Publisher Name"); // Input field for publisher name
        Button btnToAdd = new Button("Add Book", event -> addBookData(bookNameTextField.getValue(), bookAuthorTextField.getValue(), publisherNameTextField.getValue())); // Button to trigger adding book data

        // Section to delete a book
        H5 headingToDelete = new H5("Delete a Book"); // Heading for the delete book section
        TextField bookNameTextFieldToDeleField = new TextField("Book Name"); // Input field for the book name to delete
        TextField bookAuthorTextFieldToDeleField = new TextField("Book Author"); // Input field for the author of the book to delete
        TextField publisherNameTextFieldToDeleField = new TextField("Publisher Name"); // Input field for the publisher of the book to delete
        Button btnToDelete = new Button("Delete Book"); // Button to trigger the delete action (logic to be implemented)

        // Horizontal layouts for adding and deleting books
        HorizontalLayout hLayoutToAddBook = new HorizontalLayout(bookNameTextField, bookAuthorTextField, publisherNameTextField, btnToAdd); // Layout for add book inputs
        HorizontalLayout hLayoutToDeleteBook = new HorizontalLayout(bookNameTextFieldToDeleField, bookAuthorTextFieldToDeleField, publisherNameTextFieldToDeleField, btnToDelete); // Layout for delete book inputs

        // Adding all components to the main layout
        add(headingToAddBook, hLayoutToAddBook, headingToDelete, hLayoutToDeleteBook);

        // Section to load the existing book data from the server
        loadBookData();
    }

    // Method to load existing book data from the server
    private void loadBookData() {
        try {
            List<BookDataModel> bookData = getData(); // Fetch book data
            setupUI(bookData); // Set up the grid UI with the fetched data
        } catch (RestClientException e) {
            // Show a notification if fetching data fails
            Notification.show("Failed to fetch book data: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            e.printStackTrace(); // Log the exception
        }
    }

    // Method to perform a GET request to retrieve book data
    private List<BookDataModel> getData() {
        return restTemplate.exchange(url, HttpMethod.GET, null,
            new ParameterizedTypeReference<List<BookDataModel>>() {}).getBody(); // Exchange to get list of books
    }

    // Method to set up the grid with book data
    private void setupUI(List<BookDataModel> bookDataModels) {
        grid.setItems(bookDataModels); // Set items in the grid

        // Add columns to the grid for book properties
        grid.addColumn(BookDataModel::getBookName).setHeader("Book Name");
        grid.addColumn(BookDataModel::getBookAuthor).setHeader("Author");
        grid.addColumn(BookDataModel::getPublicationName).setHeader("Publication");

        add(grid); // Add the grid to the main layout
    }

    // Method to add new book data through a POST request
    public void addBookData(String bookName, String bookAuthor, String publicationName) {
        BookDataModel newBookData = new BookDataModel(); // Create a new book data model
        newBookData.setBookName(bookName); // Set book name
        newBookData.setBookAuthor(bookAuthor); // Set book author
        newBookData.setPublicationName(publicationName); // Set publisher name

        try {
            // Perform a POST request to add the new book
            ResponseEntity<BookDataModel> response = restTemplate.postForEntity(url, newBookData, BookDataModel.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // Show notification if book is added successfully
                Notification.show("Book added successfully: " + newBookData.getBookName(), 3000, Notification.Position.MIDDLE);
                refreshGrid(); // Refresh the grid to show the updated list
            } else {
                // Show notification if adding the book fails
                Notification.show("Failed to add the book: " + response.getStatusCode(), 3000, Notification.Position.MIDDLE);
            }
        } catch (RestClientException e) {
            // Show notification if an error occurs during the add operation
            Notification.show("Error occurred while adding the book: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            e.printStackTrace(); // Log the exception
        }
    }

    // Method to refresh the grid with the latest book data
    private void refreshGrid() {
        List<BookDataModel> updatedData = getData(); // Fetch updated data
        grid.setItems(updatedData); // Update the grid with new data
    }
}