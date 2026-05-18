package com.example.demo;

import com.example.demo.security.JwtHandshakeInterceptor;
import com.example.demo.security.StompAuthChannelInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class WebSocketSecurityTest {

    @Autowired
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Autowired
    private StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Test
    void websocketSecurityBeansAreRegistered() {
        assertNotNull(jwtHandshakeInterceptor);
        assertNotNull(stompAuthChannelInterceptor);
    }
}
