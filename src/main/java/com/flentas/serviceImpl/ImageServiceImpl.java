package com.flentas.serviceImpl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import com.flentas.controller.MergeController;
import com.flentas.model.AuditLogEntity;
import com.flentas.repository.AuditLogRepository;
import com.flentas.response.ResponseDto;
import com.flentas.service.ImageService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class ImageServiceImpl implements ImageService {
	
	private static Logger logger=LoggerFactory.getLogger(ImageServiceImpl.class);

	@Autowired

	AuditLogRepository auditLogRepository;

	private final Path root = Paths.get("ImageUpload");

	String originalSize = null;
	
	 @Value("${jwt.secret}")
	    private String secret;
		
	 @Value("${compressionPercentage}")
	    private String compressionPercentage;

	public void init() {
		try {
			if (!Files.exists((root))) {
				Files.createDirectory(root);
			}
			logger.info("Directory created successfully for upload Image file"+root.getRoot());
		} catch (IOException e) {
			logger.error("Directory not created for upload Image file"+e.getMessage());
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

	public ResponseEntity<InputStreamResource> save(MultipartFile file,@RequestHeader String authorization) throws IOException 
	{
		
		 Date date = new Date();
		  
		  SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy_HHmmss");
		  
		  String strDate = formatter.format(date);
		  
		  String destinationFileName="CompressImage//compress_"+strDate+".jpg";
		 
		  Arrays.asList(file).stream().forEach(files -> {
			  
			 
			try {
				Files.copy(files.getInputStream(), this.root.resolve(files.getOriginalFilename()),
						StandardCopyOption.REPLACE_EXISTING);
				 logger.info("Image file uploaded successfully"+file.getOriginalFilename());
				
				File[] fileName = new File(root.toString()).listFiles();

				for (File file2 : fileName) {
					BufferedImage resizedImage = null;
					BufferedImage bufferedImage = null;

					try {
						bufferedImage = ImageIO.read(file2);

						int originalHeight = bufferedImage.getHeight();
						int originalWidth = bufferedImage.getWidth();

						String originHeight = Integer.toString(originalHeight);
						String originWidth = Integer.toString(originalWidth);

						originalSize = originHeight + "*" + originWidth;

						System.out.println(originalSize);

					} catch (IOException e) {
						logger.error("Image file not uploaded successfully"+e.getMessage());
						e.printStackTrace();
					}
					
					

					
					if (bufferedImage.getWidth() >=1000 && bufferedImage.getHeight() >= 1000) {
						 resizedImage = Scalr.resize(bufferedImage, Mode.FIT_TO_WIDTH, 800, 800);
						   

					} 
					 
					 else if (bufferedImage.getWidth() >= 500 && bufferedImage.getHeight() >= 500)
					  { resizedImage = Scalr.resize(bufferedImage, Mode.FIT_TO_WIDTH, 400, 400);
					  
					  }
					 
					  else {
						resizedImage = Scalr.resize(bufferedImage, Mode.FIT_TO_WIDTH, 300, 300);

					}
					
					 
					try {
						//ImageIO.write(resizedImage, "jpg", new File("CompressImage//compress.jpg"));
						
						ImageIO.write(resizedImage, "jpg", new File(destinationFileName));
						
					
						int compressHeight = resizedImage.getHeight();
						int compressWidth = resizedImage.getWidth();
						String newHeight = Integer.toString(compressHeight);
						String newWidth = Integer.toString(compressWidth);
						String compressSize = newHeight + "*" + newWidth;
						System.out.println(compressSize);

						String imageFileName = Arrays.toString(fileName);
						// Timestamp currentDateAndTime=;
						String operationType = "compress";
						String token = authorization;
						String originalSize1 = originalSize;
						String compressSize1 = compressSize;
	
						Claims claims = Jwts.parser().
								setSigningKey(secret).
								parseClaimsJws(token.replace("Bearer", "")).
								getBody();

						String applicationId = claims.getSubject();

						AuditLogEntity auditLogEntity = new AuditLogEntity();

						// auditLogEntity.setTimestamp(currentTimeAndDate);
						auditLogEntity.setType(operationType);
						auditLogEntity.setToken(token);
						auditLogEntity.setApplId(applicationId);
						auditLogEntity.setParam1(imageFileName);
						auditLogEntity.setParam2(originalSize1);
						auditLogEntity.setParam3(compressSize1);
				
						auditLogRepository.save(auditLogEntity);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.error("Image file  compressed  successfully");
					}
					
				}

			} catch (IOException e) {
				logger.error("Image file not compressed successfully");
				e.printStackTrace();
			}

		});
		
		
		File deleteFile = new File(root.toString());
		File[] deletefiles = deleteFile.listFiles();
		{
			for (File f : deletefiles) {
				if (f.isFile() && f.exists())

				{
					f.delete();
					logger.info("uploaded Image file deleted successfully"+deleteFile.getName());
				} else {
					logger.error("uploaded Image file not deleted successfully");
				}

			}
		}
		 

		
		//String fileName = "CompressImage//compress.jpg";
		File imageFile = new File(destinationFileName);
		
		System.out.println("Final Path: " + imageFile.getPath());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
		// headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "GET, POST, PUT");
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		headers.add("Content-Disposition", "filename=" + destinationFileName);
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		headers.setContentLength(imageFile.length());
		ResponseEntity<InputStreamResource> response = new ResponseEntity<InputStreamResource>(
				new InputStreamResource(new FileInputStream(imageFile)), headers, HttpStatus.OK);
		logger.info("compress Image downloaded successfully"+imageFile.getName());
		logger.info("compress Image downloaded successfully"+imageFile.getPath());
	
		return response;

	}
	public ResponseEntity<Object> saveCompressedImageByQuality(MultipartFile file,@RequestHeader String authorization) throws IOException 
	{
		
		init();
		ResponseEntity<Object> response = new ResponseEntity<Object>(null, null, 500);
		ResponseDto resDto = new ResponseDto(null, null, null);
		 Date date = new Date();
		  
		  SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy_HHmmss");
		  
		  String strDate = formatter.format(date);
		  
		  if (createFolder("CompressImage") == false) 
		  {
			resDto.setMessage("Something went wrong.");
			resDto.setHttpStatus(HttpStatus.BAD_REQUEST);
			resDto.setStatus(false);
			return new ResponseEntity<Object>(resDto, null, HttpStatus.BAD_REQUEST);
		  }
		  String destinationFileName="CompressImage//compress_"+strDate+".jpg";

		  //Get compression percentage
		  float outputCompressPercentage = Float.parseFloat(compressionPercentage)/100;

		  String extension = file.getOriginalFilename().split("\\.")[1];
		  
		  //Allow only jpeg and jpg images
		  try {
			if(extension.equals("jpeg") || extension.equals("jpg"))
			  {
				  Arrays.asList(file).stream().forEach(files -> {
					  
					
					try {
						Files.copy(files.getInputStream(), this.root.resolve(files.getOriginalFilename()),
								StandardCopyOption.REPLACE_EXISTING);
						 logger.info("Image file uploaded successfully"+file.getOriginalFilename());
						
						File[] fileName = new File(root.toString()).listFiles();

						for (File file2 : fileName) {
							//Check if input file is equal to uploaded file
							if(file2.getName().equals(file.getOriginalFilename()))
							{	
								BufferedImage bufferedImage = null;
								long originalImageSize = (file2.length())/1024;
	
								try {
									bufferedImage = ImageIO.read(file2);
	
									int originalHeight = bufferedImage.getHeight();
									int originalWidth = bufferedImage.getWidth();
	
									String originHeight = Integer.toString(originalHeight);
									String originWidth = Integer.toString(originalWidth);
	
									originalSize = originHeight + "*" + originWidth;
	
	
								} catch (IOException e) {
									logger.error("Image file not uploaded successfully"+e.getMessage());
									e.printStackTrace();
								}
								
								
	
								//File compressedImageFile = new File("compressed_image.jpg");
								
								//Create compressed image at destination location
							    OutputStream os = new FileOutputStream(destinationFileName);
	
							    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
							    ImageWriter writer = (ImageWriter) writers.next();
	
							    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
							    writer.setOutput(ios);
	
							    ImageWriteParam param = writer.getDefaultWriteParam();
	
							    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
							    // Change the quality value you prefer
							    param.setCompressionQuality(outputCompressPercentage);  
								 
								try {
									
								    writer.write(null, new IIOImage(bufferedImage, null, null), param);
								    long compressedImageSize = (ios.length())/1024;
								    os.close();
								    ios.close();
								    writer.dispose();
	
									
								
									int compressHeight = bufferedImage.getHeight();
									int compressWidth = bufferedImage.getWidth();
									String newHeight = Integer.toString(compressHeight);
									String newWidth = Integer.toString(compressWidth);
									String compressSize = newHeight + "*" + newWidth;
									System.out.println(compressSize);
	
									String imageFileName = Arrays.toString(fileName);
									// Timestamp currentDateAndTime=;
									String operationType = "compress";
									String token = authorization;
									String originalSize1 = originalSize+","+originalImageSize;
									
									String compressSize1 = compressSize+","+compressedImageSize;
				
									Claims claims = Jwts.parser().
											setSigningKey(secret).
											parseClaimsJws(token.replace("Bearer", "")).
											getBody();
	
									String applicationId = claims.getSubject();
	
									AuditLogEntity auditLogEntity = new AuditLogEntity();
	
									// auditLogEntity.setTimestamp(currentTimeAndDate);
									auditLogEntity.setType(operationType);
									auditLogEntity.setToken(token);
									auditLogEntity.setApplId(applicationId);
									auditLogEntity.setParam1(imageFileName);
									auditLogEntity.setParam2(originalSize1);
									auditLogEntity.setParam3(compressSize1);
							
									auditLogRepository.save(auditLogEntity);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									logger.error("Image file  compressed  successfully");
								}
								
						}
					}

					} catch (IOException e) {
						logger.error("Image file not compressed successfully");
						e.printStackTrace();
					}

				});
				
				
				//Delete uploaded file from uploads folder
				Path path = root.resolve(file.getOriginalFilename());
				File deleteFile = new File(path.toString());
				
				if (deleteFile.isFile() && deleteFile.exists())

				{
					deleteFile.delete();
					logger.info("uploaded Image file deleted successfully"+deleteFile.getName());
				} else {
					logger.error("uploaded Image file not deleted successfully");
				}

				
				//String fileName = "CompressImage//compress.jpg";
				File imageFile = new File(destinationFileName);
				
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType("application/octet-stream"));
				// headers.add("Access-Control-Allow-Origin", "*");
				headers.add("Access-Control-Allow-Methods", "GET, POST, PUT");
				headers.add("Access-Control-Allow-Headers", "Content-Type");
				headers.add("Content-Disposition", "filename=" + destinationFileName);
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");
				headers.setContentLength(imageFile.length());
				response = new ResponseEntity<Object>(
						new InputStreamResource(new FileInputStream(imageFile)), headers, HttpStatus.OK);
				logger.info("compress Image downloaded successfully"+imageFile.getName());
				logger.info("compress Image downloaded successfully"+imageFile.getPath());
						
			  }
			  else
			  {
				  resDto.setMessage("Invalid input");
				  resDto.setHttpStatus(HttpStatus.BAD_REQUEST);
				  resDto.setStatus(false);
				  response = new ResponseEntity<Object>(resDto,null,HttpStatus.BAD_REQUEST);
			  }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resDto.setMessage("Something went wrong");
			resDto.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			resDto.setStatus(false);
			response = new ResponseEntity<Object>(resDto,null,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	
		  return response;
	}
}
