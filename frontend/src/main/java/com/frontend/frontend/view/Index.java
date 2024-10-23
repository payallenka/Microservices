package com.frontend.frontend.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.frontend.frontend.model.BookDataModel;
import com.frontend.frontend.utils.SearchUtils;
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
    private final String url = "http://client:8080"; // Base URL for API requests

    @Autowired // Dependency injection for RestTemplate
    public Index(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        // Create main layout with two columns
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidth("100%");

        // Create a vertical layout for CRUD operations
        VerticalLayout crudLayout = new VerticalLayout();
        crudLayout.setWidth("50%");

        // Set up the CRUD sections
        setupSearchSection(crudLayout);
        setupAddSection(crudLayout);
        setupDeleteSection(crudLayout);
        setupUpdateSection(crudLayout);

        // Set up the grid
        setupGrid();

        // Add CRUD layout and grid to the main layout
        mainLayout.add(crudLayout, grid);
        add(mainLayout);
        
        // Load existing book data
        loadBookData();
    }

    private void setupSearchSection(VerticalLayout crudLayout) {
        H5 headingToSearchBook = new H5("Search");
        TextField bookNameToSearch = new TextField("Book Name");
        TextField bookAuthorToSearch = new TextField("Book Author");
        TextField publicationNameToSearch = new TextField("Publisher Name");
        Button btnToSearch = new Button("Search");

        btnToSearch.addClickListener(event -> {
            String bookName = bookNameToSearch.getValue();
            String bookAuthor = bookAuthorToSearch.getValue();
            String publicationName = publicationNameToSearch.getValue();

            List<BookDataModel> dataList = getData();
            List<BookDataModel> searchResultGrid = linearSearch(dataList, bookName, bookAuthor, publicationName);
            grid.setItems(searchResultGrid);
        });

        HorizontalLayout hLayoutToSearchBook = new HorizontalLayout(bookNameToSearch, bookAuthorToSearch, publicationNameToSearch);
        crudLayout.add(headingToSearchBook, hLayoutToSearchBook, btnToSearch);
    }

    private void setupAddSection(VerticalLayout crudLayout) {
        H5 headingToAddBook = new H5("Add a Book");
        TextField bookNameTextField = new TextField("Book Name");
        TextField bookAuthorTextField = new TextField("Book Author");
        TextField publisherNameTextField = new TextField("Publisher Name");
        Button btnToAdd = new Button("Add Book", event -> addBookData(bookNameTextField.getValue(), bookAuthorTextField.getValue(), publisherNameTextField.getValue()));

        HorizontalLayout hLayoutToAddBook = new HorizontalLayout(bookNameTextField, bookAuthorTextField, publisherNameTextField);
        crudLayout.add(headingToAddBook, hLayoutToAddBook, btnToAdd);
    }

    private void setupDeleteSection(VerticalLayout crudLayout) {
        H5 headingToDelete = new H5("Delete a Book");
        TextField bookNameTextFieldToDeleField = new TextField("Book Name");
        TextField bookAuthorTextFieldToDeleField = new TextField("Book Author");
        TextField publisherNameTextFieldToDeleField = new TextField("Publisher Name");
        Button btnToDelete = new Button("Delete Book");

        btnToDelete.addClickListener(event -> {
            String bookName = bookNameTextFieldToDeleField.getValue();
            String bookAuthor = bookAuthorTextFieldToDeleField.getValue();
            String publicationName = publisherNameTextFieldToDeleField.getValue();

            deleteData(bookName, bookAuthor, publicationName);
        });

        HorizontalLayout hLayoutToDeleteBook = new HorizontalLayout(bookNameTextFieldToDeleField, bookAuthorTextFieldToDeleField, publisherNameTextFieldToDeleField);
        crudLayout.add(headingToDelete, hLayoutToDeleteBook, btnToDelete);
    }

    private void setupUpdateSection(VerticalLayout crudLayout) {
        H5 headingToUpdate = new H5("Update a Book");

        // First layer with three inputs
        TextField bookNameTextFieldToUpdate = new TextField("Book Name");
        TextField bookAuthorTextFieldToUpdate = new TextField("Book Author");
        TextField publisherNameTextFieldToUpdate = new TextField("Publisher Name");
        HorizontalLayout hLayoutToUpdateFirstLayer = new HorizontalLayout(bookNameTextFieldToUpdate, bookAuthorTextFieldToUpdate, publisherNameTextFieldToUpdate);

        // Second layer with two inputs
        TextField oldData = new TextField("Old Data");
        TextField newData = new TextField("New Data");
        HorizontalLayout hLayoutToUpdateSecondLayer = new HorizontalLayout(oldData, newData);

        // Button for updating the book
        Button btnToUpdate = new Button("Update Book");

        btnToUpdate.addClickListener(event -> {
            String bookName = bookNameTextFieldToUpdate.getValue();
            String bookAuthor = bookAuthorTextFieldToUpdate.getValue();
            String publicationName = publisherNameTextFieldToUpdate.getValue();
            String oldDataToChange = oldData.getValue();
            String newDataToChangeTo = newData.getValue();

            updateData(bookName, bookAuthor, publicationName, oldDataToChange, newDataToChangeTo);
        });

        VerticalLayout updateLayout = new VerticalLayout(hLayoutToUpdateFirstLayer, hLayoutToUpdateSecondLayer, btnToUpdate);
        crudLayout.add(headingToUpdate, updateLayout);
    }

    private void setupGrid() {
        grid.setWidth("50%"); // Set the grid width to take equal space
        grid.addColumn(BookDataModel::getBookName).setHeader("Book Name");
        grid.addColumn(BookDataModel::getBookAuthor).setHeader("Author");
        grid.addColumn(BookDataModel::getPublicationName).setHeader("Publication");
        grid.getElement().getStyle().set("border-bottom", "none"); // Remove bottom border
    }

    private List<BookDataModel> linearSearch(List<BookDataModel> dataList, String bookName, String bookAuthor, String publicationName) {
        return SearchUtils.linearSearch(dataList, bookName, bookAuthor, publicationName);
    }

    // Method to load existing book data from the server
    private void loadBookData() {
        try {
            List<BookDataModel> bookData = getData();
            setupUI(bookData);
        } catch (RestClientException e) {
            Notification.show("Failed to fetch book data: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    // Method to perform a GET request to retrieve book data
    private List<BookDataModel> getData() {
        return restTemplate.exchange(url, HttpMethod.GET, null,
            new ParameterizedTypeReference<List<BookDataModel>>() {}).getBody();
    }

    // Method to set up the grid with book data
    private void setupUI(List<BookDataModel> bookDataModels) {
        grid.setItems(bookDataModels);
    }

    // Method to add new book data through a POST request
    public void addBookData(String bookName, String bookAuthor, String publicationName) {
        BookDataModel newBookData = new BookDataModel();
        newBookData.setBookName(bookName);
        newBookData.setBookAuthor(bookAuthor);
        newBookData.setPublicationName(publicationName);

        try {
            ResponseEntity<BookDataModel> response = restTemplate.postForEntity(url, newBookData, BookDataModel.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                Notification.show("Book added successfully: " + newBookData.getBookName(), 3000, Notification.Position.MIDDLE);
                refreshGrid();
            } else {
                Notification.show("Failed to add the book: " + response.getStatusCode(), 3000, Notification.Position.MIDDLE);
            }
        } catch (RestClientException e) {
            Notification.show("Error occurred while adding the book: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    public void deleteData(String bookName, String bookAuthor, String publicationName) {
        List<BookDataModel> bookData = getData();
        String trimmedBookName = bookName != null ? bookName.trim() : null;
        String trimmedBookAuthor = bookAuthor != null ? bookAuthor.trim() : null;
        String trimmedPublicationName = publicationName != null ? publicationName.trim() : null;
    
        boolean bookFound = false;
    
        for (int i = 0; i < bookData.size(); i++) {
            BookDataModel currentBook = bookData.get(i);
            boolean matches = true;
    
            if (trimmedBookName != null && !trimmedBookName.isEmpty() && !currentBook.getBookName().equalsIgnoreCase(trimmedBookName)) {
                matches = false;
            }
            if (trimmedBookAuthor != null && !trimmedBookAuthor.isEmpty() && !currentBook.getBookAuthor().equalsIgnoreCase(trimmedBookAuthor)) {
                matches = false;
            }
            if (trimmedPublicationName != null && !trimmedPublicationName.isEmpty() && !currentBook.getPublicationName().equalsIgnoreCase(trimmedPublicationName)) {
                matches = false;
            }
    
            if (matches) {
                bookFound = true;
    
                try {
                    // Send a DELETE request using the index in the URL
                    ResponseEntity<Void> response = restTemplate.exchange(url + "/" + i, HttpMethod.DELETE, null, Void.class);
    
                    if (response.getStatusCode().is2xxSuccessful()) {
                        Notification.show("The Book has been deleted successfully");
                        refreshGrid(); // Refresh the grid to show updated data
                    } else {
                        Notification.show("Failed to delete the book. Server response: " + response.getStatusCode());
                    }
                } catch (RestClientException e) {
                    Notification.show("Error occurred while deleting the book: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
                    e.printStackTrace();
                }
    
                return; // Exit after handling the deletion
            }
        }
    
        if (!bookFound) {
            Notification.show("Book not found");
        }
    }

    public void updateData(String bookName, String bookAuthor, String publicationName, String oldData, String newData) {
        // Get the complete book data
        List<BookDataModel> originalData = getData();
        
        // Search for the books matching the criteria
        List<BookDataModel> resultSetFromSearch = SearchUtils.linearSearch(originalData, bookName, bookAuthor, publicationName);
        
        for (BookDataModel foundBook : resultSetFromSearch) {
            int originalIndex = originalData.indexOf(foundBook); // Find the original index in the full data
    
            boolean updated = false;
    
            // Check if the oldData matches and set new data accordingly
            if (foundBook.getBookName().equals(oldData)) {
                foundBook.setBookName(newData);
                updated = true;
            } else if (foundBook.getBookAuthor().equals(oldData)) {
                foundBook.setBookAuthor(newData);
                updated = true;
            } else if (foundBook.getPublicationName().equals(oldData)) {
                foundBook.setPublicationName(newData);
                updated = true;
            }
    
            if (updated) {
                try {
                    // Send a PUT request to update the book on the server using the correct index
                    ResponseEntity<BookDataModel> response = restTemplate.exchange(url + "/" + originalIndex, HttpMethod.PUT, new HttpEntity<>(foundBook), BookDataModel.class);
                    
                    if (response.getStatusCode().is2xxSuccessful()) {
                        Notification.show("Updated book information successfully.");
                    } else {
                        Notification.show("Failed to update the book: " + response.getStatusCode(), 3000, Notification.Position.MIDDLE);
                    }
                } catch (RestClientException e) {
                    Notification.show("Error occurred while updating the book: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
                }
                break; // Exit after the first successful update
            }
        }
    
        refreshGrid(); // Refresh the grid to show updated data
    }

    // Method to refresh the grid with the latest book data
    private void refreshGrid() {
        List<BookDataModel> updatedData = getData();
        grid.setItems(updatedData);
    }
}
