package com.example.book.gateway.Controller;

import com.example.book.gateway.Service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class Controller {
    private final ClientService clientService;

    @Autowired
    public Controller(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/authorInfo/{authorId}")
    public Mono<ResponseEntity<?>> getAllAuthorInfo(@PathVariable String authorId) {
        return clientService.allAuthorInfo(authorId)
                .map(response -> {
                    int status = (int) response.get("status");
                    String message = (String) response.get("message");
                    Object data = response.get("data");
                    System.out.println("============================" + data);
                    HttpStatus httpStatus = HttpStatus.valueOf(status);

                    if (httpStatus.is2xxSuccessful()) {
                        return ResponseEntity.status(httpStatus)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(data);

                    } else {
                        return ResponseEntity.status(httpStatus)
                                .body(message);
                    }
                })
                .onErrorResume(e -> {
                    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(ResponseEntity.status(httpStatus)
                            .body("An error occurred: " + e.getMessage()));
                });
    }
}
