package com.cossg.gateway.filter;

import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
	private final SecretKey key;

	public AuthorizationHeaderFilter(@Value("${jwt.secret-key}") String key) {
		super(Config.class);
		byte[] keyBytes = Decoders.BASE64.decode(key);
		this.key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest req = exchange.getRequest();
			HttpHeaders header = req.getHeaders();
			log.info("uri={}", req.getURI());

			if(!header.containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
			}

			String authorization = header.get(HttpHeaders.AUTHORIZATION).get(0);
			if(authorization == null || !authorization.startsWith("Bearer")) {
				return onError(exchange, "Unavailable header", HttpStatus.UNAUTHORIZED);
			}

			String jwt = authorization.substring(7);
			if(isValid(jwt)) {
				return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
			}

			return chain.filter(exchange);
		};
	}

	private boolean isValid(String jwt) {

		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(jwt)
				.getBody()
				.getExpiration()
				.before(new Date());
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
		ServerHttpResponse resp = exchange.getResponse();
		resp.setStatusCode(status);

		log.error(err);
		return resp.setComplete();
	}

	public static class Config {
	}
}

