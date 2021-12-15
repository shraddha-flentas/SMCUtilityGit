package com.flentas.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.flentas.service.ImageService;

@RestController
@RequestMapping("/api")
public class ImageController {

	private static Logger logger=LoggerFactory.getLogger(ImageController.class);
	
	@Autowired
	private ImageService imageService;
	
	
	@CrossOrigin(origins="*")
	@PostMapping("/compress")
	public ResponseEntity<Object> uploadFiles(@RequestParam("file") MultipartFile file,@RequestHeader String authorization) {
		try {
			
			  List<String> fileNames = new ArrayList<>();
			  logger.info("Request received successfully for Image file"+file);
			  return imageService.saveCompressedImageByQuality(file,authorization);
				
		} catch (Exception e) {
			logger.error("Request not received for Image file"+e.getMessage());
			
		}
		return null;
	}
	
}