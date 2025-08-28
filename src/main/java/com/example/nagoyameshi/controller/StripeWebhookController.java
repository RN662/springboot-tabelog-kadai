package com.example.nagoyameshi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

@RestController
public class StripeWebhookController {
	@Value("${stripe.webhook-secret}")
	private String webhookSecret;

	private final UserRepository userRepository;

	public StripeWebhookController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@PostMapping("/stripe/webhook")
	public ResponseEntity<String> handleStripeWebhook(
			@RequestBody String payload,
			@RequestHeader("Stripe-Signature") String sigHeader) {
		
		System.out.println("[WEBHOOK] 受信しました");

		Event event;

		try {
			// Webhookの署名検証
			event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
		} catch (Exception e) {
			System.out.println("[WEBHOOK] 署名エラー: " + e.getMessage());
			return ResponseEntity.badRequest().body("Webhook error");
		}
		
		System.out.println("[WEBHOOK] イベント種類: " + event.getType());

		if ("checkout.session.completed".equals(event.getType())) {
			// 決済完了時：有料会員にする
			handlePaymentSuccess(event);
			
		} else if ("invoice.payment_succeeded".equals(event.getType())) {
			// 解約時：無料会員にする
			handleInvoiceSuccess(event);

		} else if ("customer.subscription.deleted".equals(event.getType())) {
			// 解約時：無料会員にする
			handleSubscriptionCancel(event);
		}
		
		System.out.println("[WEBHOOK] 処理完了");
		return ResponseEntity.ok("Success");
	}

    private void handleInvoiceSuccess(Event event) {
    	try {
			Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
			
			if (invoice != null) {
				String customerId = invoice.getCustomer();
				System.out.println("[WEBHOOK] Invoice success - Customer ID: " + customerId);
				
				User user = userRepository.findByStripeCustomerId(customerId);
				
				if (user != null) {
					user.setIsPaid(true);
					userRepository.save(user);
					System.out.println("[WEBHOOK] 請求成功でis_paid=trueに更新 - User ID: " + user.getId());
					
				} else {
					System.out.println("[WEBHOOK] ユーザーが見つかりません: " + customerId);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 決済完了時の処理
	private void handlePaymentSuccess(Event event) {
		System.out.println("[WEBHOOK] Checkout session completed 処理開始");
		
		try {
			Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
			if (session == null)  {
				System.out.println("[WEBHOOK] Session is null"); 
				return;
			}
			
			String customerId = session.getCustomer();
			String userId = null;
			
			if (session.getMetadata() != null) {
				userId = session.getMetadata().get("userId");
			}
			
			System.out.println("[WEBHOOK] Customer ID: " + customerId + ", User ID: " + userId);
				
			User user = null;
			
			// metadata（userId）で特定できればそれを優先
			if (userId != null) {
				try {
					user = userRepository.findById(Integer.parseInt(userId)).orElse(null);
				} catch (NumberFormatException ignore) {}
			}
			
			// それでも見つからなければ、cus_*** から逆引き
			if (user == null && customerId != null) {
				user = userRepository.findByStripeCustomerId(customerId);
			}
			
			if (user != null) {
				// 初回なら customerId を保存（既に入っていれば上書き不要）
				if (user.getStripeCustomerId() == null || user.getStripeCustomerId().isEmpty()) {
					user.setStripeCustomerId(customerId);
				}
				user.setIsPaid(true);
				userRepository.save(user);
				System.out.println("[WEBHOOK] Checkout成功でis_paid=trueに更新 - User ID: " + user.getId());
				
			} else {
				System.out.println("[WEBHOOK] ユーザーが見つかりません");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 決済完了時の処理
	private void handleSubscriptionCancel(Event event) {
		try {
			Subscription subscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);

			if (subscription != null) {
				String customerId = subscription.getCustomer();
				User user = userRepository.findByStripeCustomerId(customerId);

				if (user != null) {
					user.setIsPaid(false);
					userRepository.save(user);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

}
