package com.flentas.service;
 
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.flentas.controller.ImageController;
 
@Service
public class JwtUserDetailsService implements UserDetailsService{
	
	private static Logger logger=LoggerFactory.getLogger(JwtUserDetailsService.class);
	
    @Value("${appData1}")
    private String appData1;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	logger.info("Request for jwtUserDetailsService initiate successfully"+username);

        //Split the appId and secret key string with seperator and convert to individual array
        String[] appData11Array = appData1.split("##");
        String[] newappData11 = null;
        logger.info("appData1"+appData1);
        
        List<String> applicationArray = new ArrayList<String>();        
        for (String applicationData : appData11Array) 
        {
            newappData11 = applicationData.split("~");
 
            for (String appData11: newappData11)
                {
                    applicationArray.add(appData11);
                }
        }

        int i = 0;
        //Create seperate array for appID and secret key
        List<String> applicationIdsArray = new ArrayList<String>();    
        List<String> secretKeyArray = new ArrayList<String>();    
        for (String appData11: applicationArray)
        {
            //If even idex then appID otherwise its secret key
            if(i % 2 == 0)
            {
                applicationIdsArray.add(appData11);
            }
            else
            {
                secretKeyArray.add(appData11);
            }
            ++i;    
        }

        //Check each appID and match its corresponding secretkey
        for (int appIdIndex = 0;appIdIndex<applicationIdsArray.size();++appIdIndex)
        {
            String appId = applicationIdsArray.get(appIdIndex);
            String appSecretKey = secretKeyArray.get(appIdIndex);

            if (appId.equals(username)) {
            	
            	
            	logger.info("appId checked successfully"+appId);
            	logger.info("appSecretKey matched successfully"+appSecretKey);
                return new User(appId, appSecretKey,
                        new ArrayList<>());
            } 
 
        }
        return null;

    }
}