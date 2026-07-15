package com.satish.demo;

import org.junit.jupiter.api.Test;

import com.satish.demo.controller.RepositoryDetailsController;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryDetailsControllerTest {

	@Test
	void returnsProjectDescription() {
		RepositoryDetailsController controller = new RepositoryDetailsController();

		assertEquals("This is the sample DevOps Project", controller.getProjectDescription());
	}
}
