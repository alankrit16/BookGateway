package com.example.book.gateway.Service;

import com.example.book.gateway.ExceptionHandler.ErrorResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.book.gateway.Response.MappedResponse.responseMap;

@Service
public class ClientService {

    private final WebClient webClientForAuthors;
    private final WebClient webClientForBooks;
    @Autowired
    public ClientService(WebClient.Builder webClientBuilder) {
        this.webClientForAuthors = webClientBuilder.baseUrl("http://localhost:8081").build();
        this.webClientForBooks = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public Mono<Map<String, Object>> allAuthorInfo(String authorId) {
        return webClientForAuthors
                .get()
                .uri("/users/author/{authorId}", authorId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>(){})
                .flatMap(authorInfo -> {
                    String authorName = extractAuthorName(authorInfo);
                    return webClientForBooks.get()
                            .uri("/books/findBookByAuthor?authorName={authorName}&authorId={authorId}", authorName, authorId)
                            .retrieve()
                            .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>(){})
                            .collectList()
                            .map(booksInfoList -> {
                                List<Map<String, Object>> selectedBooksInfo = booksInfoList.stream()
                                        .map(bookInfo -> {
                                            Map<String, Object> selectedFields = new HashMap<>();
                                            Map<String, Object> data = (Map<String, Object>) bookInfo.get("data");
                                            selectedFields.put("bookId", data.get("bookId"));
                                            selectedFields.put("bookName", data.get("bookName"));
                                            selectedFields.put("publisherName", data.get("publisherName"));
                                            return selectedFields;
                                        })
                                        .toList();
                                Map<String, Object> response = new HashMap<>();
                                response.put("authorName", authorName);
                                response.put("authorId", authorId);
                                response.put("books",selectedBooksInfo);
                                return responseMap(200,"SUCCESS",response);
                            })
                            .switchIfEmpty(Mono.error(new RuntimeException("No Books Registered By this Author")))
                            .onErrorResume(e -> Mono.just(responseMap(500, "FAILED", e.getMessage())));
                })
                .switchIfEmpty(Mono.just(responseMap(404, "Author Not Found", null)))
                .onErrorResume(e -> Mono.just(responseMap(500, "FAILED", e.getMessage())));
    }

    @SuppressWarnings("unchecked")
    private String extractAuthorName(Map<String, Object> authorInfo) {
        if (authorInfo.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) authorInfo.get("data");
            if (data.containsKey("authorName")) {
                return (String) data.get("authorName");
            }
        }
        return null; // Handle the case when authorName is not found
    }
}
