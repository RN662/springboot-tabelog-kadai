package com.example.nagoyameshi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
	public Page<Reservation> findByUserOrderByReservedAtDesc(User user, Pageable pageable);
	
	public void deleteByShopId(Integer shopId);
	
	public List<Reservation> findByShopAndReservedAtBetween(Shop shop, LocalDateTime startTime, LocalDateTime endTime);
}
