package com.example.book.gateway.ExceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.book.gateway.Response.MappedResponse.responseMap;

public class ErrorResponseHandler {
    public static Mono<Map<String, Object>> handleError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            String responseBody = responseException.getResponseBodyAsString();

            HttpStatus httpStatus = HttpStatus.resolve(statusCode);

            if (httpStatus != null) {
                if (statusCode >= 400 && statusCode < 500) {
                    // Handle 4xx client errors
                    return Mono.just(responseMap(statusCode, httpStatus.getReasonPhrase(), responseBody));
                } else if (statusCode >= 500 && statusCode < 600) {
                    // Handle 5xx server errors
                    return Mono.just(responseMap(statusCode, httpStatus.getReasonPhrase(), responseBody));
                }
            }
        }

        // Handle other errors here
        return Mono.just(responseMap(500, "Internal Server Error", throwable.getMessage()));
    }
}
