
package com.flentas.serviceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
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
import com.flentas.controller.SplitController;
import com.flentas.model.AuditLogEntity;
import com.flentas.repository.AuditLogRepository;
import com.flentas.response.ResponseDto;
import com.flentas.service.SplitService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Service
public class SplitServiceImpl implements SplitService {

	private static Logger logger = LoggerFactory.getLogger(SplitServiceImpl.class);

	@Autowired

	private AuditLogRepository auditLogRepository;

	private final Path root = Paths.get("splitUploads");
	private final Path outputPdfPath = Paths.get("splitOutput//splitPdfOutput");
	private final Path outputZipPath = Paths.get("splitOutput//splitZipOutput");

	Date date = new Date();

	SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy_HHmmss");

	String strDate = formatter.format(date);

	@Value("${jwt.secret}")
	private String secret;

	@Override
	public void init() {
		try {
			if (!Files.exists((root))) {
				Files.createDirectory(root);
			}
			logger.info("Directory created successfully for upload file" + root.getRoot());
		} catch (IOException e) {
			logger.error("Directory not created for upload file" + e.getMessage());
			throw new RuntimeException("Could not initialize folder for upload!");
		}
	}
	
	private boolean createFolder(String path) {
		boolean resp = true;
		try {
			File pathAsFile = new File(path);

			if (!Files.exists(Paths.get(path))) {
				pathAsFile.mkdirs();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error : " + e.getMessage());
			resp = false;

		}
		return resp;
	}


	
	@Override
	public ResponseEntity<Object> save(MultipartFile file,String pageRange,@RequestHeader (value="Authorization") String authorization) throws IOException {
		
		init();
		String extension = file.getOriginalFilename().split("\\.")[1];
		ResponseEntity<Object> response = new ResponseEntity<Object>(null, null, 500);
		
		ResponseDto resDto = new ResponseDto(null, null, null);
		
		if (createFolder("splitOutput//splitPdfOutput") == false) 
		{
			resDto.setMessage("Something went wrong.");
			resDto.setHttpStatus(HttpStatus.BAD_REQUEST);
			resDto.setStatus(false);
			return new ResponseEntity<Object>(resDto, null, HttpStatus.BAD_REQUEST);
		}
		
		if (createFolder("splitOutput//splitZipOutput") == false) 
		{
			resDto.setMessage("Something went wrong.");
			resDto.setHttpStatus(HttpStatus.BAD_REQUEST);
			resDto.setStatus(false);
			return new ResponseEntity<Object>(resDto, null, HttpStatus.BAD_REQUEST);
		}
		try {
			
			//If invalid Extension return 400
			if(!extension.equals("pdf"))
			{
				resDto.setMessage("Invalid input");
				resDto.setHttpStatus(HttpStatus.BAD_REQUEST);
				resDto.setStatus(false);
				response = new ResponseEntity<Object>(resDto,null,HttpStatus.BAD_REQUEST);
				
			}
			//Else split logic
			else
			{

				//Delete existing splitted pdf & zip files from the output folders
				File deleteExistingPdfFile = new File(outputPdfPath.toString());
				File deleteExistingZipFile = new File(outputZipPath.toString());
				File[] deletePdfFiles = deleteExistingPdfFile.listFiles();
				{
					for (File f : deletePdfFiles) {
						if (f.isFile() && f.exists())

						{
							f.delete();
							logger.info("uploaded file deleted successfully:"+f.getName());
						} else {
							logger.error("uploaded file not deleted successfully:"+f.getName());
						}

					}
				}
				
				File[] deleteZipFiles = deleteExistingZipFile.listFiles();
				{
					for (File f : deleteZipFiles) {
						if (f.isFile() && f.exists())

						{
							f.delete();
							logger.info("uploaded file deleted successfully:"+f.getName());
						} else {
							logger.error("uploaded file not deleted successfully:"+f.getName());
						}

					}
				}
				
				try {
					Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()),
							StandardCopyOption.REPLACE_EXISTING);
					  logger.info("file uploaded successfully:"+file.getOriginalFilename());
				}
					catch (Exception e) {
						logger.error("file not uploaded successfully:"+file.getOriginalFilename());
						throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
					}
				
				
				//Split the page number sequence by "," operator and convert it into array
				String[] pageNumber = pageRange.split(",");
				List<String> pageNumberArray = new ArrayList<String>();
				for (String number : pageNumber) {
					pageNumberArray.add(number);
				}
				
				File[] files = new File(root.toString()).listFiles();
				try {
					for (File file2 : files) 
					{
						//Check if input file is equal to uploaded file
						if(file2.getName().equals(file.getOriginalFilename()))
						{
							PDDocument document = PDDocument.load(file2);
							int i =1;
							int fromPage = 0;
							int toPage = 0;
							//Iterate through page range array
							for (String page : pageNumberArray) 
							{
								logger.info("Current page number processing for splitting:"+page);
								Splitter splitter = new Splitter();
								
								//If page contains dash operator then range is specified else consider as single page 
								if(page.indexOf("-") > 0 )
								{					
									//Split by - operator, and get to and from page range
									String[] pageRangeArray = page.split("-");
									fromPage = Integer.parseInt(pageRangeArray[0]);
									toPage = Integer.parseInt(pageRangeArray[1]);
								}
								else
								{
									//No range is specified hence consider to and from as single page
									fromPage = Integer.parseInt(page);
									toPage = Integer.parseInt(page);
								}
								
								
								 splitter.setStartPage(fromPage); 
								 splitter.setEndPage(toPage);
								 splitter.setSplitAtPage(toPage);
								 List<PDDocument> Pages = splitter.split(document);
						
						            for (PDDocument doc : Pages) {
						          
						          
						            	logger.info("splitOutput//splitPdfOutput//split" + i + ".pdf");
						              doc.save("splitOutput//splitPdfOutput//split" + i +"-"+strDate+".pdf"); 
						            
						                doc.close();                 
						            }     
						            i++;
								
							}
							logger.info("split files are created successfully:"+document.getNumberOfPages());
							document.close();
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
						String fileNames=Arrays.toString(files);
						
						//Timestamp currentDateAndTime=;
						String operationType="split";
						String token=authorization;
						String pageRanges=Arrays.toString(pageNumber);
						String parameter3="-";
		
						Claims claims = Jwts.parser().
								setSigningKey(secret).
								parseClaimsJws(token.replace("Bearer", "")).
								getBody();
		
						String applicationId = claims.getSubject();
		
						
						AuditLogEntity auditLogEntity = new AuditLogEntity();
						
						//auditLogEntity.setTimestamp(currentTimeAndDate); 
						auditLogEntity.setType(operationType);
						auditLogEntity.setToken(token);
						auditLogEntity.setApplId(applicationId);
						auditLogEntity.setParam1(fileNames);
						auditLogEntity.setParam2(pageRanges);
						auditLogEntity.setParam3(parameter3);
						auditLogRepository.save(auditLogEntity);
		
						  FileOutputStream fos = new FileOutputStream("splitOutput//splitZipOutput//split"); 
						  ZipOutputStream zos = new ZipOutputStream(fos); 
						  final Path splitOutput = Paths.get("splitOutput//splitPdfOutput");
						  byte[] buffer = new byte[5120]; 
						  File[] listFileNames = new File(splitOutput.toString()).listFiles();
						  
						  for (File uploadedFile : listFileNames) 
						  { 
							  
							  FileInputStream fis = new FileInputStream(uploadedFile); 
							  zos.putNextEntry(new ZipEntry(uploadedFile.getName())); 
							  int length;
							  while ((length = fis.read(buffer)) > 0) 
							  { 
								  zos.write(buffer, 0, length); 
							  }
							  zos.closeEntry(); 
							  fis.close();
							  
							  
						  }
		
						 zos.close();
						 
						//Delete uploaded file from uploads folder
						Path path = root.resolve(file.getOriginalFilename());
						File deleteFile = new File(path.toString());
							
						if (deleteFile.isFile() && deleteFile.exists())
						{
							deleteFile.delete();
							logger.info("uploaded file deleted successfully:"+deleteFile.getName());
						} else {
							logger.error("uploaded file not deleted successfully:"+deleteFile.getName());
						}
		
						String fileName = "splitOutput//splitZipOutput//split";
						File pdfFile = new File(fileName);
						
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.parseMediaType("application/zip"));
						//headers.add("Access-Control-Allow-Origin", "*");
						headers.add("Access-Control-Allow-Methods", "GET, POST, PUT");
						headers.add("Access-Control-Allow-Headers", "Content-Type");
						headers.add("Content-Disposition", "filename=" + fileName);
						headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
						headers.add("Pragma", "no-cache");
						headers.add("Expires", "0");
						headers.add("X-Frame-Options", "SAMEORIGIN");
						headers.setContentLength(pdfFile.length());
						response = new ResponseEntity<Object>(
								new InputStreamResource(new FileInputStream(pdfFile)), headers, HttpStatus.OK);
						logger.info("merged file downloaded successfully:"+pdfFile.getName());
						logger.info("merged file downloaded successfully:"+pdfFile.getPath());
		
						
					}
				
		} 
		catch (Exception e) {
			e.printStackTrace();
//			response = new ResponseEntity<Object>(
//					"\"message\":\"Something went wrong\"", null, 500);
			
			resDto.setMessage("Something went wrong");
			resDto.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			resDto.setStatus(false);
			response = new ResponseEntity<Object>(resDto,null,HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		return response;
	}
	
}
