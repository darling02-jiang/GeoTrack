package com.geotrack.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关转发请求时打印访问日志（方法、路径、状态码、耗时）。
 */
@Component
public class GatewayRequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GatewayRequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();
        String query = req.getURI().getQuery();
        String pathWithQuery = query == null ? path : path + "?" + query;
        String remote =
                req.getRemoteAddress() != null ? req.getRemoteAddress().toString() : "-";

        return chain
                .filter(exchange)
                .doFinally(
                        signalType -> {
                            var status = exchange.getResponse().getStatusCode();
                            int code = status != null ? status.value() : 0;
                            log.info(
                                    "{} {} from {} -> {} ({} ms)",
                                    req.getMethod(),
                                    pathWithQuery,
                                    remote,
                                    code,
                                    System.currentTimeMillis() - start);
                        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
