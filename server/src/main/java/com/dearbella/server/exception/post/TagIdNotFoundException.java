package com.dearbella.server.exception.post;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class TagIdNotFoundException extends RuntimeException {
    private String message;

    public TagIdNotFoundException(Long id) {
        super(id.toString());
        message = id.toString();
    }
}
