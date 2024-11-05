package com.sharespace.sharespace_server.global.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.sharespace.sharespace_server.global.annotation.CheckPermission;
import com.sharespace.sharespace_server.global.exception.CustomRuntimeException;
import com.sharespace.sharespace_server.global.exception.error.UserException;
import com.sharespace.sharespace_server.global.utils.RequestParser;

@Aspect
@Component
public class PermissionAspect {
	@Around("@annotation(checkPermission)")
	public Object checkPermission(ProceedingJoinPoint joinPoint, CheckPermission checkPermission) throws Throwable{
		String[] requiredRoles = checkPermission.roles();
		String userCurrentRole = RequestParser.getCurrentUserRole();

		// 비로그인한 사용자의 경우, 예외를 던진다.
		if (userCurrentRole.equals("ROLE_ANONYMOUS")) {
			throw new CustomRuntimeException(UserException.NOT_LOGGED_IN_USER);
		}

		if (!Arrays.asList(requiredRoles).contains(userCurrentRole)) {
			throw new CustomRuntimeException(UserException.NOT_AUTHORIZED);
		}

		return joinPoint.proceed();
	}
}
