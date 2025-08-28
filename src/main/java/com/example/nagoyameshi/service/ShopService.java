package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.ShopHoliday;
import com.example.nagoyameshi.form.ShopEditForm;
import com.example.nagoyameshi.form.ShopRegisterForm;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopHolidayRepository;
import com.example.nagoyameshi.repository.ShopRepository;

@Service
public class ShopService {
	private final ShopRepository shopRepository;
	private final ShopHolidayRepository shopHolidayRepository;
	private final ReservationRepository reservationRepository;
	private final FavoriteRepository favoriteRepository;
	private final ReviewRepository reviewRepository;

	public ShopService(ShopRepository shopRepository, ShopHolidayRepository shopHolidayRepository, ReservationRepository reservationRepository, FavoriteRepository favoriteRepository, ReviewRepository reviewRepository) {
		this.shopRepository = shopRepository;
		this.shopHolidayRepository = shopHolidayRepository;
		this.reservationRepository = reservationRepository;
		this.favoriteRepository = favoriteRepository;
		this.reviewRepository = reviewRepository;
	}
	
	// コントローラから移動
	public ShopEditForm toShopEditForm(Shop shop) {
		ShopHoliday holiday = shopHolidayRepository.findByShop(shop);

		return new ShopEditForm(shop.getId(), shop.getCategory().getId(), shop.getName(), null,
				shop.getDescription(), shop.getLowestPrice(), shop.getHighestPrice(),
				shop.getOpeningTime().toString(), shop.getClosingTime().toString(), 
				// 定休日情報：存在する場合は実際の値、存在しない場合はfalse
				holiday != null ? holiday.isMonday() : false,
				holiday != null ? holiday.isTuesday() : false,
				holiday != null ? holiday.isWednesday() : false,
				holiday != null ? holiday.isThursday() : false,
				holiday != null ? holiday.isFriday() : false,
				holiday != null ? holiday.isSaturday() : false,
				holiday != null ? holiday.isSunday() : false,
				shop.getPostalCode(), shop.getAddress(), shop.getPhoneNumber(), shop.getSeatingCapacity());
	}

	@Transactional
	public void create(Category category, ShopRegisterForm shopRegisterForm) {
		Shop shop = new Shop();
		MultipartFile imageFile = shopRegisterForm.getImageFile();

		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			shop.setImageName(hashedImageName);
		}

		shop.setCategory(category);
		shop.setName(shopRegisterForm.getName());
		shop.setDescription(shopRegisterForm.getDescription());
		shop.setLowestPrice(shopRegisterForm.getLowestPrice());
		shop.setHighestPrice(shopRegisterForm.getHighestPrice());
		shop.setOpeningTime(LocalTime.parse(shopRegisterForm.getOpeningTime()));
		shop.setClosingTime(LocalTime.parse(shopRegisterForm.getClosingTime()));
		shop.setPostalCode(shopRegisterForm.getPostalCode());
		shop.setAddress(shopRegisterForm.getAddress());
		shop.setPhoneNumber(shopRegisterForm.getPhoneNumber());
		shop.setSeatingCapacity(shopRegisterForm.getSeatingCapacity());
		
		// 店舗を保存
		shopRepository.save(shop);
		
		// 定休日情報を1つのレコードとして作成・保存
		ShopHoliday shopHoliday = new ShopHoliday();
		shopHoliday.setShop(shop);
	    shopHoliday.setMonday(shopRegisterForm.isMonday());
	    shopHoliday.setTuesday(shopRegisterForm.isTuesday());
	    shopHoliday.setWednesday(shopRegisterForm.isWednesday());
	    shopHoliday.setThursday(shopRegisterForm.isThursday());
	    shopHoliday.setFriday(shopRegisterForm.isFriday());
	    shopHoliday.setSaturday(shopRegisterForm.isSaturday());
	    shopHoliday.setSunday(shopRegisterForm.isSunday());
	    shopHolidayRepository.save(shopHoliday);
	}

	@Transactional
	public void update(Category category, ShopEditForm shopEditForm) {
		Shop shop = shopRepository.getReferenceById(shopEditForm.getId());
		MultipartFile imageFile = shopEditForm.getImageFile();

		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			shop.setImageName(hashedImageName);
		}

		shop.setCategory(category);
		shop.setName(shopEditForm.getName());
		shop.setDescription(shopEditForm.getDescription());
		shop.setLowestPrice(shopEditForm.getLowestPrice());
		shop.setHighestPrice(shopEditForm.getHighestPrice());
		shop.setOpeningTime(LocalTime.parse(shopEditForm.getOpeningTime()));
		shop.setClosingTime(LocalTime.parse(shopEditForm.getClosingTime()));
		shop.setPostalCode(shopEditForm.getPostalCode());
		shop.setAddress(shopEditForm.getAddress());
		shop.setPhoneNumber(shopEditForm.getPhoneNumber());
		shop.setSeatingCapacity(shopEditForm.getSeatingCapacity());

		shopRepository.save(shop);
		
		// 既存の定休日レコードを探す
		ShopHoliday shopHoliday = shopHolidayRepository.findByShop(shop);

		// 定休日レコードが存在しない場合は新規作成
		if (shopHoliday == null) {
			shopHoliday = new ShopHoliday();
			shopHoliday.setShop(shop);
		}
		
	    shopHoliday.setMonday(shopEditForm.isMonday());
	    shopHoliday.setTuesday(shopEditForm.isTuesday());
	    shopHoliday.setWednesday(shopEditForm.isWednesday());
	    shopHoliday.setThursday(shopEditForm.isThursday());
	    shopHoliday.setFriday(shopEditForm.isFriday());
	    shopHoliday.setSaturday(shopEditForm.isSaturday());
	    shopHoliday.setSunday(shopEditForm.isSunday());
	    
	    shopHolidayRepository.save(shopHoliday);
	}
	
	@Transactional
	public void delete(Integer shopId) {
		if (!shopRepository.existsById(shopId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "店舗が見つかりません");
		}
		
		shopHolidayRepository.deleteByShopId(shopId);
		reservationRepository.deleteByShopId(shopId);
		favoriteRepository.deleteByShopId(shopId);
		reviewRepository.deleteByShopId(shopId);
		
		shopRepository.deleteById(shopId);
	}

	// UUIDを使って生成したファイル名を返す
	public String generateNewFileName(String fileName) {
		String[] fileNames = fileName.split("\\.");
		for (int i = 0; i < fileNames.length - 1; i++) {
			fileNames[i] = UUID.randomUUID().toString();
		}

		String hashedFileName = String.join(".", fileNames);
		return hashedFileName;
	}

	// 画像ファイルを指定したファイルにコピーする
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
