
package com.flentas.serviceImpl;

import java.io.File;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;
import com.flentas.model.AuditLogEntity;
import com.flentas.repository.AuditLogRepository;
import com.flentas.response.ResponseDto;
import com.flentas.service.MergeServiceStorage;

@Service
public class MergeServiceImpl implements MergeServiceStorage {

	private static Logger logger = LoggerFactory.getLogger(MergeServiceImpl.class);

	@Autowired

	private AuditLogRepository auditLogRepository;

	private final Path root = Paths.get("mergeUploads");

	@Value("${jwt.secret}")
	private String secret;

	public void init() {
		try {
			if (!Files.exists((root))) {
				Files.createDirectory(root);
			}
			logger.info("Directory created successfully for upload files" + root.getFileName());
		} catch (IOException e) {
			logger.error("Directory not created for upload files" + root.getFileName());
			throw new RuntimeException("Could not initialize folder for upload!");
		}
	}

	private boolean createFolder(String path) {
		boolean resp = true;
		try {
			File pathAsFile = new File(path);

			if (!Files.exists(Paths.get(path))) {
				pathAsFile.mkdir();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error : " + e.getMessage());
			resp = false;

		}
		return resp;
	}

	public ResponseEntity<Object> save(MultipartFile[] file,
			@RequestHeader(value = "Authorization") String authorization) throws IOException {

		init();
		ResponseEntity<Object> response = new ResponseEntity<Object>(null, null, 500);
		ResponseDto resDto = new ResponseDto(null, null, null);
		// instantiatE PDFMergerUtility class
		PDFMergerUtility pdfMerger = new PDFMergerUtility();

		Date date = new Date();

		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy_HHmmss");

		String strDate = formatter.format(date);

		if (createFolder("mergeOutput") == false) 
		{
			resDto.setMessage("Something went wrong.");
			resDto.setHttpStatus(HttpStatus.BAD_REQUEST);
			resDto.setStatus(false);
			return new ResponseEntity<Object>(resDto, null, HttpStatus.BAD_REQUEST);
		}
		/*
		 * File pathAsFile = new File(path);
		 * 
		 * if (!Files.exists(Paths.get(path))) { pathAsFile.mkdir(); }
		 */

		String destinationFileName = "mergeOutput//merged_" + strDate + ".pdf";

		// set destination file path
		// pdfMerger.setDestinationFileName("mergeOutput//merged.pdf");
		pdfMerger.setDestinationFileName(destinationFileName);

		List<MultipartFile> fileUploadArrayList = Arrays.asList(file);
		// Initialize counter
		int fileCounter = 1;
		int invalidInput = 0;
		try {
			for (MultipartFile files : fileUploadArrayList) {
				String extension = files.getOriginalFilename().split("\\.")[1];
				// If extension is not pdf then return error
				if (!extension.equals("pdf")) {
					invalidInput = 1;
					break;
				}
				try {
					// upload file to uploads folder and add counter before original filename
					Files.copy(files.getInputStream(), this.root.resolve(fileCounter + files.getOriginalFilename()),
							StandardCopyOption.REPLACE_EXISTING);
					logger.info("files uploaded successfully" + files.getOriginalFilename());
				} catch (IOException e) {
					logger.error("files not uploaded successfully" + e.getMessage());
					e.printStackTrace();

				}
				fileCounter++;
			}

			// If invalid input (other than pdf) return errors
			if (invalidInput == 1) {
				resDto.setMessage("Invalid input");
				resDto.setHttpStatus(HttpStatus.BAD_REQUEST);
				resDto.setStatus(false);
				response = new ResponseEntity<Object>(resDto, null, HttpStatus.BAD_REQUEST);

			} else {
				// Get all files from uploads folder and sort the array.
				File[] listFileNames = new File(root.toString()).listFiles();
				Arrays.sort(listFileNames);
				

				for (File uploadedFile : listFileNames) {
					for(MultipartFile inputFile : fileUploadArrayList)
					{
						if(inputFile.getOriginalFilename().equals(uploadedFile.getName().substring( 1, uploadedFile.getName().length())))
						{
							logger.info("Uploaded file added to pdf merger: " + uploadedFile.getAbsolutePath());
							pdfMerger.addSource(uploadedFile.getAbsoluteFile());
						}
					}
					

				}

				// merge documents
				try {
					pdfMerger.mergeDocuments(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
					resDto.setMessage("Enable to merge files");
					resDto.setHttpStatus(HttpStatus.BAD_REQUEST);
					resDto.setStatus(false);
					HttpHeaders headers = new HttpHeaders();
					//headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
					headers.setContentType(MediaType.APPLICATION_JSON);
					response = new ResponseEntity<Object>(resDto, headers, HttpStatus.BAD_REQUEST);
					//response = new ResponseEntity<Object>(new InputStreamResource(new FileInputStream(pdfFile)), headers,HttpStatus.OK);
					return response;
				}

				logger.info("PDF Merged successfully");

				// Add audit log to table
				String fileNames = Arrays.toString(listFileNames);
				// Timestamp currentDateAndTime=yyyy-MM-dd HH:mm:ss;
				String operationType = "merge";
				String token = authorization;
				String parameter2 = "merged_" + strDate + ".pdf";
				String parameter3 = "-";

				// Extract application ID from bearer token

				Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token.replace("Bearer", ""))
						.getBody();

				String applicationId = claims.getSubject();

				AuditLogEntity auditLogEntity = new AuditLogEntity();

				// auditLogEntity.setTimestamp(currentTimeAndDate);
				auditLogEntity.setType(operationType);
				auditLogEntity.setToken(token);
				auditLogEntity.setApplId(applicationId);
				auditLogEntity.setParam1(fileNames);
				auditLogEntity.setParam2(parameter2);
				auditLogEntity.setParam3(parameter3);

				auditLogRepository.save(auditLogEntity);

				logger.info("Added to audit successfully");

				File deleteFile = new File(root.toString());
				File[] deletefiles = deleteFile.listFiles();
				//Delete uploaded file
				for (File f : deletefiles) 
				{
					for(MultipartFile inputFile : fileUploadArrayList)
					{
						if (f.isFile() && f.exists() && inputFile.getOriginalFilename().equals(f.getName().substring( 1,f.getName().length())))
						{
							f.delete();
							logger.info("uploaded files deleted successfully");

						} 
						else {
							logger.error("uploaded files are not deleted successfully");
						}
					}
				}
	
				File pdfFile = new File(destinationFileName);
				logger.info("Final Path: " + pdfFile.getPath());
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
				// headers.add("Access-Control-Allow-Origin", "*");
				headers.add("Access-Control-Allow-Methods", "GET, POST, PUT");
				headers.add("Access-Control-Allow-Headers", "Content-Type");
				headers.add("Content-Disposition", "filename=" + destinationFileName);
				// headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;
				// filename=merged.pdf")
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");

				headers.setContentLength(pdfFile.length());
				response = new ResponseEntity<Object>(new InputStreamResource(new FileInputStream(pdfFile)), headers,
						HttpStatus.OK);
				logger.info("merged file downloaded successfully:" + pdfFile.getName());
				logger.info("merged file downloaded successfully:" + pdfFile.getPath());

			}
		} catch (Exception e) {
			e.printStackTrace();

			resDto.setMessage("Something went wrong");
			resDto.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			resDto.setStatus(false);
			response = new ResponseEntity<Object>(resDto, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;

	}
}
