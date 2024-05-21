package com.cossg.gateway.response;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Order(-1)
@RequiredArgsConstructor
@Component
public class ErrorResponse implements ErrorWebExceptionHandler {
	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		ServerHttpResponse response = exchange.getResponse();

		BaseResponse<?> baseResponse;
		if (ex instanceof BaseException e) {
			baseResponse = new BaseResponse<>(e.getStatus());
			response.setStatusCode(baseResponse.httpStatus());
		} else {
			baseResponse = new BaseResponse<>(BaseResponseStatus.INTERNAL_SERVER_ERROR);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		try {
			return response
					.writeWith(Mono.just(response.bufferFactory()
					.wrap(objectMapper.writeValueAsBytes(baseResponse))));
		} catch (JsonProcessingException e) {
			return Mono.error(e);
		}
	}
}
