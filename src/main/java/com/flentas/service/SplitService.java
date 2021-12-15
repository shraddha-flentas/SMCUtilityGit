package com.flentas.service;

import java.io.IOException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface SplitService {
	
	 public void init();
	
	ResponseEntity<Object> save(MultipartFile file, String pageRange,String authorization) throws IOException;
}
