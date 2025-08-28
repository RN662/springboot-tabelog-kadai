package com.example.nagoyameshi.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
	public Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
	
	public Favorite findByShopAndUser(Shop shop, User user);
	
	public void deleteByShopId(Integer shopId);
	
	Optional<Favorite> findByShopIdAndUserId(Integer shopId, Integer userId);
}
