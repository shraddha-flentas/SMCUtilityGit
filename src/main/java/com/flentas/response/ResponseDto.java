package com.flentas.response;

import org.springframework.http.HttpStatus;

public class ResponseDto {
	 public ResponseDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	private String message;
	 private Boolean status;
	 private HttpStatus httpStatus;
	public String getMessage() {
		return message;
	}
	@Override
	public String toString() {
		return "ResponseDto [message=" + message + ", status=" + status + ", httpStatus=" + httpStatus + "]";
	}
	public ResponseDto(String message, Boolean status, HttpStatus httpStatus) {
		super();
		this.message = message;
		this.status = status;
		this.httpStatus = httpStatus;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
	 
	  
}
