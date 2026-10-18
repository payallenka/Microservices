package com.bookstore.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookstore.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

	boolean existsByIsbn(String isbn);

	boolean existsByIsbnAndIdNot(String isbn, Long id);

	@Query("""
			select b from Book b
			where lower(b.title) like lower(concat('%', :query, '%'))
			   or lower(b.author) like lower(concat('%', :query, '%'))
			   or lower(b.publisher) like lower(concat('%', :query, '%'))
			""")
	Page<Book> search(@Param("query") String query, Pageable pageable);
}
