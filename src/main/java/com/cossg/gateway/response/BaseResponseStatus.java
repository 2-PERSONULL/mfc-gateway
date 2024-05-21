package com.cossg.gateway.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BaseResponseStatus {

	NOT_FOUND_TOKEN(HttpStatus.UNAUTHORIZED, false, 401, "토큰이 필요한 요청입니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, false, 401, "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, false, 401, "만료된 토큰입니다."),
	INVALID_ACCESS(HttpStatus.FORBIDDEN, false, 403, "잘못된 접근입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "서버 에러");

	private final HttpStatusCode httpStatusCode;
	private final boolean isSuccess;
	private final int code;
	private final String message;

}