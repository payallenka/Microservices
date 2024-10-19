package com.frontend.frontend.utils;

import java.util.ArrayList;
import java.util.List;
import com.frontend.frontend.model.BookDataModel;

public class SearchUtils {

    public static List<BookDataModel> linearSearch(List<BookDataModel> dataSet, String bookName, String bookAuthor, String publicationName) {
        List<BookDataModel> resultList = new ArrayList<>(); // Local result list

        for (BookDataModel b : dataSet) {
            String name = b.getBookName();
            String author = b.getBookAuthor();
            String publication = b.getPublicationName();

            // Check for matches, allowing for null checks
            boolean matches = true;
            if (bookName != null && !bookName.trim().isEmpty() && !bookName.equalsIgnoreCase(name)) {
                matches = false;
            }
            if (bookAuthor != null && !bookAuthor.trim().isEmpty() && !bookAuthor.equalsIgnoreCase(author)) {
                matches = false;
            }
            if (publicationName != null && !publicationName.trim().isEmpty() && !publicationName.equalsIgnoreCase(publication)) {
                matches = false;
            }

            // If all specified criteria match, add to results
            if (matches) {
                resultList.add(b); // Directly add the existing BookDataModel
            }
        }

        return resultList; // Return all matching results
    }
}
