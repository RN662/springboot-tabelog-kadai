package com.example.nagoyameshi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.cloudinary.Cloudinary;

@Configuration
@Profile("production")
@ConditionalOnProperty(name = "CLOUDINARY_URL")
public class CloudinaryConfig {
	
	@Bean
	public Cloudinary cloudinary(@Value("${CLOUDINARY_URL}") String url) {
		return new Cloudinary(url);
	}
}
