package com.hongsolo.taskspree.common.presentation;

import com.hongsolo.taskspree.common.domain.Error;
import com.hongsolo.taskspree.common.domain.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;

public abstract class ApiController {

    protected ResponseEntity<?> handleResult(Result<?> result) {
        if (result.isSuccess()) {
            // If value is void/null, return 204 No Content, else 200 OK
            return result instanceof Result.Success<?> s && s.value() == null
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(result); // Or just return the value directly depending on preference
        }

        return handleFailure(result.error());
    }

    private ResponseEntity<ProblemDetail> handleFailure(Error error) {
        HttpStatus status = switch (error.type()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return ResponseEntity.status(status).body(mapToProblemDetail(error, status));
    }

    private ProblemDetail mapToProblemDetail(Error error, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, error.description());
        problemDetail.setTitle(error.code());
        problemDetail.setType(URI.create("https://taskspree.com/errors/" + error.code()));
        return problemDetail;
    }
}