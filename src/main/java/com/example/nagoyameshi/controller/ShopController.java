package com.example.nagoyameshi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.ShopHoliday;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationInputForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopHolidayRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.ReviewService;

@Controller
@RequestMapping("/shops")
public class ShopController {
	private final ShopRepository shopRepository;
	private final CategoryRepository categoryRepository;
	private final ReviewRepository reviewRepository;
	private final ReviewService reviewService;
	private final FavoriteRepository favoriteRepository;
	private final FavoriteService favoriteService;
	private final ShopHolidayRepository shopHolidayRepository;
	private final ReservationService reservationService;

	public ShopController(ShopRepository shopRepository, CategoryRepository categoryRepository,
			ReviewRepository reviewRepository, ReviewService reviewService, FavoriteRepository favoriteRepository,
			FavoriteService favoriteService, ShopHolidayRepository shopHolidayRepository,
			ReservationService reservationService) {
		this.shopRepository = shopRepository;
		this.categoryRepository = categoryRepository;
		this.reviewRepository = reviewRepository;
		this.reviewService = reviewService;
		this.favoriteRepository = favoriteRepository;
		this.favoriteService = favoriteService;
		this.shopHolidayRepository = shopHolidayRepository;
		this.reservationService = reservationService;
	}

	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "categoryName", required = false) String categoryName,
			@RequestParam(name = "price", required = false) Integer price,
			@RequestParam(name = "order", required = false) String order,
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model) {
		Page<Shop> shopPage;

		if (keyword != null && !keyword.isEmpty()) {
			if (order != null && order.equals("priceAsc")) {
				shopPage = shopRepository.findByNameLikeOrAddressLikeOrCategory_CategoryNameLikeOrderByHighestPriceAsc(
						"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%", pageable);
			} else if (order != null && order.equals("priceDesc")) {
				shopPage = shopRepository.findByNameLikeOrAddressLikeOrCategory_CategoryNameLikeOrderByHighestPriceDesc(
						"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%", pageable);
			} else {
				shopPage = shopRepository.findByNameLikeOrAddressLikeOrCategory_CategoryNameLikeOrderByCreatedAtDesc(
						"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%", pageable);
			}

		} else if (categoryName != null && !categoryName.isEmpty()) {
			if (order != null && order.equals("priceAsc")) {
				shopPage = shopRepository.findByCategory_CategoryNameOrderByHighestPriceAsc(categoryName, pageable);
			} else if (order != null && order.equals("priceDesc")) {
				shopPage = shopRepository.findByCategory_CategoryNameOrderByHighestPriceDesc(categoryName, pageable);
			} else {
				shopPage = shopRepository.findByCategory_CategoryNameOrderByCreatedAtDesc(categoryName, pageable);
			}

		} else if (price != null) {
			if (order != null && order.equals("priceAsc")) {
				shopPage = shopRepository.findByHighestPriceLessThanEqualOrderByHighestPriceAsc(price, pageable);
			} else if (order != null && order.equals("priceDesc")) {
				shopPage = shopRepository.findByHighestPriceLessThanEqualOrderByHighestPriceDesc(price, pageable);
			} else {
				shopPage = shopRepository.findByHighestPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
			}

		} else {
			if (order != null && order.equals("priceAsc")) {
				shopPage = shopRepository.findAllByOrderByHighestPriceAsc(pageable);
			} else if (order != null && order.equals("priceDesc")) {
				shopPage = shopRepository.findAllByOrderByHighestPriceDesc(pageable);
			} else {
				shopPage = shopRepository.findAllByOrderByCreatedAtDesc(pageable);
			}
		}
		
		// 評価★用のデータ
		Map<Integer, Double> averageScores = new HashMap<>();
		Map<Integer, Long> totalReviewCounts = new HashMap<>();
		
		for (Shop shop : shopPage.getContent()) {
			double averageScore = reviewService.getAverageScore(shop);
			long totalReviewCount = reviewService.getReviewCountByShop(shop);
			
			averageScores.put(shop.getId(), averageScore);
			totalReviewCounts.put(shop.getId(), totalReviewCount);
		}
		
		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories);
		model.addAttribute("shopPage", shopPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryName", categoryName);
		model.addAttribute("price", price);
		model.addAttribute("order", order);
		model.addAttribute("averageScores", averageScores);
		model.addAttribute("totalReviewCounts", totalReviewCounts);

		return "shops/index";
	}

	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Shop shop = shopRepository.getReferenceById(id);
		ShopHoliday holiday = shopHolidayRepository.findByShop(shop);
		Favorite favorite = null;
		boolean hasUserAlreadyReviewed = false;
		boolean isFavorite = false;

		if (userDetailsImpl != null) {
			User user = userDetailsImpl.getUser();
			hasUserAlreadyReviewed = reviewService.hasUserAlreadyReviewed(shop, user);
			isFavorite = favoriteService.isFavorite(shop, user);

			if (isFavorite) {
				favorite = favoriteRepository.findByShopAndUser(shop, user);
			}
		}

		List<Review> newReviews = reviewRepository.findTop4ByShopOrderByCreatedAtDesc(shop);
		double averageScore = reviewService.getAverageScore(shop);
		long totalReviewCount = reviewService.getReviewCountByShop(shop);

		// 予約フォーム用のデータを追加
		List<Integer> holidays = reservationService.getHolidayDays(shop);
		List<String> timeSlots = reservationService.generateTimeSlots(shop);

		model.addAttribute("shop", shop);
		model.addAttribute("holiday", holiday);
		model.addAttribute("reservationInputForm", new ReservationInputForm());
		model.addAttribute("favorite", favorite);
		model.addAttribute("isFavorite", isFavorite);
		model.addAttribute("hasUserAlreadyReviewed", hasUserAlreadyReviewed);
		model.addAttribute("newReviews", newReviews);
		model.addAttribute("averageScore", averageScore);
		model.addAttribute("totalReviewCount", totalReviewCount);
	    model.addAttribute("holidays", holidays);
	    model.addAttribute("timeSlots", timeSlots);

		return "shops/show";
	}

}
