package org.example.loadbalancer.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.loadbalancer.services.ForwardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

	private final ForwardService forwardService;

	public MainController(ForwardService forwardService) {
		this.forwardService = forwardService;
	}

	@GetMapping("/")
	public void forwardRequest(
			@RequestHeader("Host") String host,
			@RequestHeader("X-Forwarded-For") String ip,
			@RequestHeader("User-Agent") String userAgent,
			@RequestHeader("Accept") String accept,
			HttpServletRequest request
	) {
		String method = request.getMethod();
		String uri = request.getRequestURI();
		String protocol = request.getProtocol();
		forwardService.forwardRequest(ip, method, uri, protocol, host, userAgent, accept);
	}

}
