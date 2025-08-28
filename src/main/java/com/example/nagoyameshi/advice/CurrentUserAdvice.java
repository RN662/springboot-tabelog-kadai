package com.example.nagoyameshi.advice;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;

@ControllerAdvice
@Component
public class CurrentUserAdvice {
	private final UserRepository userRepository;
	
	public CurrentUserAdvice(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	// 未ログインなら null、ログイン中なら DB から最新のユーザーを返す
	@ModelAttribute("currentUser")
	@Transactional(readOnly = true)
	public User addCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		if (userDetailsImpl == null) return null;
		Integer userId = userDetailsImpl.getUser().getId();
		
		User latestUser = userRepository.findById(userId).orElse(null);
		
		return latestUser;	
	}
}
