package com.example.nagoyameshi.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@Profile("production")
@RequiredArgsConstructor
public class CloudinaryService {
	private final Cloudinary cloudinary;
	
	public String upload(MultipartFile file) throws IOException {
		String id = UUID.randomUUID().toString();
		Map<?, ?> uploadResult = cloudinary.uploader().upload
				(file.getBytes(),
				ObjectUtils.asMap("folder","storage","public_id",id,"resource_type","image"));
		
		return (String) uploadResult.get("secure_url");
	}
}
