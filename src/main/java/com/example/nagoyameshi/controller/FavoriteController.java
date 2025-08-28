package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;

@Controller
public class FavoriteController {
	private final FavoriteRepository favoriteRepository;
	private final ShopRepository shopRepository;
	private final FavoriteService favoriteService;
	
	public FavoriteController(FavoriteRepository favoriteRepository, ShopRepository shopRepository, FavoriteService favoriteService) {
		this.favoriteRepository = favoriteRepository;
		this.shopRepository = shopRepository;
		this.favoriteService = favoriteService;
	}
	
	@GetMapping("/favorites")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable, Model model) {
		User user = userDetailsImpl.getUser();
		Page<Favorite> favoritePage = favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable);
		
		model.addAttribute("favoritePage", favoritePage);
		
		return "favorites/index";
	}
	
	@PostMapping("/shops/{shopId}/favorites/create")
	public String create(@PathVariable(name = "shopId") Integer shopId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			             RedirectAttributes redirectAttributes, Model model) 
	{
		Shop shop = shopRepository.getReferenceById(shopId);
		User user = userDetailsImpl.getUser();
		
		favoriteService.create(shop, user);
		redirectAttributes.addFlashAttribute("successMessage", "お気に入りに追加しました。");
		
		return "redirect:/shops/{shopId}";
	}
	
	@PostMapping("/shops/{shopId}/favorites/delete")
	public String delete(@PathVariable(name = "shopId") Integer shopId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes) {
		try {
			User user = userDetailsImpl.getUser();
			favoriteService.delete(shopId, user);
			redirectAttributes.addFlashAttribute("successMessage", "お気に入りを解除しました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お気に入りの解除に失敗しました。");
		}
		
		return "redirect:/shops/{shopId}";
	}

}
