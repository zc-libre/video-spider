package com.libre.video.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.libre.core.exception.LibreException;
import com.libre.core.result.R;
import com.libre.video.mapper.UserMapper;
import com.libre.video.pojo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * @author: Libre
 * @Date: 2022/5/14 3:50 AM
 */
@RestController
@RequestMapping("/video/user")
@RequiredArgsConstructor
public class UserController {
	private final UserMapper userMapper;

	/**
	 * 简单登录使用
	 * @return /
	 */
	@PostMapping("/login")
	public R<Map<String, Object>> login(@RequestBody User user) {
		User dbUser = Optional.ofNullable(userMapper.findByUsername(user.getUsername())).orElseThrow(() -> new LibreException("用户不存在"));
		if (!dbUser.getPassword().equals(user.getPassword())) {
			throw new LibreException("用户名或密码错误");
		}
		Map<String, Object> map = Maps.newHashMap();
		map.put("token", user.getUsername());
		return R.data(map);
	}

	@PostMapping("/info")
	public R<Map<String, Object>> info(String token) {
		User dbUser = Optional.ofNullable(userMapper.findByUsername(token)).orElseThrow(() -> new LibreException("用户不存在"));
		Map<String, Object> map = Maps.newHashMap();
		map.put("roles", ImmutableList.of("admin"));
		map.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
		map.put("introduction", "I am a super administrator");
		map.put("name", dbUser.getUsername());
		return R.data(map);
	}
}
