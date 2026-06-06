package com.geotrack.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 每个 HTTP 请求完成后打印一行访问日志（方法、路径、状态码、耗时）。
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            String query = request.getQueryString();
            String pathWithQuery =
                    query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
            log.info(
                    "{} {} from {} -> {} ({} ms)",
                    request.getMethod(),
                    pathWithQuery,
                    request.getRemoteAddr(),
                    response.getStatus(),
                    System.currentTimeMillis() - start);
        }
    }
}
