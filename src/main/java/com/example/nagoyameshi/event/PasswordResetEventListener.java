package com.example.nagoyameshi.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.PasswordResetTokenService;

@Component
public class PasswordResetEventListener {
	private final PasswordResetTokenService passwordResetTokenService;
	private final JavaMailSender javaMailSender;
	
	public PasswordResetEventListener(PasswordResetTokenService passwordResetTokenService, JavaMailSender mailSender) {
		this.passwordResetTokenService = passwordResetTokenService;
		this.javaMailSender = mailSender;
	}
	
	@EventListener
	private void onPasswordResetEvent(PasswordResetEvent passwordResetEvent) {
		User user = passwordResetEvent.getUser();
		String token = UUID.randomUUID().toString();
		passwordResetTokenService.create(user, token);
		
		String senderAddress = "rrr.coppe62@gmail.com";
		String recipientAddress = user.getEmail();
		String subject = "[NAGOYAMESHI] パスワード再設定のご案内";
		
		String requestUrl = passwordResetEvent.getRequestUrl();
		String baseUrl = requestUrl.replace("/password/forgot", "");
		String confirmationUrl = baseUrl + "/password/reset?token=" + token;
		String termsUrl = baseUrl + "/terms-of-service";
		
		String message =  """
				%s 様

				パスワード再設定のご依頼を受け付けました。
				下記リンクより新しいパスワードを設定してください。

				▼パスワード再設定リンク
				%s

				※本リンクには有効期限があります。期限切れの場合は、再度お手続きをお願いします。
				※このメールに心当たりがない場合は、第三者による誤操作の可能性があります。リンクはクリックせず破棄してください。

				セキュリティのため、他サービスと同じパスワードの使い回しはお控えください。

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
