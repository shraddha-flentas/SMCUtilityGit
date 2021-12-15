package com.flentas.service;

import java.io.IOException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
  
  public interface ImageService 
  {
  
  public void init();
  
  public ResponseEntity<InputStreamResource> save(MultipartFile file,String authorization) throws IOException;
  public ResponseEntity<Object> saveCompressedImageByQuality(MultipartFile file,String authorization) throws IOException;
  }
 