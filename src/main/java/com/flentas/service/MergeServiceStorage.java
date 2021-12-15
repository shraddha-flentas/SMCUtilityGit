package com.flentas.service;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface MergeServiceStorage {
	
	public void init();
	public ResponseEntity<Object> save(MultipartFile[] file,String authorization) throws IOException;
	


}