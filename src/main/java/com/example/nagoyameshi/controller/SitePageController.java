package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SitePageController {
	
	@GetMapping("/terms-of-service")
	public String termsOfService() {
		
		return "terms-of-service";
	}
	
	@GetMapping("/company")
	public String company() {
		
		return "company";
	}

}
