package org.bcp.dlmsenrolment.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = Optional.ofNullable(request.getHeader(REQUEST_ID_HEADER))
                .filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startedAt = System.currentTimeMillis();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String user = currentUsername();

        log.info("Incoming request method={} path={} query={} user={} remoteIp={}",
                method,
                path,
                query == null ? "" : query,
                user,
                request.getRemoteAddr());

        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("Request failed method={} path={} status={} user={}",
                    method,
                    path,
                    response.getStatus(),
                    user,
                    ex);
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            log.info("Request completed method={} path={} status={} durationMs={} user={}",
                    method,
                    path,
                    response.getStatus(),
                    durationMs,
                    currentUsername());
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        String name = org.bcp.dlmsenrolment.security.SecurityUtils.getUsername(authentication);
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return "anonymous";
        }
        return name;
    }
}
