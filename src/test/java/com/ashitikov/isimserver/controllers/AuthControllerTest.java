package com.ashitikov.isimserver.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final String SERVER_URL = "http://192.168.1.204:9080";
    private final String USERNAME = "ITIM Manager";
    private final String PASSWORD = "1q@3e4r";

    @Test
    public void testRestConnection() throws Exception {
        System.out.println(SERVER_URL + ", " + USERNAME + ", " + PASSWORD);
        System.out.println("!!!!! ISIM...");
        System.out.println("Attempting to connect to ISIM server at: " + SERVER_URL);

        // Test 1: Successful login flow
        try {
            // Step 1: Initialize session
            MockHttpSession session = new MockHttpSession();
            
            mockMvc.perform(get("/itim/restlogin/login.jsp")
                    .session(session))
                    .andExpect(status().isOk());
            
            System.out.println("Session initialized with ID: " + session.getId());

            // Step 2: Authenticate
            mockMvc.perform(post("/itim/j_security_check")
                            .session(session)
                            .param("j_username", USERNAME)
                            .param("j_password", PASSWORD))
                    .andExpect(status().isOk());

            // Step 3: Get CSRF token
            MvcResult userInfoResult = mockMvc.perform(get("/itim/rest/systemusers/me")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("CSRFToken"))
                    .andReturn();

            String csrfToken = userInfoResult.getResponse().getHeader("CSRFToken");
            System.out.println("ISIMClient instance created with username: " + USERNAME);
            System.out.println("Connection successful!");
            assertNotNull(csrfToken, "CSRF token should not be null");

        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
            throw e;
        }

        // Test 2: Failed login with wrong password
        System.out.println("\nTesting with wrong password...");
        try {
            // Step 1: Initialize session
            MockHttpSession session = new MockHttpSession();
            
            mockMvc.perform(get("/itim/restlogin/login.jsp")
                    .session(session))
                    .andExpect(status().isOk());

            // Step 2: Authenticate with wrong password
            mockMvc.perform(post("/itim/j_security_check")
                            .session(session)
                            .param("j_username", USERNAME)
                            .param("j_password", "234234234"))
                    .andExpect(status().isUnauthorized());

            System.out.println("Connection failed with password '234234234' as expected");

        } catch (Exception e) {
            System.out.println("Test with wrong password failed: " + e.getMessage());
            throw e;
        }

        // Test 3: New successful connection
        System.out.println("\nNEW CONNECTION!");
        try {
            // Step 1: Initialize session
            MockHttpSession session = new MockHttpSession();
            
            mockMvc.perform(get("/itim/restlogin/login.jsp")
                    .session(session))
                    .andExpect(status().isOk());

            // Step 2: Authenticate
            mockMvc.perform(post("/itim/j_security_check")
                            .session(session)
                            .param("j_username", USERNAME)
                            .param("j_password", PASSWORD))
                    .andExpect(status().isOk());

            // Step 3: Get CSRF token
            MvcResult userInfoResult = mockMvc.perform(get("/itim/rest/systemusers/me")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("CSRFToken"))
                    .andReturn();

            String csrfToken = userInfoResult.getResponse().getHeader("CSRFToken");
            System.out.println("ISIMClient instance created with username: " + USERNAME);
            System.out.println("Connection successful!");
            assertNotNull(csrfToken, "CSRF token should not be null");

        } catch (Exception e) {
            System.out.println("New connection failed: " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testSimplifiedAuthentication() throws Exception {
        // Test successful authentication
        mockMvc.perform(post("/api/auth")
                .param("username", USERNAME)
                .param("password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.csrfToken").exists());

        // Test failed authentication
        mockMvc.perform(post("/api/auth")
                .param("username", USERNAME)
                .param("password", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }
}
