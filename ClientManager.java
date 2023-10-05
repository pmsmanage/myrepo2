package com.example.demo5;

import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jose.shaded.json.parser.JSONParser;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

public class ClientManager {
    protected String clientToken;
    protected String realmName;

    protected String keycloakServer; //without http
    protected String clientID;

    protected String clientSecret;

    protected long expire_time; // store when the current token will be invalid, time in milliseconds
    public ClientManager(String keycloakServer, String realmName, String clientID, String clientSecret) {
        this.keycloakServer = keycloakServer;
        this.realmName = realmName;
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.expire_time = 0;
    }

    public String getClientToken(){
        Date date = new Date();
        if (this.expire_time < date.getTime() + 1000)
            return this.getNewToken();
        else return this.clientToken;
    }

    protected String getNewToken(){
        RestTemplate restTemplate = new RestTemplate();
        String apiURL = "https://"+keycloakServer+"/auth/realms/"+realmName+"/protocol/openid-connect/token";

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("client_id", this.clientID);
            map.add("client_secret", this.clientSecret);
            map.add("scope", "openid");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);


            ResponseEntity<String> responseEntity = restTemplate.exchange(apiURL, HttpMethod.POST, entity, String.class);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(responseEntity.getBody());
            this.clientToken = json.getAsString("id_token");
            Date date = new Date();
            // calculate the time this token will be invalid, keycloak give how long until it will be invalid
            // keycloak response with time in seconds so it get multiply with 1000 to store as milliseconds
            this.expire_time = date.getTime() + 1000 * json.getAsNumber("expires_in").longValue();
            return this.clientToken;
        } catch (Exception e) {
            return apiURL + e;
        }
    }

    public String getUserToken(String username, String password){
        try{
            RestTemplate restTemplate = new RestTemplate();
            String apiURL = "https://"+keycloakServer+"/auth/realms/"+realmName+"/protocol/openid-connect/token";


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "password");
            map.add("client_id", this.clientID);
            map.add("client_secret", this.clientSecret);
            map.add("username", username);
            map.add("password", password);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);


            ResponseEntity<String> responseEntity = restTemplate.exchange(apiURL, HttpMethod.POST, entity, String.class);
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(responseEntity.getBody());
            return json.getAsString("access_token");

        } catch (Exception e) {
            return e.toString();
        }
    }

    public String resolveToken(String token){
        RestTemplate restTemplate = new RestTemplate();
        String apiURL = "https://"+keycloakServer+"/auth/realms/"+this.realmName+"/protocol/openid-connect/token/introspect";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String username = this.clientID;
        String password = this.clientSecret;
        String credentials = username + ':' + password;
        String base64Credentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

        headers.add("Authorization", "Basic "+base64Credentials);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("token", token);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(apiURL, HttpMethod.POST, entity, String.class);
        return responseEntity.getBody();
    }

    public void isUserAllowed(String token, String resource) throws HttpClientErrorException {
        // check if user is allowed to access a specific resource

        RestTemplate restTemplate = new RestTemplate();
        String apiURL = "https://"+keycloakServer+"/auth/realms/"+this.realmName+"/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Bearer "+token);


        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket");
        map.add("audience", this.clientID);
        map.add("permission", resource);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        restTemplate.exchange(apiURL, HttpMethod.POST, entity, String.class);
        // if not accepted an HttpClientErrorException will be thrown
    }

    public boolean logout(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();
        String apiURL = "https://"+keycloakServer+"/auth/realms/"+this.realmName+"/protocol/openid-connect/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String username = this.clientID;
        String password = this.clientSecret;
        String credentials = username + ':' + password;
        String base64Credentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        headers.add("Authorization", "Basic "+base64Credentials);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("refresh_token", refreshToken);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            restTemplate.exchange(apiURL, HttpMethod.POST, entity, String.class);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
