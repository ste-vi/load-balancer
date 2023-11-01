package org.stevi.balancer.exceptions;

import lombok.Getter;
import org.stevi.balancer.enumeration.HttpStatusCode;

@Getter
public class HttpRequestException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final HttpRequestExceptionBody errorMessage;

    public HttpRequestException(HttpStatusCode statusCode, String message) {
        this.statusCode = statusCode;
        this.errorMessage = new HttpRequestExceptionBody(message);
    }

    public record HttpRequestExceptionBody(String message) {

        @Override
        public String toString() {
            return "{\"message\": \"%s\"}".formatted(message);
        }
    }
}
