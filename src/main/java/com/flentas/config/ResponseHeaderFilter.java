package com.flentas.config;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ResponseHeaderFilter implements Filter {
	 private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHeaderFilter.class);
   @Override
   public void destroy() {
	   System.out.println("inside filter class:destroy");
   }

   @Override
   public void doFilter
      (ServletRequest request, ServletResponse response, FilterChain filterchain) 
      throws IOException, ServletException {
	   
	   System.out.println("inside filter class");
	   
	   HttpServletRequest servrequest = (HttpServletRequest) request;
       HttpServletResponse servresponse = (HttpServletResponse) response;

       LOGGER.info("Logging Request  {} : {}", servrequest.getMethod(), servrequest.getRequestURI());
       String sessionid = servrequest.getSession().getId();
      // servresponse.setHeader("Access-Control-Allow-Origin", "*");
      // servresponse.setHeader("X-Frame-Options", "DENY");
       //servresponse.setHeader("X-Frame-Options", "SAMEORIGIN");
       servresponse.setHeader("SET-COOKIE","JSESSIONID=" + sessionid + "; HttpOnly");
//       call next filter in the filter chain
       filterchain.doFilter(servrequest, servresponse);

       LOGGER.info("Logging Response :{}", response.getContentType());
          //place to perform request processing.
      }

   @Override
   public void init(FilterConfig filterconfig) throws ServletException {
	   System.out.println("inside filter class:init");
//	   Log.info("Testing headers");
   }
}