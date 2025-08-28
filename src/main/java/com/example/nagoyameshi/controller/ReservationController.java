package com.example.nagoyameshi.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.ShopHoliday;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationInputForm;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopHolidayRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.ReviewService;

@Controller
public class ReservationController {
	private final ReservationRepository reservationRepository;
	private final ShopRepository shopRepository;
	private final ShopHolidayRepository shopHolidayRepository;
	private final ReservationService reservationService;
	private final ReviewRepository reviewRepository;
	private final ReviewService reviewService;
	private final FavoriteRepository favoriteRepository;
	private final FavoriteService favoriteService;

	public ReservationController(ReservationRepository reservationRepository, ShopRepository shopRepository,
			ReservationService reservationService, ShopHolidayRepository shopHolidayRepository,
			ReviewRepository reviewRepository, ReviewService reviewService, FavoriteRepository favoriteRepository,
			FavoriteService favoriteService) {
		this.reservationRepository = reservationRepository;
		this.shopRepository = shopRepository;
		this.reservationService = reservationService;
		this.shopHolidayRepository = shopHolidayRepository;
		this.reviewRepository = reviewRepository;
		this.reviewService = reviewService;
		this.favoriteRepository = favoriteRepository;
		this.favoriteService = favoriteService;
	}

	@GetMapping("/reservations")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@PageableDefault(page = 0, size = 20, sort = "id", direction = Direction.DESC) Pageable pageable,
			Model model) {
		User user = userDetailsImpl.getUser();
		Page<Reservation> reservationPage = reservationRepository.findByUserOrderByReservedAtDesc(user, pageable);

		LocalDate today = LocalDate.now();

		model.addAttribute("reservationPage", reservationPage);
		model.addAttribute("today", today);

		return "reservations/index";
	}

	@GetMapping("/shops/{id}/reservations/input")
	public String input(@PathVariable(name = "id") Integer id,
			@ModelAttribute @Validated ReservationInputForm reservationInputForm, BindingResult bindingResult,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes,
			Model model) {

		Shop shop = shopRepository.getReferenceById(id);

		if (bindingResult.hasErrors()) {
			List<Integer> holidays = reservationService.getHolidayDays(shop);
			List<String> timeSlots = reservationService.generateTimeSlots(shop);
			ShopHoliday holiday = shopHolidayRepository.findByShop(shop);
			List<Review> newReviews = reviewRepository.findTop4ByShopOrderByCreatedAtDesc(shop);
			long totalReviewCount = reviewRepository.countByShop(shop);
			double averageScore = reviewService.getAverageScore(shop);

			boolean hasUserAlreadyReviewed = false;
			boolean isFavorite = false;
			Favorite favorite = null;

			if (userDetailsImpl != null) {
				User user = userDetailsImpl.getUser();
				hasUserAlreadyReviewed = reviewService.hasUserAlreadyReviewed(shop, user);
				isFavorite = favoriteService.isFavorite(shop, user);
				if (isFavorite) {
					favorite = favoriteRepository.findByShopAndUser(shop, user);
				}
			}

			model.addAttribute("shop", shop);
	        model.addAttribute("holiday", holiday);
	        model.addAttribute("holidays", holidays);
	        model.addAttribute("timeSlots", timeSlots);
	        model.addAttribute("newReviews", newReviews);
	        model.addAttribute("totalReviewCount", totalReviewCount);
	        model.addAttribute("averageScore", averageScore);
	        model.addAttribute("hasUserAlreadyReviewed", hasUserAlreadyReviewed);
	        model.addAttribute("isFavorite", isFavorite);
	        model.addAttribute("favorite", favorite);
			model.addAttribute("errorMessage", "予約内容に不備があります。");
			
			return "shops/show";
		}

		redirectAttributes.addFlashAttribute("reservationInputForm", reservationInputForm);

		return "redirect:/shops/{id}/reservations/confirm";
	}

	@GetMapping("/shops/{id}/reservations/confirm")
	public String confirm(@PathVariable(name = "id") Integer id,
			@ModelAttribute ReservationInputForm reservationInputForm,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes,
			Model model) {

		Shop shop = shopRepository.getReferenceById(id);
		User user = userDetailsImpl.getUser();

		try {
			reservationService.input(reservationInputForm, shop, user);
			ReservationRegisterForm reservationRegisterForm = reservationService.toreseRegisterForm(shop, user,
					reservationInputForm);

			model.addAttribute("shop", shop);
			model.addAttribute("reservationRegisterForm", reservationRegisterForm);

			return "reservations/confirm";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("reservationInputForm", reservationInputForm);
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

			return "redirect:/shops/" + id;
		}
	}

	@PostMapping("/shops/{id}/reservations/create")
	public String create(@ModelAttribute ReservationRegisterForm reservationRegisterForm,
			RedirectAttributes redirectAttributes) {

		try {
			reservationService.create(reservationRegisterForm);
			return "redirect:/reservations?reserved";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "予約の作成に失敗しました。");
			return "redirect:/shops/" + reservationRegisterForm.getShopId();
		}
	}

	@PostMapping("/reservations/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id,
			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes) {

		try {
			User user = userDetailsImpl.getUser();
			reservationService.delete(id, user);
			redirectAttributes.addFlashAttribute("successMessage", "予約をキャンセルしました。");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "予約のキャンセルに失敗しました。");
		}

		return "redirect:/reservations";
	}
}
