package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.ShopHoliday;

public interface ShopHolidayRepository extends JpaRepository<ShopHoliday, Integer> {
	public ShopHoliday findByShop(Shop shop);
	
	public void deleteByShopId(Integer shopId);	
}
