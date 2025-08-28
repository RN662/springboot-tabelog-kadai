package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.PasswordResetToken;
import com.example.nagoyameshi.entity.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
	public PasswordResetToken findByToken(String token);
	
	public List<PasswordResetToken> findByUser(User user);

	public void deleteByUser(User user);
}
