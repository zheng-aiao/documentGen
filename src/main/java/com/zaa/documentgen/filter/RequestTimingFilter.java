package com.zaa.documentgen.filter;

import com.zaa.documentgen.config.LicenseConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestTimingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        long startNs = System.nanoTime();
        LicenseConfig.setConfig();
        int statusAfter = 0;
        try {
            filterChain.doFilter(request, response);
            statusAfter = response.getStatus();
        } finally {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000L;
            response.setHeader("X-Response-Time-ms", String.valueOf(durationMs));

            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            String fullPath = query == null ? uri : uri + "?" + query;
            String clientIp = resolveClientIp(request);

            log.info("{} {} -> {} ({} ms) ip={}", method, fullPath, statusAfter, durationMs, clientIp);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
