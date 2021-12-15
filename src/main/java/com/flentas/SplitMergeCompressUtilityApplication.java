package com.flentas;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.flentas.service.ImageService;
import com.flentas.service.MergeServiceStorage;
import com.flentas.service.SplitService;
import com.flentas.serviceImpl.MergeServiceImpl;


@SpringBootApplication
//@ServletComponentScan
public class SplitMergeCompressUtilityApplication implements CommandLineRunner  {
	
	private static Logger logger=LoggerFactory.getLogger(SplitMergeCompressUtilityApplication.class);
	
	
	@Resource
	  MergeServiceStorage storageService;
	  ImageService imageService;
	  SplitService splitService;
		
	

	public static void main(String[] args) {
		SpringApplication.run(SplitMergeCompressUtilityApplication.class, args);
		logger.info("SpringBootApplication executed successfully");
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		
	}
	

}
