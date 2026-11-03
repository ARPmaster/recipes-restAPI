package com.example.demo.exception;

/** Se lanza cuando falla la comunicación con Spoonacular. */
public class SpoonacularException extends RuntimeException {

	/** Motivos posibles del fallo: clave inválida, cuota agotada u otro error del proveedor. */
	public enum Reason {
		INVALID_API_KEY,
		QUOTA_EXCEEDED,
		UPSTREAM_ERROR
	}

	private final Reason reason;

	/**
	 * Crea la excepción.
	 *
	 * @param reason motivo del fallo
	 * @param message descripción del fallo; se usa en los logs y no se envía al cliente
	 */
	public SpoonacularException(Reason reason, String message) {
		super(message);
		this.reason = reason;
	}

	/**
	 * Crea la excepción conservando el error original.
	 *
	 * @param reason motivo del fallo
	 * @param message descripción del fallo; se usa en los logs y no se envía al cliente
	 * @param cause error original que provocó el fallo
	 */
	public SpoonacularException(Reason reason, String message, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}

	/**
	 * Devuelve el motivo del fallo.
	 *
	 * @return el motivo del fallo
	 */
	public Reason getReason() {
		return reason;
	}
}
