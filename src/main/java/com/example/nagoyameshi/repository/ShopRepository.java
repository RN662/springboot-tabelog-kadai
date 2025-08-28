package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Integer> {
	public Page<Shop> findByNameLike(String keyword, Pageable pageable);

	public Page<Shop> findByNameLikeOrAddressLikeOrCategory_CategoryNameLikeOrderByCreatedAtDesc(String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable);
	public Page<Shop> findByNameLikeOrAddressLikeOrCategory_CategoryNameLikeOrderByHighestPriceAsc(String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable);
	public Page<Shop> findByNameLikeOrAddressLikeOrCategory_CategoryNameLikeOrderByHighestPriceDesc(String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable);
	
	public Page<Shop> findByCategory_CategoryNameOrderByCreatedAtDesc(String categoryName, Pageable pageable);
	public Page<Shop> findByCategory_CategoryNameOrderByHighestPriceAsc(String categoryName, Pageable pageable);
	public Page<Shop> findByCategory_CategoryNameOrderByHighestPriceDesc(String categoryName, Pageable pageable);
	
	public Page<Shop> findByHighestPriceLessThanEqualOrderByCreatedAtDesc(Integer price, Pageable pageable);
	public Page<Shop> findByHighestPriceLessThanEqualOrderByHighestPriceAsc(Integer price, Pageable pageable);
	public Page<Shop> findByHighestPriceLessThanEqualOrderByHighestPriceDesc(Integer price, Pageable pageable);
	
	public Page<Shop> findAllByOrderByCreatedAtDesc(Pageable pageable);
	public Page<Shop> findAllByOrderByHighestPriceAsc(Pageable pageable);
	public Page<Shop> findAllByOrderByHighestPriceDesc(Pageable pageable);
	
	public List<Shop> findTop6ByOrderByCreatedAtDesc();
	
	public List<Shop> findByCategoryId(Integer categoryId);
	
	public void deleteByCategoryId(Integer categoryId); 
}
