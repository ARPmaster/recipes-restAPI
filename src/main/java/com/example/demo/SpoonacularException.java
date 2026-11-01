package com.example.demo;

public class SpoonacularException extends RuntimeException {

	public enum Reason {
		INVALID_API_KEY,
		QUOTA_EXCEEDED,
		UPSTREAM_ERROR
	}

	private final Reason reason;

	public SpoonacularException(Reason reason, String message) {
		super(message);
		this.reason = reason;
	}

	public SpoonacularException(Reason reason, String message, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}

	public Reason getReason() {
		return reason;
	}
}
