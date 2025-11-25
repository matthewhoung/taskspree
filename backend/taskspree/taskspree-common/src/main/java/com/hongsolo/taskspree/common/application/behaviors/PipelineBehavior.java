package com.hongsolo.taskspree.common.application.behaviors;

import com.hongsolo.taskspree.common.application.cqrs.Command;

/**
 * Represents a cross-cutting concern (middleware) in the command processing pipeline.
 * Examples: Logging, Validation, Transaction management.
 */
public interface PipelineBehavior {

    /**
     * Delegate representing the next step in the pipeline.
     */
    interface Delegate<R> {
        R next();
    }

    /**
     * Process the command and optionally call the next step in the pipeline.
     * @param command The command being processed.
     * @param next The next step in the pipeline (either another behavior or the final handler).
     * @param <C> The command type.
     * @param <R> The result type.
     * @return The result of the execution.
     */
    <C extends Command<R>, R> R handle(C command, Delegate<R> next);
}