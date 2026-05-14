package com.ssafy.yamyam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/yamyam")
public class YamYamLogController {

	@GetMapping("/list")
	public String yamYamList() {
		return "yamyamlog/yamyamlog_list";
	}
	
}
