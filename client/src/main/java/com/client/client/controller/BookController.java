package com.client.client.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.client.client.model.BookDataModel; // Ensure correct casing
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/") // Base URL for the controller
public class BookController {

    private final ObjectMapper objectMapper; // ObjectMapper for JSON serialization/deserialization

    // Constructor for dependency injection of ObjectMapper
    public BookController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Saves the list of BookDataModel objects to a JSON file.
     *
     * @param bookData List of books to be saved.
     */
    private void saveData(List<BookDataModel> bookData) {
        try {
            objectMapper.writeValue(new File("items.json"), bookData); // Write data to items.json
        } catch (IOException e) {
            // Log the exception (consider using a logging framework)
            e.printStackTrace();
        }
    }

    /**
     * Loads the list of BookDataModel objects from a JSON file.
     *
     * @return List of books loaded from the file, or an empty list if the file does not exist or an error occurs.
     */
    private List<BookDataModel> loadData() {
        File file = new File("items.json"); // Define the file to load data from
        if (!file.exists()) {
            return new ArrayList<>(); // Return empty list if the file doesn't exist
        }

        try {
            return objectMapper.readValue(file, new TypeReference<List<BookDataModel>>() {}); // Read data from items.json
        } catch (IOException e) {
            // Log the exception (consider using a logging framework)
            e.printStackTrace();
            return new ArrayList<>(); // Return an empty list in case of an error
        }
    }

    /**
     * Handles GET requests to retrieve the list of books.
     *
     * @return ResponseEntity containing the list of BookDataModel objects and HTTP status OK.
     */
    @GetMapping
    public ResponseEntity<List<BookDataModel>> getBooks() {
        List<BookDataModel> bookData = loadData(); // Load the book data
        return ResponseEntity.ok(bookData); // Return the loaded data with HTTP status 200 OK
    }

    /**
     * Handles POST requests to add a new book.
     *
     * @param bookDetails The book details to be added, provided in the request body.
     * @return ResponseEntity containing the added BookDataModel and HTTP status 201 Created.
     */
    @PostMapping
    public ResponseEntity<BookDataModel> addBook(@RequestBody BookDataModel bookDetails) {
        List<BookDataModel> currentData = loadData(); // Load current book data
        currentData.add(bookDetails); // Add the new book details to the list
        saveData(currentData); // Save the updated list to the file
        return ResponseEntity.status(HttpStatus.CREATED).body(bookDetails); // Return the added book with HTTP status 201 Created
    }

    /**
     * Handles DELETE requests to remove a book by its index.
     *
     * @param index The index of the book to be deleted.
     * @return ResponseEntity containing a message indicating the result of the deletion and HTTP status.
     */
    @DeleteMapping("/{index}")
    public ResponseEntity<String> deleteBook(@PathVariable int index) {
        List<BookDataModel> currentBookData = loadData(); // Load current book data

        // Check if the index is out of bounds
        if (index < 0 || index >= currentBookData.size()) {
            return ResponseEntity.badRequest().body("Index out of bounds"); // Return error message if index is invalid
        }

        currentBookData.remove(index); // Remove the book at the specified index
        saveData(currentBookData); // Save the updated list to the file
        return ResponseEntity.ok("Deleted book at index: " + index); // Return success message
    }
}
