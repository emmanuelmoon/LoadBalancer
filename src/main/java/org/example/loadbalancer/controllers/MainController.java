package org.example.loadbalancer.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.loadbalancer.services.ForwardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
public class MainController {

	private final ForwardService forwardService;

	public MainController(ForwardService forwardService) {
		this.forwardService = forwardService;
	}

	@GetMapping("/")
	public String forwardRequest(
			@RequestHeader("Host") String host,
			@RequestHeader("User-Agent") String userAgent,
			@RequestHeader("Accept") String accept,
			HttpServletRequest request
	) throws ExecutionException, InterruptedException {
		String method = request.getMethod();
		String uri = request.getRequestURI();
		String protocol = request.getProtocol();
		return forwardService.forwardRequest(method, uri, protocol, host, userAgent, accept).get();
	}

}
