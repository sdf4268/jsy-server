package com.jeonsy.resume.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.jeonsy.resume.dto.SmartThingsTokenDto;
import com.jeonsy.resume.entity.T_SMARTTHINGS_TOKEN;
import com.jeonsy.resume.repository.SmartThingsTokenRepository;

@Service
@Transactional
public class SmartThingsTokenService {
	
	@Autowired
	private SmartThingsTokenRepository tokenRepository;
	
	private final RestTemplate restTemplate = new RestTemplate();

	private String clientId = "5166fb72-f8a6-47b5-86a4-789e97edd160";

	private String clientSecret = "ef5e570c-1738-4729-a00e-053e69c24c40";

	private String tokenUrl = "https://api.smartthings.com/oauth/token";

	// get token from smartthings api
	public String refreshAccessToken() {
        Optional<T_SMARTTHINGS_TOKEN> latestTokenOpt = getLatestToken();

        if (!latestTokenOpt.isPresent()) {
            System.out.println("Last token is null");
            return null;
        }

        T_SMARTTHINGS_TOKEN latestToken = latestTokenOpt.get();
        String refreshToken = latestToken.getRefreshToken();


        // HTTP Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret); // Basic Auth 인증 사용

        // request body
        String requestBody = "grant_type=refresh_token&client_id=" + clientId + "&refresh_token=" + refreshToken;

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Request SmartThings API
            ResponseEntity<SmartThingsTokenDto> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, requestEntity, SmartThingsTokenDto.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            	SmartThingsTokenDto tokenResponse = response.getBody();

                // save token to db
                saveToken(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(),
                          tokenResponse.getExpiresIn(), tokenResponse.getInstalledAppId());

                return tokenResponse.getAccessToken();
            } else {
            	System.out.println("Failed to get Token");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error occur to get Token");
            return null;
        }
    }
	
	public void saveToken(String accessToken, String refreshToken, int expiresIn, String installedAppId) {
		LocalDateTime createAt = LocalDateTime.now();
		T_SMARTTHINGS_TOKEN token = new T_SMARTTHINGS_TOKEN();
		token.setAccessToken(accessToken);
		token.setRefreshToken(refreshToken);
		token.setExpiresIn(expiresIn);
		token.setInstalledAppId(installedAppId);
		token.setCreatedAt(createAt);
		tokenRepository.save(token);
		System.out.println("toekn saved: " + token);
	}

	public Optional<T_SMARTTHINGS_TOKEN> getLatestToken() {
		return tokenRepository.findTopByOrderByCreatedAtDesc();
	}
}
