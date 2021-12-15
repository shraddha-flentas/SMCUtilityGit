
package com.flentas.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

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
import com.flentas.service.MergeServiceStorage;


@RestController
@RequestMapping("/api")
public class MergeController {

	private static Logger logger=LoggerFactory.getLogger(MergeController.class);
	
	@Autowired
	private MergeServiceStorage mergestorageService;
	
	
	
	@CrossOrigin(origins="*",allowedHeaders="*")
	@PostMapping("/merge")
	public ResponseEntity<Object> uploadFiles(@RequestParam("files") MultipartFile[] files,@RequestHeader (value="Authorization") String authorization)
	{
		
		
		try {
			
			
			  List<String> fileNames = new ArrayList<>();
			
			 logger.info("Request received for merge files successfully"+files);
			  return mergestorageService.save(files,authorization);
			  
				
		} catch (Exception e) {
	
			logger.error("Request not received for merge files"+" "+e.getMessage());
			
		}
		return null;
	}
	
}