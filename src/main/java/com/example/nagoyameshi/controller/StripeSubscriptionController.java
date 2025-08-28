package com.example.nagoyameshi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeSubscriptionService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/subscription")
public class StripeSubscriptionController {
	private final StripeSubscriptionService stripeSubscriptionService;
	private final UserRepository userRepository;

	public StripeSubscriptionController(StripeSubscriptionService stripeSubscriptionService,
			UserRepository userRepository) {
		this.stripeSubscriptionService = stripeSubscriptionService;
		this.userRepository = userRepository;
	}

	@Value("${stripe.public-key}")
	private String stripePublicKey;

	@GetMapping("/register")
	public String showSubscriptionPage(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			HttpServletRequest httpServletRequest, Model model) {
		User user = userDetailsImpl.getUser();

		String sessionId = stripeSubscriptionService.createStripeSession(user, httpServletRequest);

		if (sessionId != null) {
			model.addAttribute("sessionId", sessionId);
			model.addAttribute("stripePublicKey", stripePublicKey);
			model.addAttribute("user", user);

		} else {
			model.addAttribute("errorMessage", "セッション作成に失敗しました。");
		}

		return "subscription/register";
	}

	@GetMapping("/success")
	public String subscriptionSuccess(@RequestParam(name = "session_id") String sessionId,
			@AuthenticationPrincipal UserDetailsImpl principal, HttpServletRequest httpServletRequest,
			RedirectAttributes redirectAttributes) {
		
		redirectAttributes.addFlashAttribute("successMessage", "有料プラン登録が完了しました。");

		return "redirect:/";
	}

	@GetMapping("/cancel")
	public String showCancelPage(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userDetailsImpl.getUser();

		User latestUser = userRepository.findById(user.getId()).orElse(user);

		model.addAttribute("user", latestUser);

		return "subscription/cancel";
	}

	@PostMapping("/cancel")
	public String cancelSubscription(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		String customerId = user.getStripeCustomerId();

		boolean success = stripeSubscriptionService.cancelSubscription(customerId);

		if (success) {
			user.setIsPaid(false);
			userRepository.save(user);

			redirectAttributes.addFlashAttribute("successMessage", "有料プランを解約しました。");

			return "redirect:/";

		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "解約処理に失敗しました。");

			return "redirect:/subscription/cancel";
		}
	}
}
