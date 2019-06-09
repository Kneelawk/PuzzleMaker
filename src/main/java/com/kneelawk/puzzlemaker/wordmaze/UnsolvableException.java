package com.kneelawk.puzzlemaker.wordmaze;

public class UnsolvableException extends Exception {
	public UnsolvableException() {
	}

	public UnsolvableException(String message) {
		super(message);
	}

	public UnsolvableException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsolvableException(Throwable cause) {
		super(cause);
	}
}
