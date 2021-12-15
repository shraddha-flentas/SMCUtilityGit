package com.flentas.controller;


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
import com.flentas.service.SplitService;

@RestController
@RequestMapping("/api")
public class SplitController {
	
	private static Logger logger=LoggerFactory.getLogger(SplitController.class);
	
	
	
	@Autowired
	SplitService splitService;

	@CrossOrigin(origins = "*")
	@PostMapping("/split")
	public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file,String pageRange,@RequestHeader (value="Authorization") String authorization) {
		// String message = "";
		try {
			logger.info("Request received successfully for split file"+file);
			//return splitService.save(file,pageRange,authorization);
			return splitService.save(file,pageRange,authorization);

		} catch (Exception e) {
			logger.error("Request not received for split file"+e.getMessage());

		}
		return null;
	}

}
