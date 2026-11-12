package org.bcp.dlmsenrolment.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Service
public class AccountValidationService {

    private final RestTemplate restTemplate;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String grantType;
    private final String username;
    private final String password;
    private final String validationUrl;

    public AccountValidationService(
            @Qualifier("accountValidationRestTemplate") RestTemplate restTemplate,
            @Value("${account-validation.token-url}") String tokenUrl,
            @Value("${account-validation.client-id}") String clientId,
            @Value("${account-validation.client-secret}") String clientSecret,
            @Value("${account-validation.grant-type}") String grantType,
            @Value("${account-validation.username}") String username,
            @Value("${account-validation.password}") String password,
            @Value("${account-validation.url}") String validationUrl) {
        this.restTemplate = restTemplate;
        this.tokenUrl = Objects.requireNonNull(tokenUrl);
        this.clientId = Objects.requireNonNull(clientId);
        this.clientSecret = Objects.requireNonNull(clientSecret);
        this.grantType = Objects.requireNonNull(grantType);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        this.validationUrl = Objects.requireNonNull(validationUrl);
    }

    public JsonNode validateAccount(String accountNumber) {
        if (clientId.isBlank() || clientSecret.isBlank() || username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Account validation client credentials are not configured");
        }

        String accessToken = retrieveAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(Objects.requireNonNull(accessToken));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "paymentInfo", Map.of(
                        "account", Map.of("accountNumber", accountNumber)));
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                Objects.requireNonNull(validationUrl),
                Objects.requireNonNull(HttpMethod.POST),
                new HttpEntity<>(requestBody, headers),
                JsonNode.class);

        if (response.getStatusCode().value() != 200 || response.getBody() == null) {
            throw new RestClientException("Account validation returned an unexpected response");
        }
        return response.getBody();
    }

    private String retrieveAccessToken() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", grantType);
        parameters.add("client_id", clientId);
        parameters.add("client_secret", clientSecret);
        parameters.add("username", username);
        parameters.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                Objects.requireNonNull(tokenUrl),
                Objects.requireNonNull(HttpMethod.POST),
                new HttpEntity<>(parameters, headers),
                JsonNode.class);
        JsonNode tokenResponse = response.getBody();
        JsonNode token = tokenResponse == null ? null : tokenResponse.get("access_token");
        if (token == null || token.asText().isBlank()) {
            throw new RestClientException("RH-SSO did not return an access token");
        }
        return token.asText();
    }
}