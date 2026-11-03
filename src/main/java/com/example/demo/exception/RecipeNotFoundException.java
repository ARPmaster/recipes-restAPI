package com.example.demo.exception;

/** Se lanza cuando una búsqueda no da ningún resultado. La API responde con un 404. */
public class RecipeNotFoundException extends RuntimeException {
    /**
     * Crea la excepción.
     *
     * @param message mensaje de error que recibirá el cliente
     */
    public RecipeNotFoundException(String message) {
        super(message);
    }
}
