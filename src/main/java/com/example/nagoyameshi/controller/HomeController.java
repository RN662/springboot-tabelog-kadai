package com.example.nagoyameshi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.service.ReviewService;

@Controller
public class HomeController {
	private final ShopRepository shopRepository;
	private final ReviewService reviewService;
	private final CategoryRepository categoryRepository;
	
	public HomeController(ShopRepository shopRepository, ReviewService reviewService, CategoryRepository categoryRepository) {
		this.shopRepository = shopRepository;
		this.reviewService = reviewService;
		this.categoryRepository = categoryRepository;
	}
	
    @GetMapping("/")
    public String index(Model model) {
    	// 新規掲載店
    	List<Shop> newShops = shopRepository.findTop6ByOrderByCreatedAtDesc();
    	
    	// 評価が高いお店
    	List<Shop> allShops = shopRepository.findAll();
    	
    	Map<Shop, Double> scoreCache = new HashMap<>();
    	for (Shop shop : allShops) {
    		double score = reviewService.getAverageScore(shop);
    		scoreCache.put(shop, score);
    	}
    	
    	allShops.sort((shop1, shop2) -> {
    		double score1 = scoreCache.get(shop1);
    		double score2 = scoreCache.get(shop2);
    		return Double.compare(score2, score1);
    	});
    	
    	List<Shop> topRatedShops = allShops.subList(0, Math.min(6, allShops.size()));
    	
    	Map<Integer, Double> topRatedAverageScores = new HashMap<>();
    	for (Shop shop : topRatedShops) {
    		double averageScore = reviewService.getAverageScore(shop);
    		topRatedAverageScores.put(shop.getId(), averageScore);
    	}
    	
    	List<Category> allCategories =categoryRepository.findAll();
    	
        model.addAttribute("newShops", newShops);
        model.addAttribute("topRatedShops", topRatedShops);
        model.addAttribute("topRatedAverageScores", topRatedAverageScores);
        model.addAttribute("categories", allCategories);
    	
        return "index";
    } 
    
}
