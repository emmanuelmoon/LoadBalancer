package org.example.loadbalancer.Exception;

public class BadGatewayException extends RuntimeException {
	public BadGatewayException(String message) {
		super(message);
	}
}
