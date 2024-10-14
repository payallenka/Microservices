package com.client.client.model;

/**
 * Represents a book with details such as name, author, and publication.
 */
public class BookDataModel {

    // Fields to hold the details of the book
    public String bookName;
    public String bookAuthor;
    public String publicationName;

    /**
     * Default constructor that initializes the book details with default values.
     */
    public BookDataModel() {
        this.bookName = "Unknown Name"; // Default value for book name
        this.bookAuthor = "Unknown Author"; // Default value for author name
        this.publicationName = "Unknown Publication"; // Default value for publication name
    }

    /**
     * Parameterized constructor to create a BookDataModel with specified details.
     *
     * @param bookName        The name of the book.
     * @param bookAuthor      The author of the book.
     * @param publicationName  The name of the publication.
     */
    public BookDataModel(String bookName, String bookAuthor, String publicationName) {
        this.bookName = bookName; // Set the book name
        this.bookAuthor = bookAuthor; // Set the author name
        this.publicationName = publicationName; // Set the publication name
    }
    
    // Getters and setters can be added here for better encapsulation if needed
}
