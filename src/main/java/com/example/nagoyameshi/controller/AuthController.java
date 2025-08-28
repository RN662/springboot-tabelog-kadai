package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.PasswordResetToken;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.PasswordResetEventPublisher;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.service.PasswordResetTokenService;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 */
@Controller
public class AuthController {
	private final UserService userService;
	private final SignupEventPublisher signupEventPublisher;
	private final VerificationTokenService verificationTokenService;
	private final PasswordResetEventPublisher passwordResetEventPublisher;
	private final PasswordResetTokenService passwordResetTokenService;

	public AuthController(UserService userService, SignupEventPublisher signupEventPublisher,
			VerificationTokenService verificationTokenService, PasswordResetEventPublisher passwordResetEventPublisher,
			PasswordResetTokenService passwordResetTokenService) {
		this.userService = userService;
		this.signupEventPublisher = signupEventPublisher;
		this.verificationTokenService = verificationTokenService;
		this.passwordResetEventPublisher = passwordResetEventPublisher;
		this.passwordResetTokenService = passwordResetTokenService;
	}

	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("signupForm", new SignupForm());
		return "auth/signup";
	}

	@PostMapping("/signup")
	public String signup(@ModelAttribute @Validated SignupForm signupForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {

		if (userService.isEmailRegistered(signupForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}

		if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
			bindingResult.addError(fieldError);
		}

		if (bindingResult.hasErrors()) {
			return "auth/signup";
		}

		User createdUser = userService.create(signupForm);
		String requestUrl = new String(httpServletRequest.getRequestURL());
		signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
		redirectAttributes.addFlashAttribute("successMessage",
				"ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。");

		return "redirect:/";
	}

	@GetMapping("/signup/verify")
	public String verify(@RequestParam(name = "token") String token, Model model) {
		VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);

		if (verificationToken != null) {
			User user = verificationToken.getUser();
			userService.enableUser(user);
			String successMessage = "会員登録が完了しました。";
			model.addAttribute("successMessage", successMessage);

		} else {
			String errorMessage = "トークンが無効です。";
			model.addAttribute("errorMessage", errorMessage);
		}

		return "auth/verify";
	}

	@GetMapping("/password/forgot")
	public String forgotPassword() {
		return "auth/forgot-password";
	}

	// パスワード再設定申請の処理
	@PostMapping("/password/forgot")
	public String forgotPassword(@RequestParam(name = "email") String email, RedirectAttributes redirectAttributes,
			HttpServletRequest httpServletRequest) {

		// メールアドレスが登録されているかチェック
		if (!userService.isEmailRegistered(email)) {
			redirectAttributes.addFlashAttribute("errorMessage", "入力されたメールアドレスは登録されていません。");
			
			return "redirect:/password/forgot";
		}

		// ユーザーを取得してパスワードリセットイベントを発行
		User user = userService.findByEmail(email);
		String requestUrl = new String(httpServletRequest.getRequestURL());
		passwordResetEventPublisher.publishPasswordResetEvent(user, requestUrl);

		redirectAttributes.addFlashAttribute("successMessage", "パスワード再設定用のメールを送信しました。");
		
		return "redirect:/";
	}
	
	// パスワード変更画面を表示
	/**
	 * 
	 * @param token 
	 * @param model
	 * @return
	 */
	@GetMapping("/password/reset")
	public String resetPassword(@RequestParam(name = "token") String token, Model model) {
		PasswordResetToken resetToken = passwordResetTokenService.getValidToken(token);
		
		if (resetToken != null) {
			model.addAttribute("token", token);
			
			return "auth/reset-password";
			
		} else {
			model.addAttribute("errorMessage", "無効なリンクまたは期限切れです。");
			
			return "auth/error";
		}
	}

	// パスワード変更の処理
	@PostMapping("/password/reset")
	public String resetPassword(@RequestParam(name = "token") String token,
			@RequestParam(name = "password") String password,
			@RequestParam(name = "passwordConfirmation") String passwordConfirmation, RedirectAttributes redirectAttributes,
			Model model) {
		
		// トークン確認
		PasswordResetToken resetToken = passwordResetTokenService.getValidToken(token);
		if (resetToken == null) {
			model.addAttribute("errorMessage", "無効なリンクまたは期限切れです。");
			
			return "auth/error";
		}
		
		// パスワード確認
		if (!userService.isSamePassword(password, passwordConfirmation)) {
			model.addAttribute("token", token);
			model.addAttribute("errorMessage", "パスワードが一致しません。");
			
			return "auth/reset-password";
		}
		
		// パスワード更新
		User user = resetToken.getUser();
		userService.updatePassword(user, password);
		passwordResetTokenService.deleteToken(resetToken);
		
		redirectAttributes.addFlashAttribute("successMessage", "パスワードが変更されました。");
		
		return "redirect:/login";
	}

}
