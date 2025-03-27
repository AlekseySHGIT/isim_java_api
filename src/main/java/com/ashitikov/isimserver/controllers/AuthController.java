package com.ashitikov.isimserver.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.HashMap;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import java.util.List;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient.Redirect;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static String ISIM_URL = "http://192.168.1.204:9080"; // Default URL

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, 
                                 @RequestParam String password,
                                 @RequestParam(required = false) String serverUrl) {
        try {
            if (serverUrl != null && !serverUrl.isEmpty()) {
                ISIM_URL = serverUrl;
            }

            // First step: Initialize session with login.jsp
            Session session = new Session();
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            
            String loginJspUrl = ISIM_URL + "/itim/restlogin/login.jsp";
            Response loginResponse = session.get(loginJspUrl, headers);
            
            if (loginResponse.getStatusCode() != 200) {
                throw new AuthenticationError("Failed to initialize login session");
            }

            // Continue with ISIM client authentication
            ISIMClient isimClient = new ISIMClient(ISIM_URL, username, password);
            String csrfToken = isimClient.getCSRF();
            
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("CSRFToken", csrfToken);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("csrfToken", csrfToken);
            
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(response);
        } catch (AuthenticationError e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "An error occurred during login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }

    private static class AuthenticationError extends RuntimeException {
        public AuthenticationError(String message) {
            super(message);
        }
    }

    private static class Session {
        private final HttpClient client;
        private Map<String, String> cookies;

        public Session() {
            this.client = HttpClient.newBuilder()
                .followRedirects(Redirect.NORMAL)
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build();
            this.cookies = new HashMap<>();
        }

        public void post(String url, Map<String, String> headers, Map<String, String> data) {
            try {
                String formData = data.entrySet().stream()
                    .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");

                // Add cookies to headers if we have any
                if (!cookies.isEmpty()) {
                    String cookieHeader = cookies.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("");
                    headers.put("Cookie", cookieHeader);
                }

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Java-http-client")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .headers(headers.entrySet().stream()
                        .flatMap(e -> java.util.stream.Stream.of(e.getKey(), e.getValue()))
                        .toArray(String[]::new))
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("POST Response Status: " + response.statusCode());
                System.out.println("POST Response Headers: " + response.headers().map());
                updateCookies(response);
                System.out.println("Cookies after POST: " + cookies);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to execute POST request", e);
            }
        }

        public Response get(String url, Map<String, String> headers) {
            try {
                // Add cookies to headers if we have any
                if (!cookies.isEmpty()) {
                    String cookieHeader = cookies.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("");
                    headers.put("Cookie", cookieHeader);
                }

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Java-http-client")
                    .GET()
                    .headers(headers.entrySet().stream()
                        .flatMap(e -> java.util.stream.Stream.of(e.getKey(), e.getValue()))
                        .toArray(String[]::new))
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("GET Response Status: " + response.statusCode());
                System.out.println("GET Response Headers: " + response.headers().map());
                updateCookies(response);
                System.out.println("Cookies after GET: " + cookies);
                return new Response(response);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to execute GET request", e);
            }
        }

        private void updateCookies(HttpResponse<?> response) {
            List<String> setCookies = response.headers().allValues("Set-Cookie");
            for (String cookie : setCookies) {
                // Split by first semicolon to get the main cookie part
                String[] parts = cookie.split(";", 2);
                String mainPart = parts[0];
                // Split by first equals sign to handle values that may contain =
                int firstEquals = mainPart.indexOf('=');
                if (firstEquals > 0) {
                    String name = mainPart.substring(0, firstEquals);
                    String value = mainPart.substring(firstEquals + 1);
                    cookies.put(name, value);
                    System.out.println("Added/Updated cookie: " + name + "=" + value);
                }
            }
        }

        public Map<String, String> getCookies() {
            return new HashMap<>(cookies);
        }
    }

    private static class Response {
        private final HttpResponse<String> response;

        public Response(HttpResponse<String> response) {
            this.response = response;
        }

        public String getHeader(String name) {
            return response.headers().firstValue(name).orElse(null);
        }

        public int getStatusCode() {
            return response.statusCode();
        }
    }

    private static class ISIMClient {
        private final String __addr;
        private final Session s;
        private final String CSRF;

        public ISIMClient(String url, String user_, String pass_) {
            this.__addr = url;
            this.s = new Session();
            this.CSRF = login(user_, pass_);
        }

        private String login(String user_, String pass_) {
            System.out.println("1 START /itim/restlogin/login.jsp");
            System.out.println("COOKIES AFTER REST LOGIN");
            System.out.println(s.getCookies());

            System.out.println("2 START /itim/j_security_check");
            String securityUrl = this.__addr + "/itim/j_security_check";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            
            Map<String, String> data = new HashMap<>();
            data.put("j_username", user_);
            data.put("j_password", pass_);
            s.post(securityUrl, headers, data);
            System.out.println(s.getCookies());

            System.out.println("3 START /itim/rest/systemusers/me");
            String meUrl = this.__addr + "/itim/rest/systemusers/me";
            Response response = s.get(meUrl, headers);
            System.out.println(s.getCookies());

            String csrfToken = response.getHeader("CSRFToken");
            if (csrfToken != null) {
                System.out.println("CSRF token: " + csrfToken);
            } else {
                throw new AuthenticationError("Invalid credentials. Please login again.");
            }
            
            return csrfToken;
        }

        public String getCSRF() {
            return CSRF;
        }
    }
}