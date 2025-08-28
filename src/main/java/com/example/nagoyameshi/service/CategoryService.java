package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopHolidayRepository;
import com.example.nagoyameshi.repository.ShopRepository;

@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final ShopRepository shopRepository;
	private final ShopHolidayRepository shopHolidayRepository;
	private final ReservationRepository reservationRepository;
	private final FavoriteRepository favoriteRepository;
	private final ReviewRepository reviewRepository;
	
	public CategoryService(CategoryRepository categoryRepository, ShopRepository shopRepository, ShopHolidayRepository shopHolidayRepository, ReservationRepository reservationRepository, FavoriteRepository favoriteRepository, ReviewRepository reviewRepository) {
		this.categoryRepository = categoryRepository;
		this.shopRepository = shopRepository;
		this.shopHolidayRepository = shopHolidayRepository;
		this.reservationRepository = reservationRepository;
		this.favoriteRepository = favoriteRepository;
		this.reviewRepository = reviewRepository;
	}
	
	// コントローラから移動
	public CategoryEditForm toCategoryEditForm(Category category) {
		return new CategoryEditForm (
		category.getId(), category.getCategoryName());
	}
	
	@Transactional
	public void create(CategoryRegisterForm categoryRegisterForm) {
		Category category = new Category();
		
		category.setCategoryName(categoryRegisterForm.getCategoryName());
		
		categoryRepository.save(category);
	}
	
	@Transactional
	public void update(CategoryEditForm categoryEditForm) {
		Category category = categoryRepository.getReferenceById(categoryEditForm.getId());
		
		category.setCategoryName(categoryEditForm.getCategoryName());
		
		categoryRepository.save(category);
	}
	
	@Transactional
	public void delete(Integer categoryId) {
		if (!categoryRepository.existsById(categoryId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "カテゴリが見つかりません");
		}
		
		List<Shop> shops = shopRepository.findByCategoryId(categoryId);
		for (Shop shop : shops) {
			Integer shopId = shop.getId();
			
			shopHolidayRepository.deleteByShopId(shopId);
	        reservationRepository.deleteByShopId(shopId);
	        favoriteRepository.deleteByShopId(shopId);
	        reviewRepository.deleteByShopId(shopId);			
		}
		
		shopRepository.deleteByCategoryId(categoryId);
		
		categoryRepository.deleteById(categoryId);
	}
}
