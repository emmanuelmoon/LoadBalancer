package org.example.loadbalancer.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HealthCheckService {
	@Value("${forward.urls}")
	private String[] forwardUrls;

	private static final RestClient restClient = RestClient.create();

	private volatile List<String> healthyServers;

	@Scheduled(fixedRate = 10000)
	public void healthCheck() {
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			Map<String, Future<Boolean>> futures = Arrays.stream(forwardUrls).collect(Collectors.toMap(
					Function.identity(), url -> executor.submit(()-> isHealthy(url))));
			healthyServers = futures.entrySet().stream()
					.filter(entry -> {
						try {
							return entry.getValue().get();
						} catch (InterruptedException | ExecutionException e) {
							return false;
						}
					})
					.map(Map.Entry::getKey)
					.toList();
		}
	}

	private boolean isHealthy(String url) {
		return restClient.get().uri(url).retrieve().toEntity(String.class).getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(200));
	}

	public List<String> getHealthyServers() {
		return healthyServers;
	}

	public void removeServer(int index) {
		healthyServers.remove(index);
	}

}
