package com.libre.video.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: Libre
 * @Date: 2022/5/8 1:12 AM
 */
@Controller
public class RouteController {

	@GetMapping("/index")
	public String index() {
		return "index";
	}

}
