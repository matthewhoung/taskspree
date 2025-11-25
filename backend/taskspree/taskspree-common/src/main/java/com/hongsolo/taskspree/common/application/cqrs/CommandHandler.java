package com.hongsolo.taskspree.common.application.cqrs;

/**
 * Defines a handler for a specific command.
 * @param <C> The type of the command to handle.
 * @param <R> The type of the result.
 */
public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);
}