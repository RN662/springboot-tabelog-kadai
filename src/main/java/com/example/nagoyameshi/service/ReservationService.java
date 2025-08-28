package com.example.nagoyameshi.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.ShopHoliday;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationInputForm;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.ShopHolidayRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;
	private final ShopHolidayRepository shopHolidayRepository;
	private final ShopRepository shopRepository;
	private final UserRepository userRepository;
	
	public ReservationService(ReservationRepository reservationRepository, ShopHolidayRepository shopHolidayRepository, ShopRepository shopRepository, UserRepository userRepository) {
		this.reservationRepository = reservationRepository;
		this.shopHolidayRepository = shopHolidayRepository;
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
	}
	
	@Transactional
	public void input(ReservationInputForm form, Shop shop, User user) {
		LocalDate date = form.getReservationLocalDate();
		LocalTime time = form.getReservationLocalTime();
		LocalDateTime dateTime = LocalDateTime.of(date, time);
		
		
		// 予約日時が現在より未来か
		if (dateTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("過去の日時は予約できません。");
		}
		
		// 営業時間内か
		LocalTime opening = shop.getOpeningTime();
		LocalTime closing = shop.getClosingTime();
		if (time.isBefore(opening) || time.isAfter(closing)) {
			throw new IllegalArgumentException("営業時間外は予約できません。");
		}
		
		// 定休日チェック
		if (isHoliday(date, shop)) {
			throw new IllegalArgumentException("定休日は予約できません。");
		}
		
		// 座席数チェック
		LocalDateTime startTime = dateTime.withMinute(0).withSecond(0);
		LocalDateTime endTime = startTime.plusHours(1);
		
		int alreadyReserved = countReservedPeople(shop, startTime, endTime);
		int capacity = shop.getSeatingCapacity();
		
		if (alreadyReserved + form.getNumberOfPeople() > capacity) {
			throw new IllegalArgumentException("満席のため予約できません。");
		}
		
		Reservation reservation = new Reservation();
		reservation.setShop(shop);
		reservation.setUser(user);
		reservation.setReservedAt(dateTime);
		reservation.setNumberOfPeople(form.getNumberOfPeople());
	}
	
	
	// 指定した日が定休日かどうか
	private boolean isHoliday(LocalDate date, Shop shop) {
		ShopHoliday shopHoliday = shopHolidayRepository.findByShop(shop);
		
		// 定休日設定がない場合は定休日ではない
		if (shopHoliday == null) {
			return false;
		}
		
		// 予約日の曜日を取得
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		
		switch (dayOfWeek) {
		case MONDAY:
		    return shopHoliday.isMonday();
		case TUESDAY:
			return shopHoliday.isTuesday();
		case WEDNESDAY:
			return shopHoliday.isWednesday();
		case THURSDAY:
			return shopHoliday.isThursday();
		case FRIDAY:
			return shopHoliday.isFriday();
		case SATURDAY:
			return shopHoliday.isSaturday();
		case SUNDAY:
			return shopHoliday.isSunday();
		default:
			return false;
		}
	}
	
	//指定した時間帯の予約人数を数える
	private int countReservedPeople(Shop shop, LocalDateTime startTime, LocalDateTime endTime) {
		// その時間帯の全ての予約を取得
		List<Reservation> reservations = reservationRepository.findByShopAndReservedAtBetween(shop, startTime, endTime);
		
		// 予約人数の合計を計算
		int total = 0;
		for (Reservation reservation : reservations) {
			total += reservation.getNumberOfPeople();
		}
		
		return total;		
	}
	
	// コントローラから移動
	public ReservationRegisterForm toreseRegisterForm(Shop shop, User user, ReservationInputForm reservationInputForm) {
		LocalDate reservationDate = reservationInputForm.getReservationLocalDate();
		LocalTime reservationTime = reservationInputForm.getReservationLocalTime();
		Integer numberOfPeople = reservationInputForm.getNumberOfPeople();
		
		return  new ReservationRegisterForm(
				shop.getId(), user.getId(), reservationDate.toString(), reservationTime.toString(), numberOfPeople);
	}
	
	@Transactional
	public void create(ReservationRegisterForm reservationRegisterForm) {
		Shop shop = shopRepository.getReferenceById(reservationRegisterForm.getShopId());
		User user = userRepository.getReferenceById(reservationRegisterForm.getUserId());
		
		LocalDate date = LocalDate.parse(reservationRegisterForm.getReservationDate());
		LocalTime time = LocalTime.parse(reservationRegisterForm.getReservationTime());
		LocalDateTime dateTime = LocalDateTime.of(date, time);
		
		Reservation reservation = new Reservation();
		reservation.setShop(shop);
		reservation.setUser(user);
		reservation.setReservedAt(dateTime);
		reservation.setNumberOfPeople(reservationRegisterForm.getNumberOfPeople());
		
		reservationRepository.save(reservation);
	}
	
	@Transactional
	public void delete(Integer reservationId, User user) {
		Integer userId = user.getId();
		
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "予約が見つかりません"));
		
		if (!reservation.getUser().getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "他のユーザーの予約は削除できません");
		}
		
		reservationRepository.delete(reservation);
	}
	
	
	// 店舗の定休日を取得する
	public List<Integer> getHolidayDays(Shop shop) {
		List<Integer> holidays = new ArrayList<>();
		ShopHoliday shopHoliday = shopHolidayRepository.findByShop(shop);
		
		if (shopHoliday == null) {
			return holidays;
		}
		
		// 各曜日をチェック（Flatpickrの曜日番号に合わせる）
	    if (shopHoliday.isSunday()) holidays.add(0);
	    if (shopHoliday.isMonday()) holidays.add(1);
	    if (shopHoliday.isTuesday()) holidays.add(2);
	    if (shopHoliday.isWednesday()) holidays.add(3);
	    if (shopHoliday.isThursday()) holidays.add(4);
	    if (shopHoliday.isFriday()) holidays.add(5);
	    if (shopHoliday.isSaturday()) holidays.add(6);
	    
	    return holidays;
	}
	
	// 店舗の営業時間を30分刻みの時間帯リストで取得する
	public List<String> generateTimeSlots(Shop shop) {
		List<String> timeSlots = new ArrayList<>();
	    LocalTime openingTime = shop.getOpeningTime();
	    LocalTime closingTime = shop.getClosingTime();
	    
	    // 閉店時間の60分前を最終予約時間とする
	    LocalTime lastReservationTime = closingTime.minusHours(1);
	    
	    LocalTime currentTime = openingTime;
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
	    
	    while (currentTime.isBefore(lastReservationTime) || currentTime.equals(lastReservationTime)) {
	    	timeSlots.add(currentTime.format(formatter));
	    	currentTime = currentTime.plusMinutes(30); // 30分刻み
	    }
	    
	    return timeSlots;
	}
}
