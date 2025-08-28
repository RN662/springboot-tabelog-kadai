package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.repository.ReviewRepository;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	
	public ReviewService(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}
	
	public ReviewEditForm toReviewEditForm(Review review) {
		return new ReviewEditForm(review.getId(), review.getScore(),review.getContent());
	}
	 
	
	@Transactional
	public void create(Shop shop, User user, ReviewRegisterForm reviewRegisterForm) {
		Review review = new Review();
		
		review.setShop(shop);
		review.setUser(user);
		review.setScore(reviewRegisterForm.getScore());
		review.setContent(reviewRegisterForm.getContent());
		
		reviewRepository.save(review);
	}
	
	@Transactional
	public void update(ReviewEditForm reviewEditForm) {
		Review review = reviewRepository.getReferenceById(reviewEditForm.getId());
		
		review.setScore(reviewEditForm.getScore());
		review.setContent(reviewEditForm.getContent());
		
		reviewRepository.save(review);
	}
	
	public boolean hasUserAlreadyReviewed(Shop shop, User user) {
		return reviewRepository.findByShopAndUser(shop, user) != null;
	}
	
	@Transactional
	public void delete(Integer reviewId, User user) {
		Integer userId = user.getId();
		
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "レビューが見つかりません"));
		
		if (!review.getUser().getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "他のユーザーのレビューは削除できません");
		}
		
		reviewRepository.delete(review);
	}
	
	// 店舗の平均評価を取得
	public double getAverageScore(Shop shop) {
		List<Review> reviews = reviewRepository.findByShop(shop);
		
		if (reviews.isEmpty()) {
			return 0.0;
		}
		
		double total = 0.0;
		for (Review review : reviews) {
			total += review.getScore();
		}
		
		double average = total / reviews.size();
		
		return Math.round(average * 10.0) / 10.0;
	}
	
	// レビュー数を取得
	public long getReviewCountByShop(Shop shop) {
		return reviewRepository.countByShop(shop);
	}
	

}
