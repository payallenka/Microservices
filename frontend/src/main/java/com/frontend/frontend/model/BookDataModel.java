package com.frontend.frontend.model;

/**
 * Represents a book with details such as name, author, and publication.
 */
public class BookDataModel {

    // Fields to hold the details of the book
    private String bookName;        // Changed to private for encapsulation
    private String bookAuthor;      // Changed to private for encapsulation
    private String publicationName;  // Changed to private for encapsulation

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
    
    // Getter for bookName
    public String getBookName() {
        return bookName;
    }

    // Getter for bookAuthor
    public String getBookAuthor() {
        return bookAuthor;
    }

    // Getter for publicationName
    public String getPublicationName() {
        return publicationName;
    }

    // Optional: Setters can be added here for mutability
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }
}
