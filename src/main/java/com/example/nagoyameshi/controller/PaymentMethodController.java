package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.PaymentMethodService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/payment-method")
public class PaymentMethodController {

	private final PaymentMethodService paymentMethodService;

	public PaymentMethodController(PaymentMethodService paymentMethodService) {
		this.paymentMethodService = paymentMethodService;
	}

	// Stripe顧客ポータルにリダイレクト
	@GetMapping("/edit")
	public String editPaymentMethod(@ModelAttribute("currentUser") User user,
			HttpServletRequest httpServletRequest, RedirectAttributes redirectAttributes) {
		
		try {
			String url = paymentMethodService.createPortalUrl(user, httpServletRequest);
			return "redirect:" + url;
			
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "支払い方法の管理画面を開けませんでした。");
			return "redirect:/";
		}

	}

}
