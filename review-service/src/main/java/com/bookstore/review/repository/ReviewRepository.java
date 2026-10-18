package com.bookstore.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookstore.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);

	long countByBookId(Long bookId);

	@Query("select avg(r.rating) from Review r where r.bookId = :bookId")
	Double averageRatingForBook(@Param("bookId") Long bookId);
}
