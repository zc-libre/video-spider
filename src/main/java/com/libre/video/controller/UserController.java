package com.libre.video.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.libre.core.exception.LibreException;
import com.libre.core.result.R;
import com.libre.video.mapper.UserMapper;
import com.libre.video.pojo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/video/user")
@RequiredArgsConstructor
public class UserController {

	private static final String TOKEN_PREFIX = "login:token:";

	private static final long TOKEN_TTL_HOURS = 24;

	private final UserMapper userMapper;

	private final StringRedisTemplate redisTemplate;

	@PostMapping("/login")
	public R<Map<String, Object>> login(@RequestBody User user) {
		User dbUser = Optional.ofNullable(userMapper.findByUsername(user.getUsername()))
				.orElseThrow(() -> new LibreException("用户不存在"));
		if (!dbUser.getPassword().equals(user.getPassword())) {
			throw new LibreException("用户名或密码错误");
		}
		String token = UUID.randomUUID().toString();
		redisTemplate.opsForValue().set(TOKEN_PREFIX + token, dbUser.getUsername(), TOKEN_TTL_HOURS, TimeUnit.HOURS);
		Map<String, Object> map = Maps.newHashMap();
		map.put("token", token);
		return R.data(map);
	}

	@PostMapping("/info")
	public R<Map<String, Object>> info(@RequestHeader("Authorization") String token) {
		String username = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
		if (username == null) {
			throw new LibreException("token 无效或已过期");
		}
		User dbUser = Optional.ofNullable(userMapper.findByUsername(username))
				.orElseThrow(() -> new LibreException("用户不存在"));
		Map<String, Object> map = Maps.newHashMap();
		map.put("roles", ImmutableList.of("admin"));
		map.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
		map.put("introduction", "I am a super administrator");
		map.put("name", dbUser.getUsername());
		return R.data(map);
	}

	@PostMapping("/logout")
	public R<Boolean> logout(@RequestHeader("Authorization") String token) {
		redisTemplate.delete(TOKEN_PREFIX + token);
		return R.status(true);
	}

}
