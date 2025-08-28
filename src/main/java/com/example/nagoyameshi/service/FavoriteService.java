package com.example.nagoyameshi.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.FavoriteRepository;

@Service
public class FavoriteService {
	private final FavoriteRepository favoriteRepository;
	
	public FavoriteService(FavoriteRepository favoriteRepository) {
		this.favoriteRepository = favoriteRepository;
	}
	
	@Transactional
	public void create(Shop shop, User user) {
		Favorite favorite = new Favorite();
		
		favorite.setShop(shop);
		favorite.setUser(user);
		
		favoriteRepository.save(favorite);
	}
	
	public boolean isFavorite(Shop shop, User user) {
		return favoriteRepository.findByShopAndUser(shop, user) != null;
	}
	
	@Transactional
	public void delete(Integer shopId, User user) {
		Integer userId = user.getId();
		Favorite favorite = favoriteRepository.findByShopIdAndUserId(shopId, userId)
		    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "お気に入りが見つかりません"));
		    
		favoriteRepository.delete(favorite);
	}

}
