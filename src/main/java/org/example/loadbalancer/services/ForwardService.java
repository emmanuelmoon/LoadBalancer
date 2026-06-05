package org.example.loadbalancer.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ForwardService {

	private final HealthCheckService healthCheckService;

	private static final Logger logger = Logger.getLogger(ForwardService.class.getName());
	private static final RestClient restClient = RestClient.create();
	private final AtomicInteger currentIndex = new AtomicInteger(0);

	public ForwardService(HealthCheckService healthCheckService) {
		this.healthCheckService = healthCheckService;
	}

	@Async
	public CompletableFuture<String> forwardRequest(String method, String uri, String protocol, String host, String userAgent, String accept) {

		List<String> healthyUrls = healthCheckService.getHealthyServers();

		currentIndex.getAndUpdate(i -> (i + 1) % healthyUrls.size());

		String sb = "Received request from " +
				method +
				" " + uri + " " +
				protocol + "\n" +
				"Host: " + host + "\n" +
				"User-Agent: " + userAgent + "\n" +
				"Accept: " + accept + "\n";


		logger.log(Level.INFO, sb);

		CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> restClient.get().uri(healthyUrls.get(currentIndex.get())).retrieve().body(String.class));
		logger.log(Level.INFO, "Hello from server {0}", healthyUrls.get(currentIndex.get()));

		return result;
	}
}
