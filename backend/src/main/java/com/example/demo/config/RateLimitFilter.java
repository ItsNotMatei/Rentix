package com.example.demo.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    private Bucket generalBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(120).refillGreedy(120, Duration.ofMinutes(1)).build())
                .build();
    }

    private Bucket authBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(10).refillGreedy(10, Duration.ofMinutes(1)).build())
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        String ip = httpRequest.getRemoteAddr();

        boolean authPath = path.startsWith("/api/auth/signin") || path.startsWith("/api/auth/signup");
        Bucket bucket = authPath
                ? authBuckets.computeIfAbsent(ip, k -> authBucket())
                : generalBuckets.computeIfAbsent(ip, k -> generalBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"message\":\"Prea multe cereri. Încearcă din nou.\"}");
        }
    }
}
