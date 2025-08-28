package com.example.nagoyameshi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class StripeSubscriptionService {
	@Value("${stripe.api-key}")
	private String stripeApiKey;

	private static final String PRICE_ID = "price_1Ry4zEFpitW9zCwEKEvFd4rp";

	public String createStripeSession(User user, HttpServletRequest httpServletRequest) {
		Stripe.apiKey = stripeApiKey;

		String domain = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + ":"
				+ httpServletRequest.getServerPort();

		SessionCreateParams.Builder builder = SessionCreateParams.builder()
				.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
				.addLineItem(
						SessionCreateParams.LineItem.builder()
								.setQuantity(1L)
								.setPrice(PRICE_ID)
								.build())
				.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
				.setSuccessUrl(domain + "/subscription/success?session_id={CHECKOUT_SESSION_ID}")
				.setCancelUrl(domain + "/subscription/cancel")
				.setClientReferenceId(user.getId().toString()) //要追加（ユーザー特定の保険）

				.putMetadata("userId", user.getId().toString())
				.putMetadata("userName", user.getName());
		
	    // 既存の Customer を再利用、なければ新規作成をCheckoutに任せる
		if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isBlank()) {
			builder.setCustomer(user.getStripeCustomerId());
			
		}
		
		SessionCreateParams params = builder.build();

		try {
			Session session = Session.create(params);
			return session.getId();
			
		} catch (StripeException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean cancelSubscription(String customerId) {
		Stripe.apiKey = stripeApiKey;

		try {
			// 顧客のサブスクリプション一覧を取得
			SubscriptionCollection subscriptions = Subscription.list(
				SubscriptionListParams.builder()	
					.setCustomer(customerId)
					.setStatus(SubscriptionListParams.Status.ACTIVE)
					.build()
			);

			// アクティブなサブスクリプションをキャンセル
			for (Subscription subscription : subscriptions.getData()) {
				subscription.cancel();
			}
			
			// 顧客の支払い方法を削除
			PaymentMethodCollection paymentMethods = PaymentMethod.list(
				PaymentMethodListParams.builder()
				    .setCustomer(customerId)
				    .setType(PaymentMethodListParams.Type.CARD)
				    .build()
			);
			
			for (PaymentMethod paymentMethod : paymentMethods.getData()) {
				paymentMethod.detach();
			}
			
			return true;

		} catch (StripeException e) {
			e.printStackTrace();
			return false;
		}
	}

}
