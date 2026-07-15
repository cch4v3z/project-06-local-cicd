package com.satish.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RepositoryDetailsController {

	@RequestMapping("/")
	public String getProjectDescription() {
		return "This is the sample DevOps Project";
	}
}
