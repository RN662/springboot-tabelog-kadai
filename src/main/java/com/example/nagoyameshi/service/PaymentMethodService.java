package com.example.nagoyameshi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.nagoyameshi.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PaymentMethodService {
	
	@Value("${stripe.api-key}")
	private String stripeApiKey;
	
	// Stripe顧客ポータルのURLを作成
	public String createPortalUrl(User user, HttpServletRequest httpServletRequest) throws StripeException {
		Stripe.apiKey = stripeApiKey;
		
		String returnUrl = ServletUriComponentsBuilder.fromRequestUri(httpServletRequest)
				.replacePath("/")
				.build()
				.toUriString();
		
		String customerId = user.getStripeCustomerId();
		if (customerId == null || customerId.isBlank()) {
			throw new IllegalStateException("Stripeの顧客IDがありません。初回登録後にご利用ください。");	
		}
		
		com.stripe.param.billingportal.SessionCreateParams params =
				com.stripe.param.billingportal.SessionCreateParams.builder()
				      .setCustomer(customerId)
				      .setReturnUrl(returnUrl)
				      .build();
		
		com.stripe.model.billingportal.Session session =
				com.stripe.model.billingportal.Session.create(params);
		
		return session.getUrl();
	}
	

}
