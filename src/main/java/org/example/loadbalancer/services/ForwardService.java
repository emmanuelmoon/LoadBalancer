package org.example.loadbalancer.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ForwardService {
	private static final Logger logger = Logger.getLogger(ForwardService.class.getName());
	private static final RestClient restClient = RestClient.create();

	@Async
	public void forwardRequest(String ip, String method, String uri, String protocol, String host, String userAgent, String accept) {
		String sb = "Received request from " +
				ip + "\n" +
				method +
				" " + uri + " " +
				protocol + "\n" +
				"Host: " + host + "\n" +
				"User-Agent: " + userAgent + "\n" +
				"Accept: " + accept + "\n";


		logger.log(Level.INFO, sb);

		restClient.get().uri("https://dummyjson.com/c/3029-d29f-4014-9fb4").retrieve().body(String.class);
	}
}
