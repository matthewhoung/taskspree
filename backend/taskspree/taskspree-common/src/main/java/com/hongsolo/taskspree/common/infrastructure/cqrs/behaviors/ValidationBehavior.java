package com.hongsolo.taskspree.common.infrastructure.cqrs.behaviors;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.application.behaviors.PipelineBehavior;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Order(1) // Ensures Validation runs before other behaviors (like Transaction or Logging)
@RequiredArgsConstructor
public class ValidationBehavior implements PipelineBehavior {

    private final Validator validator;

    @Override
    public <C extends Command<R>, R> R handle(C command, Delegate<R> next) {
        // Validate the Java Record/Object fields annotated with @NotNull, @Size, etc.
        Set<ConstraintViolation<C>> violations = validator.validate(command);

        if (!violations.isEmpty()) {
            // Throwing an exception allows the GlobalExceptionHandler to return a structured 400 Bad Request
            throw new ConstraintViolationException(violations);
        }

        // Proceed to the next link in the chain
        return next.next();
    }
}