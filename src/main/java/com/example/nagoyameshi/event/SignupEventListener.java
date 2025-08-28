package com.example.nagoyameshi.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.VerificationTokenService;

@Component
public class SignupEventListener {
	private final VerificationTokenService verificationTokenService;
	private final JavaMailSender javaMailSender;
	
	public SignupEventListener(VerificationTokenService verificationTokenService, JavaMailSender mailSender) {
		this.verificationTokenService = verificationTokenService;
		this.javaMailSender = mailSender;
	}
	
	@EventListener
	private void onSignupEvent(SignupEvent signupEvent) {
		User user = signupEvent.getUser();
		String token = UUID.randomUUID().toString();
		verificationTokenService.create(user, token);
		
		String senderAddress = "rrr.coppe62@gmail.com";
		String recipientAddress = user.getEmail();
		String subject = "[NAGOYAMESHI] メールアドレス確認のお願い";
		
		String requestUrl = signupEvent.getRequestUrl();
		String confirmationUrl = requestUrl + "/verify?token=" + token;
		
		String baseUrl = requestUrl.replaceAll("/signup$", "");
		String termsUrl = baseUrl + "/terms-of-service";
		
		String message = """
				%s 様

				この度は NAGOYAMESHI にご登録いただきありがとうございます。
				下記リンクをクリックして、メールアドレスの確認と会員登録を完了してください。

				▼メール確認リンク
				%s

				※このメールに心当たりがない場合は、リンクをクリックせず破棄してください。

				――
				NAGOYAMESHI株式会社
				〒101-0022 東京都千代田区神田練塀町300番地 住友不動産秋葉原駅前ビル5F
				本メールは送信専用です。ご返信には対応しておりません。
				利用規約: %s
				(c) NAGOYAMESHI Inc. All rights reserved.
				""".formatted(user.getName(), confirmationUrl, termsUrl);
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(senderAddress);
		mailMessage.setTo(recipientAddress);
		mailMessage.setSubject(subject);
		mailMessage.setText(message);
		javaMailSender.send(mailMessage);
	}

}
