package com.example.nagoyameshi.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.PasswordResetToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetTokenService {
	private PasswordResetTokenRepository passwordResetTokenRepository;
	
	public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
		this.passwordResetTokenRepository = passwordResetTokenRepository;
	}
	
	// 古いトークンを削除してから新しいトークンを作成
	@Transactional
	public PasswordResetToken create(User user, String token) {
		passwordResetTokenRepository.deleteByUser(user);
		
		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setUser(user);
		resetToken.setToken(token);
		resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		
		return passwordResetTokenRepository.save(resetToken);
	}
	
	// 有効なトークンかチェック（期限切れでないか）
	public PasswordResetToken getValidToken(String token) {
		PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
		
		if (passwordResetToken == null) {
			return null;  // トークンが見つからない
		}
		
		if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			return null;  // 期限切れ
		}
		
		return passwordResetToken;
	}
	
	@Transactional
	public void deleteToken(PasswordResetToken token) {
		passwordResetTokenRepository.delete(token);
	}

}
