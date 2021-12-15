package com.flentas.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.flentas.config.JwtTokenUtil;
import com.flentas.model.JwtRequest;
import com.flentas.model.JwtResponse;
import com.flentas.service.JwtUserDetailsService;

@RestController
@CrossOrigin
public class JwtAuthenticationController {
	
	private static Logger logger=LoggerFactory.getLogger(JwtAuthenticationController.class);
	

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
	logger.info("Request for JwtAuthenticationController initiate successfully");
		authenticate(authenticationRequest.getApplicationId(), authenticationRequest.getSecretKey());
	
		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(authenticationRequest.getApplicationId());
	logger.info("loadUserByUsername method initiate successfully"+userDetails.getUsername());

		final String token = jwtTokenUtil.generateToken(userDetails);
		
		logger.info("token generated successfully"+token);
		return ResponseEntity.ok(new JwtResponse(token));
	}

	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			logger.info("username authenticated successfully"+username);
			logger.info("password authenticated successfully"+password);
		} catch (DisabledException e) {
			logger.error("username and password not authenticated successfully"+e.getMessage());
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			logger.error("username and password not authenticated successfully"+e.getMessage());
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}
	
}
