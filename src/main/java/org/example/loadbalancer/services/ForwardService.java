package org.example.loadbalancer.services;

import org.example.loadbalancer.Exception.BadGatewayException;
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
		String sb = "Received request from " +
				method +
				" " + uri + " " +
				protocol + "\n" +
				"Host: " + host + "\n" +
				"User-Agent: " + userAgent + "\n" +
				"Accept: " + accept + "\n";


		logger.log(Level.INFO, sb);

		return CompletableFuture.supplyAsync(this::retry);
	}

	private String retry() {
		List<String> healthyUrls = healthCheckService.getHealthyServers();

		for (int i = 0; i < healthyUrls.size(); i++) {
			int index = currentIndex.getAndUpdate(idx -> (idx + 1) % healthyUrls.size());

			try {
				logger.log(Level.INFO, "Forwarding request to {0}", healthyUrls.get(index));
				return restClient.get().uri(healthyUrls.get(index)).retrieve().body(String.class);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to forward request to {0}: {1}", new Object[]{healthyUrls.get(currentIndex.get()), e.getMessage()});
				healthCheckService.removeServer(i);
			}
		}

		throw new BadGatewayException("502 Bad Gateway: All backend servers failed to process the request.");
	}
}
