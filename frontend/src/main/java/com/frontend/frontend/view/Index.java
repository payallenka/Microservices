package com.frontend.frontend.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import com.frontend.frontend.model.BookDataModel;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

// This class represents the main view of the application, mapped to the root URL "/"
@Route("/")
public class Index extends VerticalLayout {

    // Instance of RestTemplate for making HTTP requests
    private final RestTemplate restTemplate;
    
    // URL of the REST API endpoint for fetching book data
    private final String url = "http://localhost:8080"; // Update this URL as necessary

    // Constructor to initialize the view and fetch data
    @Autowired
    public Index(RestTemplate restTemplate) {
        this.restTemplate = restTemplate; // Injected RestTemplate instance
        try {
            // Fetch book data from the REST API
            List<BookDataModel> bookData = getData(); 
            // Set up the UI with the fetched book data
            setupUI(bookData); 
        } catch (RestClientException e) {
            e.printStackTrace(); // Log any exceptions that occur during data fetching
            // Here you could handle the error, e.g., by showing a notification to the user
        }
    }

    // Method to retrieve book data from the specified REST API endpoint
    private List<BookDataModel> getData() {
        // Make a GET request to the REST endpoint and specify the expected response type
        return restTemplate.exchange(url, HttpMethod.GET, null, 
            new ParameterizedTypeReference<List<BookDataModel>>() {}).getBody();
    }

    // Method to set up the user interface components using the fetched book data
    private void setupUI(List<BookDataModel> bookDataModels) {
        // Create a Grid component to display the book data
        Grid<BookDataModel> grid = new Grid<>(); // Specify the type for the grid
        
        // Set the items of the grid to the fetched book data
        grid.setItems(bookDataModels); 

        // Define columns in the grid based on the properties of BookDataModel
        grid.addColumn(BookDataModel::getBookName).setHeader("Book Name"); // Column for book name
        grid.addColumn(BookDataModel::getBookAuthor).setHeader("Author"); // Column for author name
        grid.addColumn(BookDataModel::getPublicationName).setHeader("Publication"); // Column for publication name

        // Add the grid to the main layout
        add(grid); 
    }
}
