package com.libre.video.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

	private static final String TOKEN_PREFIX = "login:token:";

	private final StringRedisTemplate redisTemplate;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String token = request.getHeader("Authorization");
		if (token == null || token.isBlank()) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"code\":401,\"success\":false,\"msg\":\"未登录\"}");
			return false;
		}
		String username = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
		if (username == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"code\":401,\"success\":false,\"msg\":\"token 无效或已过期\"}");
			return false;
		}
		return true;
	}

}
