package com.example.demo5;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RestController
public class MainController {

    protected ClientManager clientManager;
    public MainController() {
        // keycloak server url should be taken from environment variables
        clientManager = new ClientManager("localhost:8081", "dev-realm", "spring", "71bc2853-bd4e-4b72-a045-8e91b46fd69d");
    }

    @RequestMapping(value = "/client", method = RequestMethod.GET)
    public String clientToken() {
        return clientManager.getClientToken();
    }

    @RequestMapping(value = "/get-user-token", method = RequestMethod.POST)
    public String userToken(@RequestParam("username") String username, @RequestParam("password") String password) {
        return clientManager.getUserToken(username, password);
    }

    @RequestMapping(value = "/books/{bookID}", method = RequestMethod.POST)
    public ResponseEntity<String> viewBooks(@RequestParam("token") String token, @PathVariable("bookID") String bookID) {
        try{
            clientManager.isUserAllowed(token, bookID);
            // access permit
            return ResponseEntity.ok(bookID+": ...");
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.toString());
        }
    }

    @RequestMapping(value = "/resolve", method = RequestMethod.POST)
    public String resolveToken(@RequestParam("token") String token) {
        return clientManager.resolveToken(token);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity<String> logout(@RequestParam("refresh_token") String refreshToken) {
        if (clientManager.logout(refreshToken))
            return ResponseEntity.status(204).body("user logged out");
        else
            return ResponseEntity.status(400).body("invalid input");

    }
}